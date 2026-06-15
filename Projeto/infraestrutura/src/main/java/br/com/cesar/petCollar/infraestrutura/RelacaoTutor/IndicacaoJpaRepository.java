package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndicacaoJpaRepository extends JpaRepository<IndicacaoJpa, String> {
    boolean existsByCpfIndicadoAndStatus(String cpfIndicado, String status);
    List<IndicacaoJpa> findByTutorIndicadorId(String tutorIndicadorId);
    java.util.Optional<IndicacaoJpa> findTopByCpfIndicadoAndStatus(String cpfIndicado, String status);
}
