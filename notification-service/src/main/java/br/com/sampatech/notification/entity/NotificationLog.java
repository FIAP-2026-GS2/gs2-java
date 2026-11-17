package br.com.sampatech.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICATION")
    private Long id;

    @Column(name = "USER_ID_USER", nullable = false)
    private Long userId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 50)
    private String eventType;

    @Column(name = "CHANNEL", nullable = false, length = 20)
    private String channel;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "SENT_AT", nullable = false)
    private LocalDateTime sentAt;
}