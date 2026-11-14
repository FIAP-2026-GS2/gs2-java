package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.request.AdminUserUpdateRequest;
import br.com.sampatech.portal.dto.request.UpdateProfileRequest;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PlatformUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //mobile

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return UserResponse.from(getByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        PlatformUser user = getByEmail(email);
        user.setName(req.name().trim());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void deactivateOwnAccount(String email) {
        getByEmail(email).setActive(false);
    }

    //admin

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getById(id));
    }

    @Transactional
    public UserResponse adminUpdate(Long id, AdminUserUpdateRequest req) {
        PlatformUser user = getById(id);
        user.setUserType(req.userType());
        user.setActive(req.active());
        return UserResponse.from(user);
    }

    @Transactional
    public void deactivate(Long id) {
        getById(id).setActive(false);
    }



    private PlatformUser getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private PlatformUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário " + id + " não encontrado"));
    }
}