package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.FiltroConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA da interface de domínio {@link IConsultaRepositorio}. Traduz domínio
 * ↔ entidade via {@code fromDomain}/{@code toDomain} e aplica os filtros da agenda
 * do tutor (RN 17) sobre o resultado.
 *
 * <p>{@code @Transactional(readOnly = true)} na classe mantém a sessão aberta durante
 * o {@code toDomain()} (que inicializa as coleções LAZY de histórico/eventos), já que
 * {@code open-in-view} está desligado; {@code salvar} sobrescreve para leitura-escrita.
 */
@Repository
@Transactional(readOnly = true)
public class ConsultaRepositorioJpa implements IConsultaRepositorio {

    private static final List<String> STATUS_ELEGIVEIS_RETORNO = List.of(
        StatusConsulta.AGUARDANDO_RETORNO.name(),
        StatusConsulta.EXAMES_SOLICITADOS.name());

    private static final List<String> STATUS_PENDENTES_RETORNO = List.of(
        StatusConsulta.AGUARDANDO_RETORNO.name(),
        StatusConsulta.EXAMES_SOLICITADOS.name(),
        StatusConsulta.RETORNO_AGENDADO.name());

    private final ConsultaJpaRepository jpa;

    public ConsultaRepositorioJpa(ConsultaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void salvar(Consulta consulta) {
        jpa.save(ConsultaJpa.fromDomain(consulta));
    }

    @Override
    public Optional<Consulta> buscarPorId(ConsultaId id) {
        return jpa.findById(id.getValor()).map(ConsultaJpa::toDomain);
    }

    @Override
    public List<Consulta> listarPorPaciente(PacienteId pacienteId, FiltroConsulta filtro) {
        FiltroConsulta criterio = filtro == null ? FiltroConsulta.vazio() : filtro;
        return jpa.findByPacienteId(pacienteId.getValor()).stream()
            .map(ConsultaJpa::toDomain)
            .filter(c -> criterio.correspondeStatus(c.getStatus()))
            .filter(c -> criterio.correspondeTipo(c.getTipo()))
            .filter(c -> criterio.correspondePeriodo(c.getHorario().getInicio()))
            .toList();
    }

    @Override
    public List<Consulta> listarPorMedicoEPeriodo(MedicoId medicoId,
                                                  LocalDateTime inicio, LocalDateTime fim) {
        return jpa.buscarPorMedicoEPeriodo(medicoId.getValor(), inicio, fim).stream()
            .map(ConsultaJpa::toDomain)
            .toList();
    }

    @Override
    public List<Consulta> listarElegiveisRetorno(PacienteId pacienteId) {
        return jpa.findByPacienteIdAndStatusIn(pacienteId.getValor(), STATUS_ELEGIVEIS_RETORNO).stream()
            .map(ConsultaJpa::toDomain)
            .toList();
    }

    @Override
    public List<Consulta> listarPendentesRetornoPorMedico(MedicoId medicoId) {
        return jpa.findByMedicoIdAndStatusIn(medicoId.getValor(), STATUS_PENDENTES_RETORNO).stream()
            .map(ConsultaJpa::toDomain)
            .toList();
    }

    @Override
    public boolean existeConflitoNoPaciente(PacienteId pacienteId, HorarioConsulta horario) {
        return jpa.existeConflito(pacienteId.getValor(), horario.getInicio(), horario.getFim());
    }
}
