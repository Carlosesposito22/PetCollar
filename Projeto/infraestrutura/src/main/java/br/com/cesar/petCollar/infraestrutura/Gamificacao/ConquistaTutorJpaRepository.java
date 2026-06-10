package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConquistaTutorJpaRepository extends JpaRepository<ConquistaTutorJpa, String> {
    List<ConquistaTutorJpa> findByTutorId(String tutorId);
    boolean existsByTutorIdAndBadgeId(String tutorId, String badgeId);
}
