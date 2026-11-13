package br.com.sampatech.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "ISSUE_REPORT")
@Getter
@NoArgsConstructor
public class IssueReport {

    @Id
    @Column(name = "ID_ISSUE_REPORT")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE")
    private IssueSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERITY")
    private Severity severity;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "OPENING_DATE")
    private LocalDateTime openingDate;

    @Column(name = "RESOLUTION_DATE")
    private LocalDateTime resolutionDate;

    @Column(name = "LATITUDE")
    private BigDecimal latitude;

    @Column(name = "LONGITUDE")
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ISSUE_TYPE_ID_ISSUE_TYPE")
    private IssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID_USER")
    private PlatformUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MONITORING_POINT_ID_POINT")
    private MonitoringPoint monitoringPoint;

    @Column(name = "LEVEL_NUMBER")
    private Long levelNumber;

    @Column(name = "SIDE")
    private String side;
}