package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProtocoloInacessibilidadeJpaRepository
        extends JpaRepository<ProtocoloInacessibilidadeJpa, String> {

    List<ProtocoloInacessibilidadeJpa> findByAtendimentoIdAndStatusNotIn(
        String atendimentoId, List<String> statusEncerrados);

    List<ProtocoloInacessibilidadeJpa> findByPacienteId(String pacienteId);

    List<ProtocoloInacessibilidadeJpa> findByStatusNotIn(List<String> statusEncerrados);

    List<ProtocoloInacessibilidadeJpa> findByStatus(String status);

    List<ProtocoloInacessibilidadeJpa> findByTutorPrincipalIdAndStatusNotIn(
        String tutorPrincipalId, List<String> statusEncerrados);
}
