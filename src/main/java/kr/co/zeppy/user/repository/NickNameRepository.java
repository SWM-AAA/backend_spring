package kr.co.zeppy.user.repository;

import kr.co.zeppy.user.entity.NicknameCounter;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NickNameRepository extends JpaRepository<NicknameCounter, Long> {
    Optional<NicknameCounter> findByNickname(String nickname);
}
