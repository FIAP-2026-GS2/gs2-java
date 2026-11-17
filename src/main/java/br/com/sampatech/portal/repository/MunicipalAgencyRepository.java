package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.MunicipalAgency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipalAgencyRepository extends JpaRepository<MunicipalAgency, Long> {

    Page<MunicipalAgency> findByActiveTrue(Pageable pageable);
}