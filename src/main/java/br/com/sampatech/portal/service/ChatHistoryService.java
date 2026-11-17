package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.response.ChatHistoryResponse;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.ChatHistoryRepository;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final PlatformUserRepository userRepository;

    public Page<ChatHistoryResponse> getHistory(String email, Pageable pageable) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"))
                .getId();
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ChatHistoryResponse::from);
    }
}