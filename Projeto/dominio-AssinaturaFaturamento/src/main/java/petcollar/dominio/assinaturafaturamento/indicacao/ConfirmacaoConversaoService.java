package petcollar.dominio.assinaturafaturamento.indicacao;

import petcollar.dominio.assinaturafaturamento.CobrancaId;

public class ConfirmacaoConversaoService {

    private final IConversaoIndicacaoRepositorio conversaoRepositorio;
    private final IRecompensaIndicacaoRepositorio recompensaRepositorio;
    private final ValidacaoFraudeService validacaoFraudeService;
    private final AplicacaoDescontoIndicadoService descontoIndicadoService;
    private final AplicacaoDescontoIndicadorService descontoIndicadorService;

    public ConfirmacaoConversaoService(IConversaoIndicacaoRepositorio conversaoRepositorio,
                                       IRecompensaIndicacaoRepositorio recompensaRepositorio,
                                       ValidacaoFraudeService validacaoFraudeService,
                                       AplicacaoDescontoIndicadoService descontoIndicadoService,
                                       AplicacaoDescontoIndicadorService descontoIndicadorService) {
        this.conversaoRepositorio = conversaoRepositorio;
        this.recompensaRepositorio = recompensaRepositorio;
        this.validacaoFraudeService = validacaoFraudeService;
        this.descontoIndicadoService = descontoIndicadoService;
        this.descontoIndicadorService = descontoIndicadorService;
    }

    public IndicacaoConfirmadaEvent confirmar(ConversaoId conversaoId,
                                              AssinaturaMetodoPagamento metodoPagamento,
                                              CobrancaId primeiraCobrancaIndicadoId,
                                              CobrancaId proximaCobrancaIndicadorId) {
        ConversaoIndicacao conversao = conversaoRepositorio.findById(conversaoId);
        if (conversao == null)
            throw new IllegalArgumentException(
                    "ConversaoIndicacao não encontrada com o id informado.");

        conversao.registrarMetodoPagamento(metodoPagamento);
        validacaoFraudeService.validarMetodoPagamentoDuplicado(
                conversao, conversao.getTutorIndicadorId());

        if (conversao.getStatus() == StatusConversao.INVALIDA_FRAUDE)
            throw new IllegalStateException(
                    "Fraude detectada: método de pagamento duplicado. Recompensa invalidada.");

        conversao.confirmar();
        conversaoRepositorio.save(conversao);

        RecompensaIndicacao recompensa = new RecompensaIndicacao(
                RecompensaIndicacaoId.gerar(),
                conversaoId,
                conversao.getTutorIndicadorId(),
                primeiraCobrancaIndicadoId,
                proximaCobrancaIndicadorId
        );

        descontoIndicadoService.resolver(recompensa, 0.0);
        descontoIndicadorService.aplicar(recompensa);
        recompensaRepositorio.save(recompensa);

        return new IndicacaoConfirmadaEvent(
                conversaoId,
                conversao.getTutorIndicadorId(),
                conversao.getCpfIndicado()
        );
    }
}
