package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAuditoriaJpaRepository extends JpaRepository<EventoAuditoriaJpa, String> {
    List<EventoAuditoriaJpa> findByTutorIdOrderByTimestampDesc(String tutorId);
    List<EventoAuditoriaJpa> findByIndicacaoIdOrderByTimestampDesc(String indicacaoId);
}
