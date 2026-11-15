package br.com.sampatech.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SEARCH_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SEARCH")
    private Long id;

    @Column(name = "USER_ID_USER")
    private Long userId;

    @Column(name = "ISSUE_TYPE_ID_ISSUE_TYPE")
    private Long issueTypeId;

    @Column(name = "NEIGHBORHOOD", length = 100)
    private String neighborhood;

    @Column(name = "ORIGIN", nullable = false, length = 10)
    private String origin;

    @Column(name = "SEARCHED_AT", nullable = false)
    private LocalDateTime searchedAt;
}