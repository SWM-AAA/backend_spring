package kr.co.zeppy.user.repository;
import java.util.List;
import java.util.Optional;
import kr.co.zeppy.user.entity.Friendship;
import kr.co.zeppy.user.entity.FriendshipStatus;
import kr.co.zeppy.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long>{
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    // 상태가 ACCEPTED인 Friendship 찾기
    Optional<Friendship> findByUserIdAndFriendIdAndStatus(Long userId, Long friendId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
            "WHERE ((f.user.id = :userId AND f.friend.id = :friendId) OR (f.user.id = :friendId AND f.friend.id = :userId)) " +
            "AND f.status = 'ACCEPTED'")
    boolean findIsAcceptFriendshipsByUserId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
            "WHERE (f.user.id = :userId AND f.friend.id = :friendId) OR (f.user.id = :friendId AND f.friend.id = :userId)")
    boolean existsByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query(value = "SELECT CASE WHEN f.user.id = :userId THEN f.friend ELSE f.user END FROM Friendship f WHERE" +
            " (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED'", nativeQuery = true)
    List<User> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT CASE WHEN friendships.user_id = :userId THEN friendships.friend_id ELSE " +
            "friendships.user_id END FROM friendships " +
            "WHERE (friendships.user_id = :userId OR friendships.friend_id = :userId) AND friendships.status = 'ACCEPTED'",
            nativeQuery = true)
    List<Long> findAcceptedFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId)")
    List<Friendship> findByUserIdOrFriendId(@Param("userId") Long userId);
}