package br.com.sampatech.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.YesNoConverter;

@Entity
@Table(name = "MUNICIPAL_AGENCY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MunicipalAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AGENCY")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "CITY", nullable = false, length = 100)
    private String city;

    @Column(name = "STATE", nullable = false, length = 2)
    private String state;

    @Convert(converter = YesNoConverter.class)
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "SUBPREFECTURE", length = 50)
    private String subprefecture;
}