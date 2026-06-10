package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkIndicacaoJpaRepository extends JpaRepository<LinkIndicacaoJpa, String> {
    Optional<LinkIndicacaoJpa> findByTutorId(String tutorId);
    Optional<LinkIndicacaoJpa> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
