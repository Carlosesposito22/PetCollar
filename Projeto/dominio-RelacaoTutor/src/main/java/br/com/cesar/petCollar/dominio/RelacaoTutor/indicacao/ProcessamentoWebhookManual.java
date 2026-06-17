package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

public class ProcessamentoWebhookManual extends ProcessamentoWebhookTemplate {

    public ProcessamentoWebhookManual(IIndicacaoRepositorio indicacaoRepositorio,
                                      IEventoAuditoriaRepositorio auditoriaRepositorio,
                                      IDescontoFaturaPort descontoFatura,
                                      IMotorGamificacaoPort motorGamificacao) {
        super(indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao);
    }

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
