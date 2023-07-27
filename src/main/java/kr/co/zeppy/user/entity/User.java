package kr.co.zeppy.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kr.co.zeppy.crew.entity.Crew;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "USERS")
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    private Long id;

    private String email;
    private String nickname;
    private String imageUrl;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "USER_CREW",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "crew_id"))
    private Set<Crew> crews = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE

    private String socialId;

    private String refreshToken;

    public void authorizeUser() {
        this.role = Role.USER;
    }

    public void updateNickname(String updateNickname) {
        this.nickname = updateNickname;
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }

    public void updateImageUrl(String updateImageUrl) {
        this.imageUrl = updateImageUrl;
    }

    public void leaveCrew(Crew crew) {
        crews.remove(crew);
        crew.removeUser(this);
    }
}
