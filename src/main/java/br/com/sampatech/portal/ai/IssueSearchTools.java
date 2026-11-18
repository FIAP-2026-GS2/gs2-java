package br.com.sampatech.portal.ai;

import br.com.sampatech.portal.dto.response.IssueDetailResponse;
import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.service.IssueService;
import br.com.sampatech.portal.service.IssueTypeService;
import br.com.sampatech.portal.service.SearchTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IssueSearchTools {

    private static final int MAX_RESULTS = 10;
    private static final int UNRESOLVED_SCAN = 100;
    private static final String UNRESOLVED = "UNRESOLVED";
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "openingDate");
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, String> STATUS_ALIASES = Map.ofEntries(
            Map.entry("OPEN", "OPEN"),
            Map.entry("ASSIGNED", "ASSIGNED"),
            Map.entry("ENCAMINHADA", "ASSIGNED"),
            Map.entry("ENCAMINHADO", "ASSIGNED"),
            Map.entry("IN PROGRESS", "IN_PROGRESS"),
            Map.entry("EM ANDAMENTO", "IN_PROGRESS"),
            Map.entry("ANDAMENTO", "IN_PROGRESS"),
            Map.entry("VALIDATION", "VALIDATION"),
            Map.entry("EM VALIDACAO", "VALIDATION"),
            Map.entry("VALIDACAO", "VALIDATION"),
            Map.entry("RESOLVED", "RESOLVED"),
            Map.entry("RESOLVIDA", "RESOLVED"),
            Map.entry("RESOLVIDO", "RESOLVED"),
            Map.entry("RESOLVIDAS", "RESOLVED"),
            Map.entry("ABERTA", UNRESOLVED),
            Map.entry("ABERTO", UNRESOLVED),
            Map.entry("ABERTAS", UNRESOLVED),
            Map.entry("PENDENTE", UNRESOLVED),
            Map.entry("PENDENTES", UNRESOLVED),
            Map.entry("NAO RESOLVIDA", UNRESOLVED),
            Map.entry("NAO RESOLVIDAS", UNRESOLVED),
            Map.entry("NAO RESOLVIDO", UNRESOLVED)
    );

    private final IssueService issueService;
    private final IssueTypeService issueTypeService;
    private final SearchTrackingService searchTrackingService;

    public record SearchResult(long totalEncontrado, int totalMostrado, List<IssueSummaryResponse> ocorrencias) {}

    @Tool(description = "Lista os tipos de ocorrência urbana disponíveis, com id, nome e categoria. "
            + "Use para descobrir o typeId antes de filtrar ocorrências por tipo.")
    public List<IssueTypeResponse> listarTiposDeOcorrencia() {
        return issueTypeService.listActive();
    }

    @Tool(description = "Busca ocorrências urbanas em São Paulo com filtros opcionais. "
            + "Retorna totalEncontrado (quantas existem no total), totalMostrado e até 10 ocorrências, "
            + "das mais recentes para as mais antigas.")
    public SearchResult buscarOcorrencias(
            @ToolParam(required = false, description = "Id do tipo de ocorrência (obtido em listarTiposDeOcorrencia)")
            Long typeId,
            @ToolParam(required = false, description = "Status exato: OPEN, ASSIGNED, IN_PROGRESS, VALIDATION ou RESOLVED. "
                    + "Para ocorrências abertas ou pendentes, não use este campo: use apenasNaoResolvidas.")
            String status,
            @ToolParam(required = false, description = "Local: bairro, rua ou avenida. Use só o nome principal, "
                    + "sem 'Rua' ou 'Avenida'. Ex.: Bela Vista, Paulista, Augusta")
            String bairro,
            @ToolParam(required = false, description = "true para trazer só as NÃO resolvidas "
                    + "(abertas, encaminhadas, em andamento ou em validação). "
                    + "Use em perguntas sobre ocorrências abertas, pendentes ou não resolvidas.")
            Boolean apenasNaoResolvidas,
            @ToolParam(required = false, description = "Data inicial no formato AAAA-MM-DD")
            String dataInicio,
            @ToolParam(required = false, description = "Data final no formato AAAA-MM-DD")
            String dataFim) {

        String statusCode = normalizeStatus(status);
        boolean unresolvedOnly = Boolean.TRUE.equals(apenasNaoResolvidas) || UNRESOLVED.equals(statusCode);
        String location = cleanLocation(bairro);
        LocalDate from = toDate(dataInicio);
        LocalDate to = toDate(dataFim);

        searchTrackingService.track(typeId, location, "CHAT", currentUserEmail());

        if (unresolvedOnly) {
            List<IssueSummaryResponse> unresolved = issueService
                    .search(typeId, null, location, from, to, PageRequest.of(0, UNRESOLVED_SCAN, NEWEST_FIRST))
                    .getContent().stream()
                    .filter(issue -> !"RESOLVED".equals(issue.status()))
                    .toList();
            List<IssueSummaryResponse> shown = unresolved.stream().limit(MAX_RESULTS).toList();
            return new SearchResult(unresolved.size(), shown.size(), shown);
        }

        Page<IssueSummaryResponse> page = issueService
                .search(typeId, statusCode, location, from, to, PageRequest.of(0, MAX_RESULTS, NEWEST_FIRST));
        return new SearchResult(page.getTotalElements(), page.getNumberOfElements(), page.getContent());
    }

    @Tool(description = "Detalha uma ocorrência pelo id, incluindo a linha do tempo de status "
            + "(quando foi aberta, encaminhada e resolvida). Retorna vazio se a ocorrência não existir.")
    public IssueDetailResponse detalharOcorrencia(
            @ToolParam(description = "Id da ocorrência") Long id) {
        try {
            return issueService.findById(id);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) return null;
        String key = Normalizer.normalize(value.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('_', ' ')
                .replaceAll("\\s+", " ");
        return STATUS_ALIASES.get(key);
    }

    private String cleanLocation(String value) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim()
                .replaceFirst("(?i)^(rua|r\\.|avenida|av\\.?|alameda|al\\.|travessa|tv\\.|praça|praca|estrada)\\s+", "")
                .trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private LocalDate toDate(String value) {
        if (value == null || value.isBlank()) return null;
        String text = value.trim();
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            try {
                return LocalDate.parse(text, BR_DATE);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }
}