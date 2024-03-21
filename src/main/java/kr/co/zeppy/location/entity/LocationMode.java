package kr.co.zeppy.location.entity;

import jakarta.persistence.*;
import kr.co.zeppy.user.entity.User;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
@Table(name = "LOCATION_MODES")
public class LocationMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_mode_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    private User friend;

    @Enumerated(EnumType.STRING)
    private LocationModeStatus status;

    public void changeLocationMode(LocationModeStatus status) { this.status = status; }
}
