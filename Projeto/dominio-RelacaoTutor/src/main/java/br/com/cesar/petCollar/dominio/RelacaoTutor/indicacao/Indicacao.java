package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

public class Indicacao {

    private final IndicacaoId id;
    private final TutorId tutorIndicadorId;
    private final LinkIndicacaoId linkId;
    private final CPF cpfIndicado;
    private final LocalDateTime timestampClique;
    private StatusIndicacao status;

    private String cobrancaIndicadorId;
    private LocalDateTime convertidaEm;
    private String motivoInvalidacao;

    public Indicacao(IndicacaoId id, TutorId tutorIndicadorId, LinkIndicacaoId linkId,
                     CPF cpfIndicado, LocalDateTime timestampClique) {
        if (id == null)               throw new IllegalArgumentException("Id da indicação não pode ser nulo.");
        if (tutorIndicadorId == null) throw new IllegalArgumentException("TutorId do indicador não pode ser nulo.");
        if (linkId == null)           throw new IllegalArgumentException("Id do link não pode ser nulo.");
        if (cpfIndicado == null)      throw new IllegalArgumentException("CPF do indicado não pode ser nulo.");
        if (timestampClique == null)  throw new IllegalArgumentException("Timestamp do clique não pode ser nulo.");
        this.id = id;
        this.tutorIndicadorId = tutorIndicadorId;
        this.linkId = linkId;
        this.cpfIndicado = cpfIndicado;
        this.timestampClique = timestampClique;
        this.status = StatusIndicacao.PENDENTE;
    }

    public Indicacao(IndicacaoId id, TutorId tutorIndicadorId, LinkIndicacaoId linkId,
                     CPF cpfIndicado, LocalDateTime timestampClique,
                     StatusIndicacao status, String cobrancaIndicadorId,
                     LocalDateTime convertidaEm, String motivoInvalidacao) {
        this.id = id;
        this.tutorIndicadorId = tutorIndicadorId;
        this.linkId = linkId;
        this.cpfIndicado = cpfIndicado;
        this.timestampClique = timestampClique;
        this.status = status;
        this.cobrancaIndicadorId = cobrancaIndicadorId;
        this.convertidaEm = convertidaEm;
        this.motivoInvalidacao = motivoInvalidacao;
    }

    public void converter(String cobrancaIndicadorId) {
        if (this.status != StatusIndicacao.PENDENTE)
            throw new IllegalStateException(
                "Só é possível converter indicações com status PENDENTE. Status atual: " + this.status);
        this.status = StatusIndicacao.CONVERTIDA;
        this.cobrancaIndicadorId = cobrancaIndicadorId;
        this.convertidaEm = LocalDateTime.now();
    }

    public void registrarDescontoIndicador(String cobrancaId) {
        if (this.status != StatusIndicacao.CONVERTIDA)
            throw new IllegalStateException("Só é possível registrar desconto em indicações CONVERTIDAS.");
        if (this.cobrancaIndicadorId != null)
            throw new IllegalStateException("Desconto do indicador já registrado na fatura " + this.cobrancaIndicadorId + ".");
        this.cobrancaIndicadorId = cobrancaId;
    }

    public void invalidar(String motivo) {
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("Motivo de invalidação não pode ser vazio.");
        if (this.status != StatusIndicacao.PENDENTE)
            throw new IllegalStateException(
                "Só é possível invalidar indicações com status PENDENTE. Status atual: " + this.status);
        this.status = StatusIndicacao.INVALIDA;
        this.motivoInvalidacao = motivo;
    }

    public IndicacaoId getId()                  { return id; }
    public TutorId getTutorIndicadorId()        { return tutorIndicadorId; }
    public LinkIndicacaoId getLinkId()          { return linkId; }
    public CPF getCpfIndicado()                 { return cpfIndicado; }
    public LocalDateTime getTimestampClique()   { return timestampClique; }
    public StatusIndicacao getStatus()          { return status; }
    public String getCobrancaIndicadorId()      { return cobrancaIndicadorId; }
    public LocalDateTime getConvertidaEm()      { return convertidaEm; }
    public String getMotivoInvalidacao()        { return motivoInvalidacao; }
}
