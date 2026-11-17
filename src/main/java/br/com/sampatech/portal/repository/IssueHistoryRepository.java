package br.com.sampatech.portal.repository;

import br.com.sampatech.portal.entity.IssueHistory;
import org.springframework.data.repository.Repository;
import java.util.List;

public interface IssueHistoryRepository extends Repository<IssueHistory, Long> {

    List<IssueHistory> findByIssueReportIdOrderByChangeDateAsc(Long issueReportId);
}