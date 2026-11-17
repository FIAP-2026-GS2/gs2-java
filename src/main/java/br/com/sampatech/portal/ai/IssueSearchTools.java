package br.com.sampatech.portal.ai;

import br.com.sampatech.portal.dto.response.IssueDetailResponse;
import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.service.IssueService;
import br.com.sampatech.portal.service.IssueTypeService;
import br.com.sampatech.portal.service.SearchTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueSearchTools {

    private final IssueService issueService;
    private final IssueTypeService issueTypeService;
    private final SearchTrackingService searchTrackingService;

    @Tool(description = "Lista os tipos de ocorrência urbana disponíveis, com id, nome e categoria. "
            + "Use para descobrir o typeId antes de filtrar ocorrências por tipo.")
    public List<IssueTypeResponse> listarTiposDeOcorrencia() {
        return issueTypeService.listActive();
    }

    @Tool(description = "Busca ocorrências urbanas em São Paulo, com filtros opcionais. "
            + "Retorna até 10 ocorrências, das mais recentes para as mais antigas.")
    public List<IssueSummaryResponse> buscarOcorrencias(
            @ToolParam(required = false, description = "Id do tipo de ocorrência (obtido em listarTiposDeOcorrencia)")
            Long typeId,
            @ToolParam(required = false, description = "Status exato: OPEN, ASSIGNED, IN_PROGRESS, VALIDATION ou RESOLVED. "
                    + "Não use para perguntas sobre ocorrências 'abertas'; nesse caso, deixe vazio.")
            String status,
            @ToolParam(required = false, description = "Nome ou parte do nome do bairro, ex.: Bela Vista")
            String bairro,
            @ToolParam(required = false, description = "Data inicial no formato AAAA-MM-DD")
            String dataInicio,
            @ToolParam(required = false, description = "Data final no formato AAAA-MM-DD")
            String dataFim) {

        searchTrackingService.track(typeId, bairro, "CHAT", null);

        var page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "openingDate"));
        return issueService.search(typeId, status, bairro, toDate(dataInicio), toDate(dataFim), page)
                .getContent();
    }

    @Tool(description = "Detalha uma ocorrência pelo id, incluindo a linha do tempo de status "
            + "(quando foi aberta, encaminhada e resolvida).")
    public IssueDetailResponse detalharOcorrencia(
            @ToolParam(description = "Id da ocorrência") Long id) {
        return issueService.findById(id);
    }

    private LocalDate toDate(String value) {
        try {
            return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}