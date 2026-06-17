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

    public void ativar() {
        if (this.status != StatusProtocolo.INATIVO)
            throw new IllegalStateException("Só é possível ativar protocolos com status INATIVO.");
        this.status = StatusProtocolo.ATIVADO;
        this.ativadoEm = LocalDateTime.now();
    }

    public void iniciarTentativasTutor() {
        if (this.status != StatusProtocolo.ATIVADO)
            throw new IllegalStateException(
                "Só é possível iniciar as tentativas com o tutor a partir do status ATIVADO.");
        this.status = StatusProtocolo.EM_TENTATIVA_TUTOR;
    }

    public void registrarTentativa(TentativaContato tentativa) {
        if (tentativa == null)
            throw new IllegalArgumentException("Tentativa não pode ser nula.");
        if (this.status != StatusProtocolo.EM_TENTATIVA_TUTOR
                && this.status != StatusProtocolo.EM_TENTATIVA_SECUNDARIOS)
            throw new IllegalStateException(
                "Só é possível registrar tentativas durante o contato com tutor ou secundários.");
        this.tentativas.add(tentativa);
    }

    public void iniciarAcionamentoSecundarios() {
        if (this.status != StatusProtocolo.EM_TENTATIVA_TUTOR)
            throw new IllegalStateException(
                "Só é possível acionar responsáveis secundários após as tentativas com o tutor.");
        this.status = StatusProtocolo.EM_TENTATIVA_SECUNDARIOS;
    }

    public void marcarTodosSecundariosAcionados() {
        if (this.status != StatusProtocolo.EM_TENTATIVA_SECUNDARIOS)
            throw new IllegalStateException(
                "Só é possível concluir o acionamento de secundários durante essa etapa.");
        this.secundariosAcionados = true;
    }

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

    public void encerrarPorEsgotamento() {
        if (this.status != StatusProtocolo.EM_ESCALONAMENTO)
            throw new IllegalStateException(
                "Só é possível encerrar por esgotamento durante o escalonamento.");
        this.status = StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO;
        this.encerradoEm = LocalDateTime.now();
        this.motivoEncerramento = MotivoEncerramento.esgotamento(
            "Todos os níveis de escalonamento foram acionados sem resposta.");
    }

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
