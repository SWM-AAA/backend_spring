package kr.co.zeppy.user.repository;
import java.util.List;
import java.util.Optional;
import kr.co.zeppy.user.entity.Friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long>{
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") Long userId);
}