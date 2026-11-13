package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueTypeRepository extends JpaRepository<IssueType, Long> {

    List<IssueType> findByActiveTrueOrderByNameAsc();

    Page<IssueType> findByActiveTrue(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}