package petcollar.dominio.assinaturafaturamento.indicacao;

public class DisparoGamificacaoService {

    private static final String CHAVE_INDICACAO_CONFIRMADA = "INDICACAO_CONFIRMADA";

    private final IRecompensaIndicacaoRepositorio repositorio;

    public DisparoGamificacaoService(IRecompensaIndicacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public IndicacaoValidadaParaGamificacaoEvent disparar(IndicacaoConfirmadaEvent evento) {
        if (evento == null)
            throw new IllegalArgumentException("IndicacaoConfirmadaEvent não pode ser nulo.");

        RecompensaIndicacao recompensa = repositorio.findByConversaoId(evento.getConversaoId());
        if (recompensa == null)
            throw new IllegalArgumentException(
                    "RecompensaIndicacao não encontrada para a conversão informada.");

        recompensa.dispararGamificacao();
        repositorio.save(recompensa);

        return new IndicacaoValidadaParaGamificacaoEvent(
                recompensa.getId(),
                recompensa.getTutorIndicadorId(),
                CHAVE_INDICACAO_CONFIRMADA
        );
    }
}
