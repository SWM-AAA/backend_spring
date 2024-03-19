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

    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId AND l.friend.id = :friendId")
    Optional<LocationMode> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

//    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId AND l.status = 'ACCURATE'")
//    List<LocationMode> findAccurateFriendsByUserId(@Param("userId") Long userId);
//
//    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId AND l.status = 'AMBIGUOUS'")
//    List<LocationMode> findAmbiguousFriendsByUserId(@Param("userId") Long userId);
//
//    @Query("SELECT l FROM LocationMode l WHERE l.user.id = :userId AND l.status = 'PINNED'")
//    List<LocationMode> findPinnedFriendsByUserId(@Param("userId") Long userId);
}
