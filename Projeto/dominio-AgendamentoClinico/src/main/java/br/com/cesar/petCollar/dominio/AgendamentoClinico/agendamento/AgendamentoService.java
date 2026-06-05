package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

/**
 * Template Method (GoF, pág. 325) — define o esqueleto do algoritmo de agendamento
 * de consultas.
 *
 * <p>Os passos <b>invariantes</b> (validação de prontuário ativo, disponibilidade
 * de agenda, conflito de horário, confirmação e notificações) são implementados
 * aqui e não podem ser sobrescritos.
 *
 * <p>Os passos <b>variantes</b> (pré-condições específicas, validações extras e a
 * criação da {@link Consulta}) são delegados às subclasses via métodos abstratos
 * ({@code protected abstract}), realizando a variação por <i>herança</i> — não por
 * interface. O wiring continua a cargo do {@code @Configuration} (sem anotações
 * Spring nesta camada).
 *
 * <p>Subclasses concretas:
 * @see AgendamentoConsultaInicialService
 * @see AgendamentoRetornoService
 */
public abstract class AgendamentoService {

    // ── Dependências compartilhadas (injetadas por construtor) ────────────────
    private final IConsultaProntuario prontuario;
    private final IConsultaRepositorio consultaRepositorio;
    private final DisponibilidadeAgendaService disponibilidadeAgenda;
    private final IServicoNotificacao servicoNotificacao;

    protected AgendamentoService(
            IConsultaProntuario prontuario,
            IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidadeAgenda,
            IServicoNotificacao servicoNotificacao) {
        if (prontuario == null)
            throw new IllegalArgumentException("Porta de prontuário não pode ser nula.");
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consulta não pode ser nulo.");
        if (disponibilidadeAgenda == null)
            throw new IllegalArgumentException("Serviço de disponibilidade não pode ser nulo.");
        if (servicoNotificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.prontuario = prontuario;
        this.consultaRepositorio = consultaRepositorio;
        this.disponibilidadeAgenda = disponibilidadeAgenda;
        this.servicoNotificacao = servicoNotificacao;
    }

    // ── MÉTODO TEMPLATE (final — o esqueleto não pode ser sobrescrito) ────────

    /**
     * Executa o algoritmo completo de agendamento. Define a ordem dos passos e
     * <b>não</b> pode ser sobrescrito.
     *
     * @param requisicao dados de entrada do agendamento
     * @return a {@link Consulta} criada, confirmada e persistida
     */
    public final Consulta agendar(RequisicaoAgendamento requisicao) {
        if (requisicao == null)
            throw new IllegalArgumentException("Requisição de agendamento não pode ser nula.");

        // Passo 1 — invariante: prontuário ativo (RN 1)
        validarProntuarioAtivo(requisicao.getPacienteId());

        // Passo 2 — variante: pré-condições específicas do tipo de consulta
        validarPreCondicoesEspecificas(requisicao);

        // Passo 3 — invariante: disponibilidade na agenda do médico (RN 4)
        validarDisponibilidadeNaAgenda(requisicao.getMedicoId(), requisicao.getHorario());

        // Passo 4 — invariante: conflito de horário no paciente (RN 5)
        validarAusenciaDeConflito(requisicao.getPacienteId(), requisicao.getHorario());

        // Passo 5 — variante: validações extras do tipo (ex.: exames no retorno)
        executarValidacoesExtras(requisicao);

        // Passo 6 — variante: criação da Consulta + confirmação e persistência (invariantes)
        Consulta consulta = criarConsulta(requisicao);
        consulta.confirmar();                         // RN 6 — confirma o agendamento
        consultaRepositorio.salvar(consulta);

        // Passo 7 — invariante: notificar médico (RN 6 e RN 13)
        notificarMedico(consulta);

        // Passo 8 — invariante: notificar Tutor (RN 14)
        notificarTutor(consulta);

        return consulta;
    }

    // ── PASSOS INVARIANTES (private — subclasses não acessam diretamente) ─────

    private void validarProntuarioAtivo(PacienteId pacienteId) {
        if (prontuario.obterStatus(pacienteId) != StatusProntuario.ATIVO)
            throw new IllegalStateException(
                    "O agendamento só é permitido para pacientes com prontuário ativo.");
    }

    private void validarDisponibilidadeNaAgenda(MedicoId medicoId, HorarioConsulta horario) {
        if (!disponibilidadeAgenda.estaDisponivel(medicoId, horario))
            throw new IllegalStateException(
                    "O médico não possui disponibilidade no horário solicitado.");
    }

    private void validarAusenciaDeConflito(PacienteId pacienteId, HorarioConsulta horario) {
        if (consultaRepositorio.existeConflitoNoPaciente(pacienteId, horario))
            throw new IllegalStateException(
                    "O paciente já possui consulta agendada neste horário.");
    }

    private void notificarMedico(Consulta consulta) {
        String mensagem = "Consulta " + consulta.getTipo() + " confirmada para "
                + consulta.getHorario()
                + (consulta.getConsultaOrigemId() != null
                    ? " (retorno da consulta de origem " + consulta.getConsultaOrigemId().getValor()
                      + ", com exames vinculados)"
                    : "")
                + ".";
        servicoNotificacao.notificarMedico(consulta.getMedicoId(), mensagem);
    }

    private void notificarTutor(Consulta consulta) {
        servicoNotificacao.notificarTutor(consulta.getTutorId(),
                "Sua consulta " + consulta.getTipo() + " foi confirmada para "
                + consulta.getHorario() + ".");
    }

    // ── PASSOS VARIANTES (protected abstract — subclasses DEVEM implementar) ──

    /**
     * Valida pré-condições específicas do tipo de consulta.
     * Inicial: motivo preenchido (RN 3). Retorno: consulta de origem elegível (RN 7).
     */
    protected abstract void validarPreCondicoesEspecificas(RequisicaoAgendamento requisicao);

    /**
     * Validações extras após a verificação de agenda e conflito.
     * Inicial: nenhuma (gancho opcional vazio). Retorno: ao menos um exame concluído (RN 10).
     */
    protected abstract void executarValidacoesExtras(RequisicaoAgendamento requisicao);

    /**
     * Cria a instância de {@link Consulta} específica do tipo (inicial ou retorno),
     * aplicando o construtor adequado do agregado.
     */
    protected abstract Consulta criarConsulta(RequisicaoAgendamento requisicao);
}
