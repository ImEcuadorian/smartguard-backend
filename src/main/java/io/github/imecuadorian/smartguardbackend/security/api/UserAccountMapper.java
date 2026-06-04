package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.domain.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {
    public UserAccountResponse toResponse(UserAccount user) {
        return new UserAccountResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
