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

    public Set<User> getFriends() {
        Set<User> friends = new HashSet<>();
        for (Friendship friendship : sentFriendships) {
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                friends.add(friendship.getFriend());
            }
        }
        for (Friendship friendship : receivedFriendships) {
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                friends.add(friendship.getUser());
            }
        }
        return friends;
    }
}
