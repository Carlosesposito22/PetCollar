package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.math.BigDecimal;
import java.util.Optional;

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

    public final void processar(IndicacaoId indicacaoId, String tokenMetodoPagamento) {
        if (indicacaoId == null)
            throw new IllegalArgumentException("Id da indicação não pode ser nulo.");

        Indicacao indicacao = buscarIndicacaoPendente(indicacaoId);

        validarFraude(indicacao, tokenMetodoPagamento);

        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.PAGAMENTO_CONFIRMADO,
            indicacao.getTutorIndicadorId(),
            indicacaoId,
            "Pagamento da primeira mensalidade do indicado CPF "
                + indicacao.getCpfIndicado().getValor() + " confirmado."
        ));

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

    protected abstract void validarFraude(Indicacao indicacao, String tokenMetodoPagamento);

    protected boolean deveDispararGamificacao() {
        return true;
    }

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
