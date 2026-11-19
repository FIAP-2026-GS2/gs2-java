package br.com.sampatech.portal.security;

import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.entity.UserType;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "segredo-de-teste-com-mais-de-32-caracteres-para-hmac";

    private final JwtService jwtService = new JwtService(SECRET, 60);

    private PlatformUser citizen() {
        return PlatformUser.builder()
                .email("cidadao@teste.com")
                .name("Cidadão Teste")
                .userType(UserType.CITIZEN)
                .build();
    }

    @Test
    void tokenCarriesTheEmailAsSubject() {
        String token = jwtService.generateToken(citizen());

        assertEquals("cidadao@teste.com", jwtService.extractEmail(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(citizen());
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThrows(JwtException.class, () -> jwtService.extractEmail(tampered));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService other = new JwtService("outro-segredo-totalmente-diferente-com-32-chars", 60);
        String token = other.generateToken(citizen());

        assertThrows(JwtException.class, () -> jwtService.extractEmail(token));
    }
}
