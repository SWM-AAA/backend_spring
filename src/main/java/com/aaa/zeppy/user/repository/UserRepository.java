package com.aaa.zeppy.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aaa.zeppy.user.entity.SocialType;
import com.aaa.zeppy.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Optional<User> findByUserName(String userName);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
