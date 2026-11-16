package br.com.sampatech.notification.listener;

import br.com.sampatech.notification.config.RabbitConfig;
import br.com.sampatech.notification.entity.SearchLog;
import br.com.sampatech.notification.messaging.SearchPerformedEvent;
import br.com.sampatech.notification.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchListener {

    private static final Logger log = LoggerFactory.getLogger(SearchListener.class);

    private final SearchLogRepository searchLogRepository;

    @RabbitListener(queues = RabbitConfig.QUEUE_SEARCH_PERFORMED)
    public void onSearchPerformed(SearchPerformedEvent event) {
        log.info("Recebido consulta.realizada: bairro={}, origem={}", event.neighborhood(), event.origin());

        searchLogRepository.save(SearchLog.builder()
                .userId(event.userId())
                .issueTypeId(event.issueTypeId())
                .neighborhood(event.neighborhood())
                .origin(event.origin())
                .searchedAt(event.searchedAt())
                .build());
    }
}