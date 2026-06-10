package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

/**
 * <h2>ConcreteClass — Confirmação Manual por Administrador</h2>
 *
 * <p>Confirmação de conversão realizada manualmente por um administrador da
 * plataforma (ex.: resolução de disputa via suporte, pagamento confirmado fora
 * do gateway). Sobrescreve {@link #validarFraude} dispensando a verificação
 * automática de método de pagamento e registrando um evento de auditoria
 * explicativo (RN-12).
 *
 * <p>O hook {@link #deveDispararGamificacao()} mantém o valor padrão
 * ({@code true}), garantindo que a Conquista Lendária ainda seja concedida
 * ao Tutor indicador (RN-6).
 */
public class ProcessamentoWebhookManual extends ProcessamentoWebhookTemplate {

    public ProcessamentoWebhookManual(IIndicacaoRepositorio indicacaoRepositorio,
                                      IEventoAuditoriaRepositorio auditoriaRepositorio,
                                      IDescontoFaturaPort descontoFatura,
                                      IMotorGamificacaoPort motorGamificacao) {
        super(indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao);
    }

    /**
     * Dispensa a verificação automática de fraude por método de pagamento.
     * Registra evento de auditoria sinalizando que a confirmação foi manual (RN-12).
     */
    @Override
    protected void validarFraude(Indicacao indicacao, String tokenMetodoPagamento) {
        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.PAGAMENTO_CONFIRMADO,
            indicacao.getTutorIndicadorId(),
            indicacao.getId(),
            "Confirmação manual por administrador — verificação automática de fraude dispensada."
        ));
    }
}
