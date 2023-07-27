package kr.co.zeppy.crew.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kr.co.zeppy.user.entity.User;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.util.Set;
import java.util.LinkedHashSet;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "CREWS")
@AllArgsConstructor
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crew_id")
    private Long id;

    private String crewName;

    @ManyToMany(mappedBy = "crews")
    private Set<User> users = new LinkedHashSet<>();

    public Crew(String crewName) {
        this.crewName = crewName;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }
}
