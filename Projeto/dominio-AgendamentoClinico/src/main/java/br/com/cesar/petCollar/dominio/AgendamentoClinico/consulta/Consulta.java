package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agregado raiz que representa uma consulta — inicial ou de retorno — e sua máquina
 * de estados ({@link StatusConsulta}). Toda mutação acontece por métodos de negócio
 * com transições guardadas; cada operação registra um {@link EventoAgendamento}
 * (RN 19) e a remarcação preserva histórico auditável (RN 18).
 */
public class Consulta {

    private final ConsultaId id;
    private final PacienteId pacienteId;
    private final TutorId tutorId;
    private final MedicoId medicoId;
    private final EspecialidadeId especialidadeId;
    private final TipoConsulta tipo;
    private final MotivoConsulta motivo;
    private HorarioConsulta horario;
    private StatusConsulta status;
    private final ConsultaId consultaOrigemId;   // somente em retornos (RN 11)
    private int quantidadeRemarcacoes;            // RN 18
    private final LocalDateTime criadaEm;
    private LocalDateTime confirmadaEm;
    private LocalDateTime canceladaEm;
    private final List<HistoricoRemarcacao> historicoRemarcacoes;
    private final List<EventoAgendamento> eventos;

    // Construtor de CRIAÇÃO — consulta inicial (sem vínculo de origem)
    public Consulta(ConsultaId id, PacienteId pacienteId, TutorId tutorId, MedicoId medicoId,
                    EspecialidadeId especialidadeId, MotivoConsulta motivo, HorarioConsulta horario) {
        this(id, pacienteId, tutorId, medicoId, especialidadeId, motivo, horario,
                TipoConsulta.INICIAL, null);
    }

    // Construtor de CRIAÇÃO — consulta de retorno (exige consulta de origem — RN 11)
    public Consulta(ConsultaId id, PacienteId pacienteId, TutorId tutorId, MedicoId medicoId,
                    EspecialidadeId especialidadeId, MotivoConsulta motivo, HorarioConsulta horario,
                    ConsultaId consultaOrigemId) {
        this(id, pacienteId, tutorId, medicoId, especialidadeId, motivo, horario,
                TipoConsulta.RETORNO, exigirOrigem(consultaOrigemId));
    }

    private Consulta(ConsultaId id, PacienteId pacienteId, TutorId tutorId, MedicoId medicoId,
                     EspecialidadeId especialidadeId, MotivoConsulta motivo, HorarioConsulta horario,
                     TipoConsulta tipo, ConsultaId consultaOrigemId) {
        if (id == null)
            throw new IllegalArgumentException("Id da consulta não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (tutorId == null)
            throw new IllegalArgumentException("Id do tutor não pode ser nulo.");
        if (medicoId == null)
            throw new IllegalArgumentException("Id do médico não pode ser nulo.");
        if (especialidadeId == null)
            throw new IllegalArgumentException("Id da especialidade não pode ser nulo.");
        if (motivo == null)
            throw new IllegalArgumentException("Motivo da consulta é obrigatório.");
        if (horario == null)
            throw new IllegalArgumentException("Horário da consulta não pode ser nulo.");
        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoId = medicoId;
        this.especialidadeId = especialidadeId;
        this.tipo = tipo;
        this.motivo = motivo;
        this.horario = horario;
        this.consultaOrigemId = consultaOrigemId;
        this.status = StatusConsulta.AGENDADA;
        this.quantidadeRemarcacoes = 0;
        this.criadaEm = LocalDateTime.now();
        this.historicoRemarcacoes = new ArrayList<>();
        this.eventos = new ArrayList<>();
        registrarEvento(TipoEventoAgendamento.CRIADA, "Consulta " + tipo + " criada.");
    }

    // Construtor de RECONSTRUÇÃO — todos os campos (usado pela infraestrutura)
    public Consulta(ConsultaId id, PacienteId pacienteId, TutorId tutorId, MedicoId medicoId,
                    EspecialidadeId especialidadeId, TipoConsulta tipo, MotivoConsulta motivo,
                    HorarioConsulta horario, StatusConsulta status, ConsultaId consultaOrigemId,
                    int quantidadeRemarcacoes, LocalDateTime criadaEm, LocalDateTime confirmadaEm,
                    LocalDateTime canceladaEm, List<HistoricoRemarcacao> historicoRemarcacoes,
                    List<EventoAgendamento> eventos) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoId = medicoId;
        this.especialidadeId = especialidadeId;
        this.tipo = tipo;
        this.motivo = motivo;
        this.horario = horario;
        this.status = status;
        this.consultaOrigemId = consultaOrigemId;
        this.quantidadeRemarcacoes = quantidadeRemarcacoes;
        this.criadaEm = criadaEm;
        this.confirmadaEm = confirmadaEm;
        this.canceladaEm = canceladaEm;
        this.historicoRemarcacoes = new ArrayList<>(historicoRemarcacoes);
        this.eventos = new ArrayList<>(eventos);
    }

    private static ConsultaId exigirOrigem(ConsultaId consultaOrigemId) {
        if (consultaOrigemId == null)
            throw new IllegalArgumentException(
                "Consulta de retorno exige vínculo com a consulta de origem.");
        return consultaOrigemId;
    }

    // ── Transições de estado ──────────────────────────────────────────────────

    public void confirmar() {
        if (this.status != StatusConsulta.AGENDADA)
            throw new IllegalStateException("Só é possível confirmar consultas com status AGENDADA.");
        this.status = StatusConsulta.CONFIRMADA;
        this.confirmadaEm = LocalDateTime.now();
        registrarEvento(TipoEventoAgendamento.CONFIRMADA, "Consulta confirmada.");
    }

    public void remarcar(HorarioConsulta novoHorario) {
        if (novoHorario == null)
            throw new IllegalArgumentException("Novo horário não pode ser nulo.");
        if (this.status != StatusConsulta.AGENDADA && this.status != StatusConsulta.CONFIRMADA)
            throw new IllegalStateException(
                "Só é possível remarcar consultas com status AGENDADA ou CONFIRMADA.");
        this.historicoRemarcacoes.add(
            new HistoricoRemarcacao(this.horario, novoHorario, LocalDateTime.now()));
        this.horario = novoHorario;
        this.quantidadeRemarcacoes++;
        this.status = StatusConsulta.AGENDADA;   // requer nova confirmação
        this.confirmadaEm = null;
        registrarEvento(TipoEventoAgendamento.REMARCADA,
            "Consulta remarcada (remarcação nº " + quantidadeRemarcacoes + ").");
    }

    public void cancelar() {
        if (this.status == StatusConsulta.CANCELADA)
            throw new IllegalStateException("A consulta já está cancelada.");
        if (this.status == StatusConsulta.REALIZADA)
            throw new IllegalStateException("Não é possível cancelar uma consulta já realizada.");
        this.status = StatusConsulta.CANCELADA;
        this.canceladaEm = LocalDateTime.now();
        registrarEvento(TipoEventoAgendamento.CANCELADA, "Consulta cancelada.");
    }

    public void marcarComoRealizada() {
        if (this.status != StatusConsulta.CONFIRMADA)
            throw new IllegalStateException(
                "Só é possível concluir consultas com status CONFIRMADA.");
        this.status = StatusConsulta.REALIZADA;
        registrarEvento(TipoEventoAgendamento.REALIZADA, "Consulta realizada.");
    }

    /** Marca a consulta realizada como elegível a retorno sem exames pendentes (RN 7). */
    public void aguardarRetorno() {
        if (this.status != StatusConsulta.REALIZADA)
            throw new IllegalStateException(
                "Só consultas REALIZADAS podem aguardar retorno.");
        this.status = StatusConsulta.AGUARDANDO_RETORNO;
        registrarEvento(TipoEventoAgendamento.AGUARDANDO_RETORNO, "Consulta aguardando retorno.");
    }

    /** Marca a consulta realizada como elegível a retorno com exames a confirmar (RN 7/8). */
    public void solicitarExames() {
        if (this.status != StatusConsulta.REALIZADA)
            throw new IllegalStateException(
                "Só consultas REALIZADAS podem ter exames solicitados.");
        this.status = StatusConsulta.EXAMES_SOLICITADOS;
        registrarEvento(TipoEventoAgendamento.EXAMES_SOLICITADOS, "Exames solicitados para retorno.");
    }

    /** Marca a consulta como com retorno já agendado, bloqueando novo agendamento (RN 7). */
    public void marcarRetornoAgendado() {
        if (this.status != StatusConsulta.AGUARDANDO_RETORNO
                && this.status != StatusConsulta.EXAMES_SOLICITADOS)
            throw new IllegalStateException(
                "Só consultas AGUARDANDO_RETORNO ou EXAMES_SOLICITADOS podem ter retorno agendado.");
        this.status = StatusConsulta.RETORNO_AGENDADO;
        registrarEvento(TipoEventoAgendamento.RETORNO_AGENDADO, "Retorno agendado pelo tutor.");
    }

    public boolean isElegivelParaRetorno() {
        return this.status == StatusConsulta.AGUARDANDO_RETORNO
            || this.status == StatusConsulta.EXAMES_SOLICITADOS;
    }

    private void registrarEvento(TipoEventoAgendamento tipo, String detalhe) {
        this.eventos.add(new EventoAgendamento(tipo, LocalDateTime.now(), detalhe));
    }

    // ── Acessores ─────────────────────────────────────────────────────────────

    public ConsultaId getId()                    { return id; }
    public PacienteId getPacienteId()            { return pacienteId; }
    public TutorId getTutorId()                  { return tutorId; }
    public MedicoId getMedicoId()                { return medicoId; }
    public EspecialidadeId getEspecialidadeId()  { return especialidadeId; }
    public TipoConsulta getTipo()                { return tipo; }
    public MotivoConsulta getMotivo()            { return motivo; }
    public HorarioConsulta getHorario()          { return horario; }
    public StatusConsulta getStatus()            { return status; }
    public ConsultaId getConsultaOrigemId()      { return consultaOrigemId; }
    public int getQuantidadeRemarcacoes()        { return quantidadeRemarcacoes; }
    public LocalDateTime getCriadaEm()           { return criadaEm; }
    public LocalDateTime getConfirmadaEm()       { return confirmadaEm; }
    public LocalDateTime getCanceladaEm()        { return canceladaEm; }

    public List<HistoricoRemarcacao> getHistoricoRemarcacoes() {
        return Collections.unmodifiableList(historicoRemarcacoes);
    }

    public List<EventoAgendamento> getEventos() {
        return Collections.unmodifiableList(eventos);
    }
}
