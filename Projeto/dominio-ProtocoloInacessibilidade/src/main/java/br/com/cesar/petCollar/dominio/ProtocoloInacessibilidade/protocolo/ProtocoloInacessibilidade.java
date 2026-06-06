package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agregado raiz da F-03 — Protocolo automatizado de tutor inacessível. Representa
 * uma execução do protocolo de contingência vinculada a um atendimento clínico,
 * com tentativas de contato (subentidade {@link TentativaContato}, RN 3) e eventos
 * de escalonamento (subentidade {@link EventoEscalonamento}, RN 7) auditáveis.
 *
 * <p><b>Continuidade do atendimento (RN 8):</b> este agregado guarda apenas o
 * {@code String}/{@link AtendimentoId} de referência ao atendimento clínico e
 * <b>nunca</b> altera o estado desse atendimento — o protocolo de contato roda em
 * paralelo, sem interromper a assistência. Não há qualquer método aqui capaz de
 * mutar o {@code Atendimento}; a continuidade é, portanto, garantida
 * arquiteturalmente.
 *
 * <p><b>Máquina de estados ({@link StatusProtocolo}):</b>
 * <pre>
 *   INATIVO ──ativar()──▶ ATIVADO ──iniciarTentativasTutor()──▶ EM_TENTATIVA_TUTOR
 *   EM_TENTATIVA_TUTOR ──iniciarAcionamentoSecundarios()──▶ EM_TENTATIVA_SECUNDARIOS
 *   EM_TENTATIVA_SECUNDARIOS (todos acionados) ──escalonar()──▶ EM_ESCALONAMENTO
 *   EM_ESCALONAMENTO ──escalonar()──▶ EM_ESCALONAMENTO (avança nível)
 *   EM_ESCALONAMENTO ──encerrarPorEsgotamento()──▶ ENCERRADO_POR_ESGOTAMENTO
 *   {qualquer estado ativo} ──encerrarComSucesso()──▶ ENCERRADO_COM_SUCESSO
 * </pre>
 * Toda transição é guardada com {@link IllegalStateException}. Em especial, só é
 * possível escalonar depois de acionados todos os responsáveis secundários
 * (RN 5).
 */
public class ProtocoloInacessibilidade {

    private final ProtocoloId id;
    private final AtendimentoId atendimentoId;
    private final PacienteId pacienteId;
    private final TutorId tutorPrincipalId;
    private final ConfiguracaoProtocoloId configuracaoId;
    private StatusProtocolo status;
    private NivelEscalonamento nivelEscalonamentoAtual;
    private boolean secundariosAcionados;
    private final List<TentativaContato> tentativas;
    private final List<EventoEscalonamento> eventosEscalonamento;
    private LocalDateTime ativadoEm;
    private LocalDateTime encerradoEm;
    private MotivoEncerramento motivoEncerramento;

    // Construtor de CRIAÇÃO — protocolo nasce INATIVO, à espera de ativação (RN 1).
    public ProtocoloInacessibilidade(ProtocoloId id, AtendimentoId atendimentoId,
                                     PacienteId pacienteId, TutorId tutorPrincipalId,
                                     ConfiguracaoProtocoloId configuracaoId) {
        if (id == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (tutorPrincipalId == null)
            throw new IllegalArgumentException("Id do tutor principal não pode ser nulo.");
        if (configuracaoId == null)
            throw new IllegalArgumentException("Id da configuração não pode ser nulo.");
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.tutorPrincipalId = tutorPrincipalId;
        this.configuracaoId = configuracaoId;
        this.status = StatusProtocolo.INATIVO;
        this.secundariosAcionados = false;
        this.tentativas = new ArrayList<>();
        this.eventosEscalonamento = new ArrayList<>();
    }

    // Construtor de RECONSTRUÇÃO — todos os campos (usado pela infraestrutura).
    public ProtocoloInacessibilidade(ProtocoloId id, AtendimentoId atendimentoId,
                                     PacienteId pacienteId, TutorId tutorPrincipalId,
                                     ConfiguracaoProtocoloId configuracaoId, StatusProtocolo status,
                                     NivelEscalonamento nivelEscalonamentoAtual,
                                     boolean secundariosAcionados, List<TentativaContato> tentativas,
                                     List<EventoEscalonamento> eventosEscalonamento,
                                     LocalDateTime ativadoEm, LocalDateTime encerradoEm,
                                     MotivoEncerramento motivoEncerramento) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.tutorPrincipalId = tutorPrincipalId;
        this.configuracaoId = configuracaoId;
        this.status = status;
        this.nivelEscalonamentoAtual = nivelEscalonamentoAtual;
        this.secundariosAcionados = secundariosAcionados;
        this.tentativas = new ArrayList<>(tentativas);
        this.eventosEscalonamento = new ArrayList<>(eventosEscalonamento);
        this.ativadoEm = ativadoEm;
        this.encerradoEm = encerradoEm;
        this.motivoEncerramento = motivoEncerramento;
    }

    // ── Transições de estado ──────────────────────────────────────────────────

    /** Ativa o protocolo por timeout do tutor (RN 1). */
    public void ativar() {
        if (this.status != StatusProtocolo.INATIVO)
            throw new IllegalStateException("Só é possível ativar protocolos com status INATIVO.");
        this.status = StatusProtocolo.ATIVADO;
        this.ativadoEm = LocalDateTime.now();
    }

    /** Inicia a rodada de tentativas com o tutor principal (RN 2). */
    public void iniciarTentativasTutor() {
        if (this.status != StatusProtocolo.ATIVADO)
            throw new IllegalStateException(
                "Só é possível iniciar as tentativas com o tutor a partir do status ATIVADO.");
        this.status = StatusProtocolo.EM_TENTATIVA_TUTOR;
    }

    /** Registra de forma auditável uma tentativa de contato já executada (RN 3). */
    public void registrarTentativa(TentativaContato tentativa) {
        if (tentativa == null)
            throw new IllegalArgumentException("Tentativa não pode ser nula.");
        if (this.status != StatusProtocolo.EM_TENTATIVA_TUTOR
                && this.status != StatusProtocolo.EM_TENTATIVA_SECUNDARIOS)
            throw new IllegalStateException(
                "Só é possível registrar tentativas durante o contato com tutor ou secundários.");
        this.tentativas.add(tentativa);
    }

    /** Passa a acionar os responsáveis secundários, esgotado o contato com o tutor (RN 4). */
    public void iniciarAcionamentoSecundarios() {
        if (this.status != StatusProtocolo.EM_TENTATIVA_TUTOR)
            throw new IllegalStateException(
                "Só é possível acionar responsáveis secundários após as tentativas com o tutor.");
        this.status = StatusProtocolo.EM_TENTATIVA_SECUNDARIOS;
    }

    /** Marca que todos os responsáveis secundários cadastrados já foram acionados (RN 5). */
    public void marcarTodosSecundariosAcionados() {
        if (this.status != StatusProtocolo.EM_TENTATIVA_SECUNDARIOS)
            throw new IllegalStateException(
                "Só é possível concluir o acionamento de secundários durante essa etapa.");
        this.secundariosAcionados = true;
    }

    /**
     * Avança para o próximo nível de escalonamento, registrando o evento auditável
     * (RN 6/7). Exige que todos os responsáveis secundários já tenham sido acionados
     * antes de qualquer escalonamento (RN 5).
     */
    public void escalonar(NivelEscalonamento nivel, String motivo, String responsavelAcionadoId) {
        if (nivel == null)
            throw new IllegalArgumentException("Nível de escalonamento não pode ser nulo.");
        boolean podeIniciar = this.status == StatusProtocolo.EM_TENTATIVA_SECUNDARIOS
                && this.secundariosAcionados;
        boolean jaEscalonando = this.status == StatusProtocolo.EM_ESCALONAMENTO;
        if (!podeIniciar && !jaEscalonando)
            throw new IllegalStateException(
                "Só é possível escalonar após acionar todos os responsáveis secundários (RN 5).");
        if (this.nivelEscalonamentoAtual != null
                && nivel.ordem() <= this.nivelEscalonamentoAtual.ordem())
            throw new IllegalStateException(
                "O escalonamento deve avançar para um nível de prioridade superior ao atual.");
        this.status = StatusProtocolo.EM_ESCALONAMENTO;
        this.nivelEscalonamentoAtual = nivel;
        this.eventosEscalonamento.add(new EventoEscalonamento(
            EventoEscalonamentoId.gerar(), nivel, motivo, responsavelAcionadoId, LocalDateTime.now()));
    }

    /** Encerra o protocolo porque alguém respondeu ao contato (RN). */
    public void encerrarComSucesso(MotivoEncerramento motivo) {
        if (motivo == null)
            throw new IllegalArgumentException("Motivo de encerramento não pode ser nulo.");
        if (isEncerrado())
            throw new IllegalStateException("O protocolo já está encerrado.");
        if (this.status == StatusProtocolo.INATIVO)
            throw new IllegalStateException("Não é possível encerrar um protocolo que não foi ativado.");
        this.status = StatusProtocolo.ENCERRADO_COM_SUCESSO;
        this.encerradoEm = LocalDateTime.now();
        this.motivoEncerramento = motivo;
    }

    /** Encerra o protocolo por esgotamento de todos os níveis de escalonamento. */
    public void encerrarPorEsgotamento() {
        if (this.status != StatusProtocolo.EM_ESCALONAMENTO)
            throw new IllegalStateException(
                "Só é possível encerrar por esgotamento durante o escalonamento.");
        this.status = StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO;
        this.encerradoEm = LocalDateTime.now();
        this.motivoEncerramento = MotivoEncerramento.esgotamento(
            "Todos os níveis de escalonamento foram acionados sem resposta.");
    }

    // ── Consultas de regra ──────────────────────────────────────────────────────

    public boolean isAtivo() {
        return status != StatusProtocolo.INATIVO && !isEncerrado();
    }

    public boolean isEncerrado() {
        return status == StatusProtocolo.ENCERRADO_COM_SUCESSO
            || status == StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO;
    }

    public boolean todosResponsaveisSecundariosAcionados() {
        return secundariosAcionados;
    }

    /** Conta as tentativas já executadas para um destinatário, por canal (RN 2). */
    public long contarTentativas(TipoDestinatario tipo, CanalContato canal) {
        return tentativas.stream()
            .filter(t -> t.getTipoDestinatario() == tipo && t.getCanal() == canal)
            .count();
    }

    public boolean tutorRespondeu() {
        return tentativas.stream()
            .anyMatch(t -> t.getTipoDestinatario() == TipoDestinatario.TUTOR_PRINCIPAL && t.houveSucesso());
    }

    public boolean algumSecundarioRespondeu() {
        return tentativas.stream()
            .anyMatch(t -> t.getTipoDestinatario() == TipoDestinatario.RESPONSAVEL_SECUNDARIO && t.houveSucesso());
    }

    // ── Acessores ───────────────────────────────────────────────────────────────

    public ProtocoloId getId()                          { return id; }
    public AtendimentoId getAtendimentoId()             { return atendimentoId; }
    public PacienteId getPacienteId()                   { return pacienteId; }
    public TutorId getTutorPrincipalId()                { return tutorPrincipalId; }
    public ConfiguracaoProtocoloId getConfiguracaoId()  { return configuracaoId; }
    public StatusProtocolo getStatus()                  { return status; }
    public NivelEscalonamento getNivelEscalonamentoAtual() { return nivelEscalonamentoAtual; }
    public LocalDateTime getAtivadoEm()                 { return ativadoEm; }
    public LocalDateTime getEncerradoEm()               { return encerradoEm; }
    public MotivoEncerramento getMotivoEncerramento()   { return motivoEncerramento; }

    public List<TentativaContato> getTentativas() {
        return Collections.unmodifiableList(tentativas);
    }

    public List<EventoEscalonamento> getEventosEscalonamento() {
        return Collections.unmodifiableList(eventosEscalonamento);
    }
}
