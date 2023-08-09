package kr.co.zeppy.user.repository;

import java.util.Optional;

import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserTag(String userTag);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
