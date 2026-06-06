package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;
import java.util.Optional;

/**
 * Repositório (porta) do agregado {@link ProtocoloInacessibilidade}, definido no
 * domínio. As subentidades (tentativas e eventos de escalonamento) são persistidas
 * em conjunto com o agregado.
 */
public interface IProtocoloInacessibilidadeRepositorio {

    void salvar(ProtocoloInacessibilidade protocolo);

    Optional<ProtocoloInacessibilidade> buscarPorId(ProtocoloId id);

    /** Protocolo ainda em andamento para o atendimento, se houver (idempotência da RN 1). */
    Optional<ProtocoloInacessibilidade> buscarAtivoPorAtendimento(AtendimentoId atendimentoId);

    List<ProtocoloInacessibilidade> listarPorPaciente(PacienteId pacienteId);

    List<ProtocoloInacessibilidade> listarAtivos();

    List<ProtocoloInacessibilidade> listarPorStatus(StatusProtocolo status);
}
