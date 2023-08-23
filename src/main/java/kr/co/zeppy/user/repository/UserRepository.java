package kr.co.zeppy.user.repository;

import java.util.Optional;

import kr.co.zeppy.user.entity.SocialType;
import kr.co.zeppy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserTag(String userTag);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    Optional<User> findById(Long id);

    @Query("SELECT u.id FROM User u WHERE u.userTag = :userTag")
    Optional<Long> findIdByUserTag(@Param("userTag") String userTag);

}
