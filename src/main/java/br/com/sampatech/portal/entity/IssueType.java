package br.com.sampatech.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "ISSUE_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ISSUE_TYPE")
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private IssueCategory category;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active;
}