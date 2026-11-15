package br.com.sampatech.notification.repository;

import br.com.sampatech.notification.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {}