package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementação provisória em memória de {@link IProtocoloInacessibilidadeRepositorio}
 * (stand-in enquanto o banco está desligado — ver application.yml). Substituível
 * pelo adapter JPA {@code ProtocoloInacessibilidadeRepositorioJpa} sem tocar no domínio.
 */
@Repository
public class ProtocoloInacessibilidadeRepositorioEmMemoria implements IProtocoloInacessibilidadeRepositorio {

    private final ConcurrentMap<String, ProtocoloInacessibilidade> protocolos = new ConcurrentHashMap<>();

    @Override
    public void salvar(ProtocoloInacessibilidade protocolo) {
        protocolos.put(protocolo.getId().getValor(), protocolo);
    }

    @Override
    public Optional<ProtocoloInacessibilidade> buscarPorId(ProtocoloId id) {
        return Optional.ofNullable(protocolos.get(id.getValor()));
    }

    @Override
    public Optional<ProtocoloInacessibilidade> buscarAtivoPorAtendimento(AtendimentoId atendimentoId) {
        return protocolos.values().stream()
            .filter(p -> p.getAtendimentoId().equals(atendimentoId))
            .filter(ProtocoloInacessibilidade::isAtivo)
            .findFirst();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarPorPaciente(PacienteId pacienteId) {
        return protocolos.values().stream()
            .filter(p -> p.getPacienteId().equals(pacienteId))
            .toList();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarAtivos() {
        return protocolos.values().stream()
            .filter(ProtocoloInacessibilidade::isAtivo)
            .toList();
    }

    @Override
    public List<ProtocoloInacessibilidade> listarPorStatus(StatusProtocolo status) {
        return protocolos.values().stream()
            .filter(p -> p.getStatus() == status)
            .toList();
    }
}
