package br.com.sampatech.portal.security;

import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PlatformUserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        PlatformUser user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getUserType().name())
                .disabled(!user.getActive())
                .accountLocked(loginAttemptService.isBlocked(normalized))
                .build();
    }
}
