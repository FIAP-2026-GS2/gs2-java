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
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final int MEMORY_SIZE = 10;

    private static final String SYSTEM_PROMPT = """
            Você é o assistente do SampaTech, o Portal da Transparência da zeladoria de São Paulo.
            As ocorrências são detectadas pelas câmeras do Smart Sampa e analisadas por IA.

            Regras:
            - Responda sempre em português, de forma curta e clara.
            - Use SOMENTE os dados retornados pelas ferramentas. Nunca invente ocorrências, datas ou status.
            - Para filtrar por tipo, chame primeiro listarTiposDeOcorrencia para descobrir o typeId.
            - Quando o usuário pedir um status específico, use o campo status com o código correspondente:
              encaminhada = ASSIGNED, em andamento = IN_PROGRESS, em validação = VALIDATION, resolvida = RESOLVED.
            - Use apenasNaoResolvidas = true SOMENTE para perguntas genéricas sobre ocorrências
              "abertas", "pendentes" ou "não resolvidas", sem um status específico.
            - Se totalEncontrado for maior que totalMostrado, diga quantas existem no total
              e que está mostrando as mais recentes.
            - Se a pergunta se referir a algo já dito na conversa (ex.: "desses", "essa", "e a outra?"),
              use as mensagens anteriores para entender e responda com base nelas.
            - Escreva todas as datas no formato DD/MM/AAAA.
            - Escreva em texto simples, SEM Markdown: não use asteriscos, negrito, títulos ou listas com *.
            - Para uma ocorrência, responda em uma frase corrida.
              Exemplo: "Há 1 ocorrência não resolvida na Bela Vista: Buraco próximo ao MASP, aberta em 20/10/2026, em andamento."
            - Para várias ocorrências, coloque uma por linha, começando com "- ".
            - Traduza o status: OPEN = aberta, ASSIGNED = encaminhada, IN_PROGRESS = em andamento,
              VALIDATION = em validação, RESOLVED = resolvida (informe a data da resolução).
            - Se perguntarem o que significa cada status, use estas definições:
              aberta = o problema foi detectado pela câmera e registrado, aguardando encaminhamento;
              encaminhada = a ocorrência foi enviada ao órgão municipal responsável;
              em andamento = o órgão responsável está trabalhando na solução;
              em validação = o serviço foi feito e as câmeras estão confirmando se o problema foi resolvido;
              resolvida = a solução foi confirmada.
            - Se não houver resultados, diga isso claramente.
            - Responda só sobre ocorrências urbanas (infraestrutura, acessibilidade, limpeza, iluminação)
              e sobre o funcionamento do SampaTech.
              Para outros assuntos, explique educadamente que só pode ajudar com isso.
            - Nunca mostre dados pessoais de usuários.
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final PlatformUserRepository userRepository;
    private final EventPublisher eventPublisher;

    public ChatService(ChatClient.Builder builder, IssueSearchTools tools,
                       PlatformUserRepository userRepository, EventPublisher eventPublisher) {
        this.chatClient = builder.defaultTools(tools).build();
        this.chatMemory = MessageWindowChatMemory.builder().maxMessages(MEMORY_SIZE).build();
        this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public ChatAnswerResponse ask(String message, String userEmail) {
        return ask(message, userEmail, null);
    }

    public ChatAnswerResponse ask(String message, String userEmail, String sessionId) {
        PlatformUser user = findUser(userEmail);
        String conversationId = conversationId(user, sessionId);

        String context = "Data de hoje: " + LocalDate.now() + ".";
        if (user != null) {
            context += " O usuário logado se chama " + user.getName().split(" ")[0]
                    + ". Cumprimente-o pelo nome só na primeira resposta da conversa.";
        }

        String answer;
        try {
            ChatClient.ChatClientRequestSpec request = chatClient.prompt()
                    .system(SYSTEM_PROMPT + "\n" + context)
                    .user(message);

            if (conversationId != null) {
                request = request
                        .advisors(memoryAdvisor)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
            }

            answer = request.call().content();
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

    public void clearMemory(String userEmail, String sessionId) {
        String conversationId = conversationId(findUser(userEmail), sessionId);
        if (conversationId != null) {
            chatMemory.clear(conversationId);
        }
    }

    private PlatformUser findUser(String userEmail) {
        return (userEmail == null) ? null : userRepository.findByEmail(userEmail).orElse(null);
    }

    private String conversationId(PlatformUser user, String sessionId) {
        if (user != null) {
            return "user-" + user.getId();
        }
        return sessionId != null ? "session-" + sessionId : null;
    }
}