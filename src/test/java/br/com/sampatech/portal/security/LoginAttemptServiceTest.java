package br.com.sampatech.portal.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void blocksAfterTooManyFailures() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            assertFalse(service.isBlocked("cidadao@teste.com"));
            service.loginFailed("Cidadao@Teste.com");
        }

        assertTrue(service.isBlocked("cidadao@teste.com"));
    }

    @Test
    void successfulLoginClearsTheCounter() {
        service.loginFailed("cidadao@teste.com");
        service.loginFailed("cidadao@teste.com");
        service.loginSucceeded("cidadao@teste.com");

        assertFalse(service.isBlocked("cidadao@teste.com"));
    }
}
