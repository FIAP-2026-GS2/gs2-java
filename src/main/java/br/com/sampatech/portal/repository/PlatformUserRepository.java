package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {

    Optional<PlatformUser> findByEmail(String email);

    boolean existsByEmail(String email);
}