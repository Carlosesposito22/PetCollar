package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * <h2>Template Method — Processamento de Conversão por Webhook (F-04)</h2>
 *
 * <p>Define o <strong>esqueleto fixo</strong> do algoritmo de confirmação de
 * conversão de uma indicação, cobrindo as regras RN-4, RN-5, RN-6 e RN-12.
 * O passo de <em>validação de fraude</em> ({@link #validarFraude}) é abstrato —
 * cada subclasse concreta aplica a estratégia adequada ao tipo de confirmação
 * (automática via gateway ou manual por administrador).
 *
 * <p>Subclasses disponíveis:
 * <ul>
 *   <li>{@link ProcessamentoWebhookAutomatico} — confirmação via gateway de pagamentos
 *       com verificação de método de pagamento (RN-8).</li>
 *   <li>{@link ProcessamentoWebhookManual} — confirmação administrativa que dispensa
 *       a verificação automática de fraude.</li>
 * </ul>
 *
 * <p>Nenhuma subclasse pode sobrescrever {@link #processar} (marcado {@code final}).
 */
public abstract class ProcessamentoWebhookTemplate {

    static final BigDecimal PERCENTUAL_DESCONTO_INDICADOR = new BigDecimal("0.15");

    protected final IIndicacaoRepositorio indicacaoRepositorio;
    protected final IEventoAuditoriaRepositorio auditoriaRepositorio;
    protected final IDescontoFaturaPort descontoFatura;
    protected final IMotorGamificacaoPort motorGamificacao;

    protected ProcessamentoWebhookTemplate(IIndicacaoRepositorio indicacaoRepositorio,
                                           IEventoAuditoriaRepositorio auditoriaRepositorio,
                                           IDescontoFaturaPort descontoFatura,
                                           IMotorGamificacaoPort motorGamificacao) {
        if (indicacaoRepositorio == null) throw new IllegalArgumentException("Repositório de indicações não pode ser nulo.");
        if (auditoriaRepositorio == null) throw new IllegalArgumentException("Repositório de auditoria não pode ser nulo.");
        if (descontoFatura == null)       throw new IllegalArgumentException("Port de desconto de fatura não pode ser nulo.");
        if (motorGamificacao == null)     throw new IllegalArgumentException("Motor de gamificação não pode ser nulo.");
        this.indicacaoRepositorio = indicacaoRepositorio;
        this.auditoriaRepositorio = auditoriaRepositorio;
        this.descontoFatura = descontoFatura;
        this.motorGamificacao = motorGamificacao;
    }

    // ── Template method ─────────────────────────────────────────────────────

    /**
     * Esqueleto fixo do processamento de conversão. Ordem:
     * <ol>
     *   <li>Busca e valida a indicação pendente (RN-4).</li>
     *   <li>Executa a validação de fraude — <em>passo abstrato</em> (RN-8).</li>
     *   <li>Aplica desconto de 15% na próxima fatura do indicador (RN-5 / RN-9).</li>
     *   <li>Confirma a indicação como CONVERTIDA.</li>
     *   <li>Concede Conquista Lendária, se aplicável — <em>hook</em> (RN-6).</li>
     *   <li>Persiste todos os eventos de auditoria (RN-12).</li>
     * </ol>
     */
    public final void processar(IndicacaoId indicacaoId, String tokenMetodoPagamento) {
        if (indicacaoId == null)
            throw new IllegalArgumentException("Id da indicação não pode ser nulo.");

        // Passo 1 — busca e validação de pré-condição (RN-4)
        Indicacao indicacao = buscarIndicacaoPendente(indicacaoId);

        // Passo 2 — validação de fraude (abstrato: varia por subclasse)
        validarFraude(indicacao, tokenMetodoPagamento);

        // Passo 3 — RN-12: confirmar pagamento recebido
        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.PAGAMENTO_CONFIRMADO,
            indicacao.getTutorIndicadorId(),
            indicacaoId,
            "Pagamento da primeira mensalidade do indicado CPF "
                + indicacao.getCpfIndicado().getValor() + " confirmado."
        ));

        // Passo 4 — RN-5 / RN-9: desconto ao indicador
        Optional<String> cobrancaDesconto = descontoFatura.aplicarDescontoProximaFatura(
            indicacao.getTutorIndicadorId(), PERCENTUAL_DESCONTO_INDICADOR
        );
        indicacao.converter(cobrancaDesconto.orElse(null));
        indicacaoRepositorio.salvar(indicacao);

        if (cobrancaDesconto.isPresent()) {
            auditoriaRepositorio.salvar(new EventoAuditoria(
                EventoAuditoriaId.gerar(),
                TipoEventoAuditoria.DESCONTO_INDICADOR_APLICADO,
                indicacao.getTutorIndicadorId(),
                indicacaoId,
                "Desconto de 15% aplicado na cobrança " + cobrancaDesconto.get() + " (RN-5)."
            ));
        }

        // Passo 5 — RN-6: Conquista Lendária (controlada por hook)
        if (deveDispararGamificacao()) {
            motorGamificacao.concederConquistaLendaria(indicacao.getTutorIndicadorId());
            auditoriaRepositorio.salvar(new EventoAuditoria(
                EventoAuditoriaId.gerar(),
                TipoEventoAuditoria.CONQUISTA_CONCEDIDA,
                indicacao.getTutorIndicadorId(),
                indicacaoId,
                "Conquista Lendária concedida ao Tutor indicador "
                    + indicacao.getTutorIndicadorId().getValor() + " (RN-6)."
            ));
        }
    }

    // ── Passo abstrato ──────────────────────────────────────────────────────

    /**
     * Passo de validação de fraude — cada subclasse concreta implementa
     * a estratégia adequada ao tipo de confirmação.
     *
     * @param indicacao            indicação sendo convertida
     * @param tokenMetodoPagamento token/fingerprint do método de pagamento usado pelo indicado
     * @throws IllegalStateException se fraude for detectada
     */
    protected abstract void validarFraude(Indicacao indicacao, String tokenMetodoPagamento);

    // ── Hook ────────────────────────────────────────────────────────────────

    /**
     * Hook que subclasses podem sobrescrever para suprimir a concessão de
     * Conquista Lendária em cenários especiais. Padrão: {@code true}.
     */
    protected boolean deveDispararGamificacao() {
        return true;
    }

    // ── Passo comum interno ─────────────────────────────────────────────────

    private Indicacao buscarIndicacaoPendente(IndicacaoId id) {
        Indicacao indicacao = indicacaoRepositorio.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Indicação não encontrada: " + id.getValor()));
        if (indicacao.getStatus() != StatusIndicacao.PENDENTE)
            throw new IllegalStateException(
                "Indicação não está com status PENDENTE. Status atual: " + indicacao.getStatus());
        return indicacao;
    }
}
