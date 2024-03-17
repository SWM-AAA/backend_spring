package kr.co.zeppy.location.repository;

import kr.co.zeppy.location.dto.FriendInfo;
import kr.co.zeppy.location.entity.LocationMode;
import kr.co.zeppy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationModeRepository extends JpaRepository<LocationMode, Long> {

    @Query("SELECT l.friend.id, u.userTag, u.imageUrl FROM LocationMode l INNER JOIN User u ON l.friend.id = u.id WHERE l.user.id = :userId AND l.status = 'ACCURATE'")
    List<FriendInfo> findAccurateFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT l.friend.id, u.userTag, u.imageUrl FROM LocationMode l INNER JOIN User u ON l.friend.id = u.id WHERE l.user.id = :userId AND l.status = 'AMBIGUOUS'")
    List<FriendInfo> findAmbiguousFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT l.friend.id, u.userTag, u.imageUrl FROM LocationMode l INNER JOIN User u ON l.friend.id = u.id WHERE l.user.id = :userId AND l.status = 'PINNED'")
    List<FriendInfo> findPinnedFriendsByUserId(@Param("userId") Long userId);
}
