package petcollar.dominio.assinaturafaturamento.indicacao;

import petcollar.dominio.assinaturafaturamento.CobrancaId;

import java.time.LocalDateTime;

public class RecompensaIndicacao {

    private final RecompensaIndicacaoId id;
    private final ConversaoId conversaoId;
    private final String tutorIndicadorId;
    private DescontoIndicado descontoIndicado;
    private DescontoIndicador descontoIndicador;
    private boolean gamificacaoDisparada;
    private StatusRecompensa status;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public RecompensaIndicacao(RecompensaIndicacaoId id, ConversaoId conversaoId,
                               String tutorIndicadorId,
                               CobrancaId primeiraCobrancaIndicadoId,
                               CobrancaId cobrancaAlvoIndicadorId) {
        if (id == null)
            throw new IllegalArgumentException("RecompensaIndicacaoId não pode ser nulo.");
        if (conversaoId == null)
            throw new IllegalArgumentException("ConversaoId não pode ser nulo.");
        if (tutorIndicadorId == null || tutorIndicadorId.isBlank())
            throw new IllegalArgumentException("tutorIndicadorId não pode ser vazio.");
        if (primeiraCobrancaIndicadoId == null)
            throw new IllegalArgumentException("primeiraCobrancaIndicadoId não pode ser nulo.");
        if (cobrancaAlvoIndicadorId == null)
            throw new IllegalArgumentException("cobrancaAlvoIndicadorId não pode ser nulo.");
        this.id = id;
        this.conversaoId = conversaoId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.descontoIndicado = new DescontoIndicado(primeiraCobrancaIndicadoId);
        this.descontoIndicador = new DescontoIndicador(cobrancaAlvoIndicadorId);
        this.gamificacaoDisparada = false;
        this.status = StatusRecompensa.PENDENTE;
        this.criadoEm = LocalDateTime.now();
    }

    public RecompensaIndicacao(RecompensaIndicacaoId id, ConversaoId conversaoId,
                               String tutorIndicadorId, DescontoIndicado descontoIndicado,
                               DescontoIndicador descontoIndicador, boolean gamificacaoDisparada,
                               StatusRecompensa status, LocalDateTime criadoEm,
                               LocalDateTime atualizadoEm) {
        this.id = id;
        this.conversaoId = conversaoId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.descontoIndicado = descontoIndicado;
        this.descontoIndicador = descontoIndicador;
        this.gamificacaoDisparada = gamificacaoDisparada;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void aplicarDescontoIndicado() {
        if (this.status == StatusRecompensa.CANCELADA_POR_FRAUDE)
            throw new IllegalStateException(
                    "Não é possível aplicar desconto em recompensa cancelada por fraude.");
        this.descontoIndicado = this.descontoIndicado.aplicar();
        this.status = this.descontoIndicador.isAplicado()
                ? StatusRecompensa.AMBOS_APLICADOS
                : StatusRecompensa.DESCONTO_INDICADO_APLICADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void aplicarDescontoIndicador() {
        if (this.status == StatusRecompensa.CANCELADA_POR_FRAUDE)
            throw new IllegalStateException(
                    "Não é possível aplicar desconto em recompensa cancelada por fraude.");
        this.descontoIndicador = this.descontoIndicador.aplicar();
        this.status = this.descontoIndicado.isAplicado()
                ? StatusRecompensa.AMBOS_APLICADOS
                : StatusRecompensa.DESCONTO_INDICADO_APLICADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void dispararGamificacao() {
        if (this.status == StatusRecompensa.CANCELADA_POR_FRAUDE)
            throw new IllegalStateException(
                    "Não é possível disparar gamificação para recompensa cancelada por fraude.");
        this.gamificacaoDisparada = true;
        this.status = StatusRecompensa.GAMIFICACAO_DISPARADA;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void cancelarPorFraude() {
        this.status = StatusRecompensa.CANCELADA_POR_FRAUDE;
        this.atualizadoEm = LocalDateTime.now();
    }

    public RecompensaIndicacaoId getId()            { return id; }
    public ConversaoId getConversaoId()             { return conversaoId; }
    public String getTutorIndicadorId()             { return tutorIndicadorId; }
    public DescontoIndicado getDescontoIndicado()   { return descontoIndicado; }
    public DescontoIndicador getDescontoIndicador() { return descontoIndicador; }
    public boolean isGamificacaoDisparada()         { return gamificacaoDisparada; }
    public StatusRecompensa getStatus()             { return status; }
    public LocalDateTime getCriadoEm()              { return criadoEm; }
    public LocalDateTime getAtualizadoEm()          { return atualizadoEm; }
}
