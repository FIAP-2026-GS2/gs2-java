package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.IssueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface IssueReportRepository extends Repository<IssueReport, Long> {

    Optional<IssueReport> findById(Long id);

    @Query("""
        SELECT i FROM IssueReport i
        LEFT JOIN i.monitoringPoint mp
        WHERE (:typeId IS NULL OR i.issueType.id = :typeId)
          AND (:status IS NULL OR i.status = :status)
          AND (:neighborhood IS NULL OR LOWER(mp.neighborhood) LIKE LOWER(CONCAT('%', :neighborhood, '%')))
          AND (:from IS NULL OR i.openingDate >= :from)
          AND (:to IS NULL OR i.openingDate <= :to)
        """)
    Page<IssueReport> search(@Param("typeId") Long typeId,
                             @Param("status") String status,
                             @Param("neighborhood") String neighborhood,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to,
                             Pageable pageable);
}