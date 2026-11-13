package br.com.sampatech.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "ISSUE_HISTORY")
@Getter
@NoArgsConstructor
public class IssueHistory {

    @Id
    @Column(name = "ID_HISTORY")
    private Long id;

    @Column(name = "PREVIOUS_STATUS")
    private String previousStatus;

    @Column(name = "NEW_STATUS")
    private String newStatus;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "CHANGE_DATE")
    private LocalDateTime changeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID_USER")
    private PlatformUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISSUE_REPORT_ID_ISSUE_REPORT")
    private IssueReport issueReport;
}