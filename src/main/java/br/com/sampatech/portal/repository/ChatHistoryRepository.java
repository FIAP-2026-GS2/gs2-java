package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface ChatHistoryRepository extends Repository<ChatHistory, Long> {

    Page<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}