package ec.com.ecommerce.modules.auth.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    @Override
    public String genefrateToken(UserDetails userDetails) {
        return "accessToken";
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return "refreshToken";
    }
}
