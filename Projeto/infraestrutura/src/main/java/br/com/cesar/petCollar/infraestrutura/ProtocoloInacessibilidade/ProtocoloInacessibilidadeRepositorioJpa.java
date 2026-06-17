package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ProtocoloInacessibilidadeRepositorioJpa implements IProtocoloInacessibilidadeRepositorio {

    private static final List<String> STATUS_ENCERRADOS = List.of(
        StatusProtocolo.ENCERRADO_COM_SUCESSO.name(),
        StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO.name(),
        StatusProtocolo.INATIVO.name());

    private final ProtocoloInacessibilidadeJpaRepository jpa;

    public ProtocoloInacessibilidadeRepositorioJpa(ProtocoloInacessibilidadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void salvar(ProtocoloInacessibilidade protocolo) {
        jpa.save(ProtocoloInacessibilidadeJpa.fromDomain(protocolo));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProtocoloInacessibilidade> buscarPorId(ProtocoloId id) {
        return jpa.findById(id.getValor()).map(ProtocoloInacessibilidadeJpa::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProtocoloInacessibilidade> buscarAtivoPorAtendimento(AtendimentoId atendimentoId) {
        return jpa.findByAtendimentoIdAndStatusNotIn(atendimentoId.getValor(), STATUS_ENCERRADOS).stream()
            .map(ProtocoloInacessibilidadeJpa::toDomain)
            .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProtocoloInacessibilidade> listarPorPaciente(PacienteId pacienteId) {
        return jpa.findByPacienteId(pacienteId.getValor()).stream()
            .map(ProtocoloInacessibilidadeJpa::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProtocoloInacessibilidade> listarAtivos() {
        return jpa.findByStatusNotIn(STATUS_ENCERRADOS).stream()
            .map(ProtocoloInacessibilidadeJpa::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProtocoloInacessibilidade> listarPorStatus(StatusProtocolo status) {
        return jpa.findByStatus(status.name()).stream()
            .map(ProtocoloInacessibilidadeJpa::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProtocoloInacessibilidade> buscarAtivoPorTutor(TutorId tutorId) {
        return jpa.findByTutorPrincipalIdAndStatusNotIn(tutorId.getValor(), STATUS_ENCERRADOS)
            .stream()
            .map(ProtocoloInacessibilidadeJpa::toDomain)
            .findFirst();
    }
}
