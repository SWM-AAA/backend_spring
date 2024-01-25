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

    Optional<User> findByUsername(String username);

    @Query("SELECT u.imageUrl FROM User u WHERE u.userTag = :userTag")
    Optional<String> findImageUrlByUserTag(@Param("userTag") String userTag);

    @Query("SELECT u.id FROM User u WHERE u.userTag = :userTag")
    Optional<Long> findIdByUserTag(@Param("userTag") String userTag);

    @Query("SELECT u.userTag FROM User u WHERE u.username = :username")
    Optional<String> findUserTagByUsername(@Param("username") String username);

    boolean existsByUserTag(String userTag);
}
