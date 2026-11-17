package br.com.sampatech.portal.dto.response;

public record AuthResponse(String token, String type, UserResponse user) {
    public static AuthResponse bearer(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}