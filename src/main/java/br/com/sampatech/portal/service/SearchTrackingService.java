package br.com.sampatech.portal.service;

import br.com.sampatech.portal.messaging.EventPublisher;
import br.com.sampatech.portal.messaging.SearchPerformedEvent;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchTrackingService {

    private final EventPublisher eventPublisher;
    private final PlatformUserRepository userRepository;

    public void track(Long typeId, String neighborhood, String origin, String userEmail) {
        boolean noFilter = typeId == null && (neighborhood == null || neighborhood.isBlank());
        if (noFilter) return;

        Long userId = (userEmail == null) ? null
                : userRepository.findByEmail(userEmail).map(u -> u.getId()).orElse(null);

        eventPublisher.publishSearchPerformed(new SearchPerformedEvent(
                userId, typeId,
                neighborhood != null ? neighborhood.trim() : null,
                origin, LocalDateTime.now()));
    }
}