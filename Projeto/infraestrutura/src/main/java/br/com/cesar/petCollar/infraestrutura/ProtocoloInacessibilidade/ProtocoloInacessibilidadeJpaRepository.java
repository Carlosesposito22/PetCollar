package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data do agregado {@link ProtocoloInacessibilidadeJpa}. Os
 * status considerados "encerrados" são passados como parâmetro pelos finders para
 * isolar os protocolos ainda em andamento.
 */
public interface ProtocoloInacessibilidadeJpaRepository
        extends JpaRepository<ProtocoloInacessibilidadeJpa, String> {

    List<ProtocoloInacessibilidadeJpa> findByAtendimentoIdAndStatusNotIn(
        String atendimentoId, List<String> statusEncerrados);

    List<ProtocoloInacessibilidadeJpa> findByPacienteId(String pacienteId);

    List<ProtocoloInacessibilidadeJpa> findByStatusNotIn(List<String> statusEncerrados);

    List<ProtocoloInacessibilidadeJpa> findByStatus(String status);
}
