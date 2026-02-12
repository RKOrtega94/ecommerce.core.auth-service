package ec.com.ecommerce.auth.application.ports.in;

import ec.com.ecommerce.auth.domain.usecases.FindUserByUsernameUseCase;
import ec.com.ecommerce.remote.security.mappers.UserEntityMapper;
import ec.com.ecommerce.remote.security.projections.LoginUserProjection;
import ec.com.ecommerce.remote.security.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindUserByUsernamePort implements FindUserByUsernameUseCase {
    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public LoginUserProjection execute(String username) {
        return userRepository.findByUsername(username)
                .map(userEntityMapper::toLoginUserProjection)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }
}
