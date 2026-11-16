package br.com.sampatech.portal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "CHAT_HISTORY")
@Getter
@NoArgsConstructor
public class ChatHistory {

    @Id
    @Column(name = "ID_CHAT")
    private Long id;

    @Column(name = "USER_ID_USER")
    private Long userId;

    @Column(name = "QUESTION")
    private String question;

    @Lob
    @Column(name = "ANSWER")
    private String answer;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}