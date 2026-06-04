package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final Duration expiration;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtService(
            @Value("${smartguard.jwt.secret}") String secret,
            @Value("${smartguard.jwt.expiration-minutes}") long expirationMinutes,
            ObjectMapper objectMapper
    ) {
        this(secret, Duration.ofMinutes(expirationMinutes), objectMapper);
    }

    public JwtService(String secret, Duration expiration, ObjectMapper objectMapper) {
        this.secret = secret;
        this.expiration = expiration;
        this.objectMapper = objectMapper;
    }

    public String createAccessToken(String username, UserRole role) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expiresAt = issuedAt + expiration.toSeconds();
            String header = encodeJson(new JwtHeader("JWT", "HS256"));
            String payload = encodeJson(new JwtClaims(username, role.name(), issuedAt, expiresAt));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Could not create JWT", exception);
        }
    }

    public JwtPrincipal parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new AuthenticationFailedException("Invalid token");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                throw new AuthenticationFailedException("Invalid token signature");
            }

            var claims = objectMapper.readValue(decode(parts[1]), JwtClaims.class);
            if (claims.exp() < Instant.now().getEpochSecond()) {
                throw new AuthenticationFailedException("Token expired");
            }

            return new JwtPrincipal(claims.sub(), UserRole.valueOf(claims.role()));
        } catch (AuthenticationFailedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuthenticationFailedException("Invalid token");
        }
    }

    public long expirationMinutes() {
        return expiration.toMinutes();
    }

    private String encodeJson(Object value) throws Exception {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private record JwtHeader(String typ, String alg) {
    }

    private record JwtClaims(String sub, String role, long iat, long exp) {
    }
}
