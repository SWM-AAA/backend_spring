package kr.co.zeppy.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.entity.LocationMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import kr.co.zeppy.global.entity.BaseModel;
import org.hibernate.annotations.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "USERS")
@AllArgsConstructor
public class User extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String nickname;

    private String imageUrl;

    private String username;

    private String password;

    @ColumnDefault("true")
    private Boolean activated;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserCrew> userCrews = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Friendship> sentFriendships = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY)
    private Set<Friendship> receivedFriendships = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserChatRoom> userChatRooms = new HashSet<>();

    // ex) 닉네임#0001
    @Column(unique = true)
    private String userTag;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE

    private String socialId;

    private String refreshToken;

    @Builder.Default
    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY)
    private Set<LocationMode> accurateFriends = new HashSet<>(); // 나를 accurate로 지정한 친구들 ?

    @Builder.Default
    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY)
    private Set<LocationMode> ambiguousFriends = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY)
    private Set<LocationMode> pinnedFriends = new HashSet<>();

    public void authorizeUser() {
        this.role = Role.USER;
    }

    public void updateNickname(String updateNickname) {
        this.nickname = updateNickname;
    }

    public void updateUserTag(String updateUserTag) {
        this.userTag = updateUserTag;
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }

    public void updateImageUrl(String updateImageUrl) {
        this.imageUrl = updateImageUrl;
    }

    public void leaveCrew(UserCrew crew) {
        userCrews.remove(crew);
    }

    public void addSentFriendships(Friendship friendship) {
        this.sentFriendships.add(friendship);
    }

    public void removeSentFriendships(Friendship friendship) {
        this.sentFriendships.remove(friendship);
    }

    public void addReceivedFriendships(Friendship friendship) {
        this.receivedFriendships.add(friendship);
    }

    public void removeReceivedFriendships(Friendship friendship) {
        this.receivedFriendships.remove(friendship);
    }

    public void addAccurateFriends(LocationMode locationMode) {
        this.accurateFriends.add(locationMode);
    }
    public void removeAccurateFriends(LocationMode locationMode) {
        this.accurateFriends.remove(locationMode);
    }
    public void addAmbiguousFriends(LocationMode locationMode) {
        this.ambiguousFriends.add(locationMode);
    }
    public void removeAmbiguousFriends(LocationMode locationMode) {
        this.ambiguousFriends.remove(locationMode);
    }
    public void addPinnedFriends(LocationMode locationMode) {
        this.pinnedFriends.add(locationMode);
    }
    public void removePinnedFriends(LocationMode locationMode) {
        this.pinnedFriends.remove(locationMode);
    }

    // 아이디 패스워드 회원가입 시 비밀번호 암호화
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void setDeleted() {
        this.activated = false;
    }
}
