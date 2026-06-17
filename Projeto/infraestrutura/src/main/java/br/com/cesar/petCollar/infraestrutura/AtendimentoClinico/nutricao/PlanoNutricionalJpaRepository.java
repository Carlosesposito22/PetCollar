package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoNutricionalJpaRepository extends JpaRepository<PlanoNutricionalJpa, String> {

    Optional<PlanoNutricionalJpa> findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
            String pacienteId, String status);

    List<PlanoNutricionalJpa> findByPacienteIdAndStatusOrderByAtualizadoEmDesc(
            String pacienteId, String status);

    List<PlanoNutricionalJpa> findByPacienteIdAndStatusInOrderByAtualizadoEmDesc(
            String pacienteId, List<String> status);

    List<PlanoNutricionalJpa> findByTutorIdAndStatusOrderByAtualizadoEmDesc(
            String tutorId, String status);

    List<PlanoNutricionalJpa> findByTutorIdAndStatusInOrderByAtualizadoEmDesc(
            String tutorId, List<String> status);

    long countByRacaoId(String racaoId);
}
