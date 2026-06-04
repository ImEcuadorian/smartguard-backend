package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.api.CreateUserRequest;
import io.github.imecuadorian.smartguardbackend.security.api.UpdateUserStatusRequest;
import io.github.imecuadorian.smartguardbackend.security.api.UserAccountMapper;
import io.github.imecuadorian.smartguardbackend.security.api.UserAccountResponse;
import io.github.imecuadorian.smartguardbackend.security.domain.UserAccount;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.UserAccountRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;

    public UserService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder,
                       UserAccountMapper userAccountMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAccountMapper = userAccountMapper;
    }

    public UserAccountResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        var user = new UserAccount(
                username,
                passwordEncoder.encode(request.password()),
                request.displayName(),
                request.role()
        );
        return userAccountMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserAccountResponse> findAll() {
        return userRepository.findAllByOrderByUsernameAsc()
                .stream()
                .map(userAccountMapper::toResponse)
                .toList();
    }

    public UserAccountResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.updateStatus(request.status());
        return userAccountMapper.toResponse(user);
    }
}
