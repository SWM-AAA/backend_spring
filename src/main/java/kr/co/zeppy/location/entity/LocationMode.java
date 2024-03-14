package kr.co.zeppy.location.entity;

import jakarta.persistence.*;
import kr.co.zeppy.user.entity.User;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class LocationMode {

    @Id
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

    public void toAccurate() { this.status = LocationModeStatus.ACCURATE; }

    public void toAmbiguous() { this.status = LocationModeStatus.AMBIGUOUS; }

    public void toPinned() { this.status = LocationModeStatus.PINNED; }
}
