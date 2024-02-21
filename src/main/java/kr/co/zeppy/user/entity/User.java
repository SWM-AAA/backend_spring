package kr.co.zeppy.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
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
@DynamicInsert
@DynamicUpdate
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

    // 아이디 패스워드 회원가입 시 비밀번호 암호화
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void setDeleted() {
        this.activated = false;
    }
}
