package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.entity.UserType;
import java.time.LocalDateTime;

public record UserResponse(
        Long id, String name, String email, UserType userType,
        LocalDateTime registrationDate, Boolean active
) {
    public static UserResponse from(PlatformUser u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getUserType(), u.getRegistrationDate(), u.getActive());
    }
}