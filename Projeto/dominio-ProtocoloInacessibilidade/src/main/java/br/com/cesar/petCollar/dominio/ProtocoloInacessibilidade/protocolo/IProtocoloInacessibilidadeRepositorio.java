package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.util.List;
import java.util.Optional;

public interface IProtocoloInacessibilidadeRepositorio {

    void salvar(ProtocoloInacessibilidade protocolo);

    Optional<ProtocoloInacessibilidade> buscarPorId(ProtocoloId id);

    Optional<ProtocoloInacessibilidade> buscarAtivoPorAtendimento(AtendimentoId atendimentoId);

    Optional<ProtocoloInacessibilidade> buscarAtivoPorTutor(TutorId tutorId);

    List<ProtocoloInacessibilidade> listarPorPaciente(PacienteId pacienteId);

    List<ProtocoloInacessibilidade> listarAtivos();

    List<ProtocoloInacessibilidade> listarPorStatus(StatusProtocolo status);
}
