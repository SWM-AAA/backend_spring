package kr.co.zeppy.crew.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.entity.UserCrew;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "CREWS")
@AllArgsConstructor
@Builder
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crew_id")
    private Long id;

    private String crewName;

    @Builder.Default
    @OneToMany(mappedBy = "crew")
    private Set<UserCrew> userCrews = new LinkedHashSet<>();

    public void addUser(UserCrew user) {
        userCrews.add(user);
    }

    public void removeUser(UserCrew user) {
        userCrews.remove(user);
    }
}
