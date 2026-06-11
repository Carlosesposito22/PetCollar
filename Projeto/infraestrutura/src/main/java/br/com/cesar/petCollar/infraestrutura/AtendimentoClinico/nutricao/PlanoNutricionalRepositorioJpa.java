package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.StatusPlanoNutricional;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Adapter JPA da interface de domínio {@link IPlanoNutricionalRepositorio}.
 * Traduz {@link PlanoNutricional} ↔ {@link PlanoNutricionalJpa} via
 * {@code fromDomain}/{@code toDomain}.
 */
@Repository
public class PlanoNutricionalRepositorioJpa implements IPlanoNutricionalRepositorio {

    private final PlanoNutricionalJpaRepository jpa;

    public PlanoNutricionalRepositorioJpa(PlanoNutricionalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(PlanoNutricional plano) {
        jpa.save(PlanoNutricionalJpa.fromDomain(plano));
    }

    @Override
    public Optional<PlanoNutricional> buscarPorId(PlanoNutricionalId id) {
        return jpa.findById(id.getValor()).map(PlanoNutricionalJpa::toDomain);
    }

    @Override
    public Optional<PlanoNutricional> buscarRascunhoDoPaciente(PacienteId pacienteId) {
        return jpa.findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
                pacienteId.getValor(), StatusPlanoNutricional.RASCUNHO.name())
                .map(PlanoNutricionalJpa::toDomain);
    }

    @Override
    public Optional<PlanoNutricional> buscarFinalizadoAtivoDoPaciente(PacienteId pacienteId) {
        return jpa.findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
                pacienteId.getValor(), StatusPlanoNutricional.FINALIZADO.name())
                .map(PlanoNutricionalJpa::toDomain);
    }

    @Override
    public List<PlanoNutricional> listarFinalizadosDoPaciente(PacienteId pacienteId) {
        // Histórico completo do médico — inclui SUBSTITUIDO para visualizar
        // a progressão das prescrições ao longo dos atendimentos.
        return jpa.findByPacienteIdAndStatusInOrderByAtualizadoEmDesc(
                pacienteId.getValor(),
                List.of(StatusPlanoNutricional.FINALIZADO.name(),
                        StatusPlanoNutricional.SUBSTITUIDO.name()))
                .stream().map(PlanoNutricionalJpa::toDomain).toList();
    }

    @Override
    public List<PlanoNutricional> listarAtivosDoTutor(TutorId tutorId) {
        return jpa.findByTutorIdAndStatusOrderByAtualizadoEmDesc(
                tutorId.getValor(), StatusPlanoNutricional.FINALIZADO.name())
                .stream().map(PlanoNutricionalJpa::toDomain).toList();
    }

    @Override
    public List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId) {
        return jpa.findByTutorIdAndStatusInOrderByAtualizadoEmDesc(
                tutorId.getValor(),
                List.of(StatusPlanoNutricional.FINALIZADO.name(),
                        StatusPlanoNutricional.SUBSTITUIDO.name()))
                .stream().map(PlanoNutricionalJpa::toDomain).toList();
    }
}
