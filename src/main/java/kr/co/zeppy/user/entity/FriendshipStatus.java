package kr.co.zeppy.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendshipStatus {
    PENDING,
    ACCEPTED,
}