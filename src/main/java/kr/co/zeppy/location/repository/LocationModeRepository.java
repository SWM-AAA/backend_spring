package kr.co.zeppy.location.repository;

import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.entity.LocationMode;
import kr.co.zeppy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationModeRepository extends JpaRepository<LocationMode, Long> {

    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId")
    List<LocationMode> findByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM LocationMode l WHERE l.friend.id = :friendId")
    List<LocationMode> findByFriendId(@Param("friendId") Long friendId);

    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId AND l.friend.id = :friendId")
    Optional<LocationMode> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT l.user.id FROM LocationMode l WHERE l.friend.id = :userId")
    List<Long> findFriendIdsByFriendId(@Param("userId") Long userId);

    @Query("SELECT l.user.id FROM LocationMode l WHERE l.friend.id = :userId and l.status = 'ACCURATE'")
    List<Long> findAccurateFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT l.user.id FROM LocationMode l WHERE l.friend.id = :userId and l.status = 'AMBIGUOUS'")
    List<Long> findAmbiguousFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT l.user.id FROM LocationMode l WHERE l.friend.id = :userId and l.status = 'PINNED'")
    List<Long> findPinnedFriendIdsByUserId(@Param("userId") Long userId);
}
