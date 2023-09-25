package kr.co.zeppy.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import kr.co.zeppy.global.entity.BaseModel;

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

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<UserCrew> userCrews = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Set<Friendship> sentFriendships = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "friend")
    private Set<Friendship> receivedFriendships = new HashSet<>();

    // ex) 닉네임#0001
    @Column(unique = true)
    private String userTag;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE

    private String socialId;

    private String refreshToken;

    // chatroom
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserChatRoom> userChatRooms = new HashSet<>();

    // chatmessages
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserChatMessage> userChatMessages = new HashSet<>();

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
}
