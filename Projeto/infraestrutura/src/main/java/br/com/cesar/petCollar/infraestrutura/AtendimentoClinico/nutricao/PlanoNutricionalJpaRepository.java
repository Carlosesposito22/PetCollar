package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoNutricionalJpaRepository extends JpaRepository<PlanoNutricionalJpa, String> {

    /** Rascunho aberto do paciente — no máximo 1 (invariante de fluxo). */
    Optional<PlanoNutricionalJpa> findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
            String pacienteId, String status);

    /** Histórico de planos finalizados de um paciente, mais recente primeiro. */
    List<PlanoNutricionalJpa> findByPacienteIdAndStatusOrderByAtualizadoEmDesc(
            String pacienteId, String status);

    /** Histórico completo (FINALIZADO + SUBSTITUIDO) de um paciente. */
    List<PlanoNutricionalJpa> findByPacienteIdAndStatusInOrderByAtualizadoEmDesc(
            String pacienteId, List<String> status);

    /** Histórico de planos finalizados de todos os pacientes de um tutor. */
    List<PlanoNutricionalJpa> findByTutorIdAndStatusOrderByAtualizadoEmDesc(
            String tutorId, String status);

    /** Histórico completo (FINALIZADO + SUBSTITUIDO) de todos os pacientes do tutor. */
    List<PlanoNutricionalJpa> findByTutorIdAndStatusInOrderByAtualizadoEmDesc(
            String tutorId, List<String> status);

    /** Conta planos (em qualquer status) que prescreveram uma ração específica. */
    long countByRacaoId(String racaoId);
}
