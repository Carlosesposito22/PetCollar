package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressoBadgeJpaRepository extends JpaRepository<ProgressoBadgeJpa, String> {
    Optional<ProgressoBadgeJpa> findByTutorIdAndBadgeId(String tutorId, String badgeId);
    List<ProgressoBadgeJpa> findByTutorId(String tutorId);
}
