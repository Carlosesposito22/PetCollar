package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ProtocoloRepositorioFake implements IProtocoloInacessibilidadeRepositorio {

    private final Map<String, ProtocoloInacessibilidade> dados = new LinkedHashMap<>();

    @Override
    public void salvar(ProtocoloInacessibilidade protocolo) {
        dados.put(protocolo.getId().getValor(), protocolo);
    }

    @Override
    public Optional<ProtocoloInacessibilidade> buscarPorId(ProtocoloId id) {
        return Optional.ofNullable(dados.get(id.getValor()));
    }

    @Override
    public Optional<ProtocoloInacessibilidade> buscarAtivoPorAtendimento(AtendimentoId atendimentoId) {
        return dados.values().stream()
            .filter(p -> p.getAtendimentoId().equals(atendimentoId))
            .filter(ProtocoloInacessibilidade::isAtivo)
            .findFirst();
    }

    @Override
    public Optional<ProtocoloInacessibilidade> buscarAtivoPorTutor(TutorId tutorId) {
        return dados.values().stream()
            .filter(p -> p.getTutorPrincipalId().equals(tutorId))
            .filter(ProtocoloInacessibilidade::isAtivo)
            .findFirst();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarPorPaciente(PacienteId pacienteId) {
        return dados.values().stream()
            .filter(p -> p.getPacienteId().equals(pacienteId))
            .toList();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarAtivos() {
        return dados.values().stream()
            .filter(ProtocoloInacessibilidade::isAtivo)
            .toList();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarPorStatus(StatusProtocolo status) {
        return dados.values().stream()
            .filter(p -> p.getStatus() == status)
            .toList();
    }
}
