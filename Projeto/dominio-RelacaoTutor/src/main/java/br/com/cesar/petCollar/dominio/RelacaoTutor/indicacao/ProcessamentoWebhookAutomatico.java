package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

/**
 * <h2>ConcreteClass — Processamento Automático via Gateway (RN-8)</h2>
 *
 * <p>Confirmação de conversão disparada automaticamente pelo webhook do gateway
 * de pagamentos. Implementa o passo {@link #validarFraude} verificando se o
 * método de pagamento utilizado pelo indicado coincide com algum método já
 * cadastrado pelo Tutor indicador (RN-8). Quando a fraude é detectada, invalida
 * a indicação e interrompe o processamento.
 */
public class ProcessamentoWebhookAutomatico extends ProcessamentoWebhookTemplate {

    public ProcessamentoWebhookAutomatico(IIndicacaoRepositorio indicacaoRepositorio,
                                          IEventoAuditoriaRepositorio auditoriaRepositorio,
                                          IDescontoFaturaPort descontoFatura,
                                          IMotorGamificacaoPort motorGamificacao) {
        super(indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao);
    }

    /**
     * Verifica fraude por método de pagamento (RN-8).
     * Invalida a indicação e lança exceção se o token coincidir com algum
     * método do Tutor indicador.
     */
    @Override
    protected void validarFraude(Indicacao indicacao, String tokenMetodoPagamento) {
        if (tokenMetodoPagamento == null || tokenMetodoPagamento.isBlank())
            throw new IllegalArgumentException("Token do método de pagamento não pode ser vazio.");

        if (descontoFatura.metodoPagamentoCoincideComIndicador(
                indicacao.getTutorIndicadorId(), tokenMetodoPagamento)) {

            indicacao.invalidar(
                "Método de pagamento do indicado coincide com o do Tutor indicador (RN-8).");
            indicacaoRepositorio.salvar(indicacao);
            auditoriaRepositorio.salvar(new EventoAuditoria(
                EventoAuditoriaId.gerar(),
                TipoEventoAuditoria.FRAUDE_BLOQUEADA,
                indicacao.getTutorIndicadorId(),
                indicacao.getId(),
                "Recompensa bloqueada: método de pagamento idêntico ao do Tutor indicador (RN-8)."
            ));
            throw new IllegalStateException(
                "Recompensa invalidada: método de pagamento idêntico ao do Tutor indicador (RN-8).");
        }
    }
}
