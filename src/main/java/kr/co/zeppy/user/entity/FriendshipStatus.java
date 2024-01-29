package kr.co.zeppy.user.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINE,
}