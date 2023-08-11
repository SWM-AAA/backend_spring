package kr.co.zeppy.user.repository;
import java.util.Optional;
import kr.co.zeppy.user.entity.Friendship;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long>{
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
}