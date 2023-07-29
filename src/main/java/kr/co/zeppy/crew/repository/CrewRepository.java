package kr.co.zeppy.crew.repository;

import java.util.Optional;
import kr.co.zeppy.crew.entity.Crew;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findById(Long id);

}
