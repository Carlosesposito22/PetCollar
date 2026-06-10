package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.StatusIndicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndicacaoJpaRepository extends JpaRepository<IndicacaoJpa, String> {
    boolean existsByCpfIndicadoAndStatus(String cpfIndicado, StatusIndicacao status);
    List<IndicacaoJpa> findByTutorIndicadorId(String tutorIndicadorId);
}
