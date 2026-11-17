package br.com.sampatech.notification.repository;

import br.com.sampatech.notification.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {}