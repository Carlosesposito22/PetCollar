package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

public class ProcessamentoWebhookAutomatico extends ProcessamentoWebhookTemplate {

    public ProcessamentoWebhookAutomatico(IIndicacaoRepositorio indicacaoRepositorio,
                                          IEventoAuditoriaRepositorio auditoriaRepositorio,
                                          IDescontoFaturaPort descontoFatura,
                                          IMotorGamificacaoPort motorGamificacao) {
        super(indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao);
    }

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
