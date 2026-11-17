package br.com.sampatech.portal.service;

import br.com.sampatech.portal.ai.IssueSearchTools;
import br.com.sampatech.portal.dto.response.ChatAnswerResponse;
import br.com.sampatech.portal.entity.PlatformUser;
import br.com.sampatech.portal.messaging.ChatMessageEvent;
import br.com.sampatech.portal.messaging.EventPublisher;
import br.com.sampatech.portal.repository.PlatformUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT = """
            Você é o assistente do SampaTech, o Portal da Transparência da zeladoria de São Paulo.
            As ocorrências são detectadas pelas câmeras do Smart Sampa e analisadas por IA.

            Regras:
            - Responda sempre em português, de forma curta e clara.
            - Use SOMENTE os dados retornados pelas ferramentas. Nunca invente ocorrências, datas ou status.
            - Para filtrar por tipo, chame primeiro listarTiposDeOcorrencia para descobrir o typeId.
            - Ao citar uma ocorrência, informe: título, bairro, data de abertura (dd/MM/aaaa) e se foi resolvida (sim/não, com a data).
            - Se não houver resultados, diga isso claramente.
            - Responda só sobre ocorrências urbanas (infraestrutura, acessibilidade, limpeza, iluminação).
              Para outros assuntos, explique educadamente que só pode ajudar com isso.
            - Nunca mostre dados pessoais de usuários.
            """;

    private final ChatClient chatClient;
    private final PlatformUserRepository userRepository;
    private final EventPublisher eventPublisher;

    public ChatService(ChatClient.Builder builder, IssueSearchTools tools,
                       PlatformUserRepository userRepository, EventPublisher eventPublisher) {
        this.chatClient = builder.defaultTools(tools).build();
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public ChatAnswerResponse ask(String message, String userEmail) {
        PlatformUser user = (userEmail == null) ? null
                : userRepository.findByEmail(userEmail).orElse(null);

        String context = "Data de hoje: " + LocalDate.now() + ".";
        if (user != null) {
            context += " O usuário logado se chama " + user.getName().split(" ")[0]
                    + ". Cumprimente-o pelo nome.";
        }

        String answer;
        try {
            answer = chatClient.prompt()
                    .system(SYSTEM_PROMPT + "\n" + context)
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Falha ao consultar o LLM", e);
            return new ChatAnswerResponse(
                    "O assistente está indisponível ou com muitas consultas agora. Tente novamente em instantes.");
        }

        if (user != null) {
            eventPublisher.publishChatMessage(
                    new ChatMessageEvent(user.getId(), message, answer, LocalDateTime.now()));
        }
        return new ChatAnswerResponse(answer);
    }
}