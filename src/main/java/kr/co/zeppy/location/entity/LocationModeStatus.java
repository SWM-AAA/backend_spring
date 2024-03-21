package kr.co.zeppy.location.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationModeStatus {
    ACCURATE,
    AMBIGUOUS,
    PINNED,
}
