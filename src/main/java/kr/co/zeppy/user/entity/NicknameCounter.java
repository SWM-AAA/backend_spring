package kr.co.zeppy.user.entity;

import jakarta.persistence.Id;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "NICKNAME_COUNTER")
@AllArgsConstructor
public class NicknameCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nickname;

    private Integer lastAssignedNumber;

    @Builder.Default
    @ElementCollection
    private Set<Integer> availableNumbers = new HashSet<>();

    public void addAvailableNumber(Integer number) {
        availableNumbers.add(number);
    }

    public boolean hasAvailableNumber() {
        return !availableNumbers.isEmpty();
    }

    public Integer getAndRemoveAvailableNumber() {
        Integer number = availableNumbers.iterator().next();
        availableNumbers.remove(number);
        return number;
    }

    public void setLastAssignedNumber(Integer lastAssignedNumber) {
        this.lastAssignedNumber = lastAssignedNumber;
    }
}
