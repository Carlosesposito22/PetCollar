package petcollar.dominio.assinaturafaturamento.indicacao;

public class AtribuicaoLastClickService {

    private final IConversaoIndicacaoRepositorio repositorio;

    public AtribuicaoLastClickService(IConversaoIndicacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public ConversaoIndicacao atribuirUltimoClique(ConversaoId conversaoId,
                                                   LinkIndicacaoId novoLinkId,
                                                   String novoTutorIndicadorId) {
        ConversaoIndicacao conversao = repositorio.findById(conversaoId);
        if (conversao == null)
            throw new IllegalArgumentException(
                    "ConversaoIndicacao não encontrada com o id informado.");

        conversao.atribuirIndicador(novoTutorIndicadorId, novoLinkId);
        conversao.adicionarEvento(
                new EventoRastreio(TipoEventoRastreio.LAST_CLICK_ATRIBUIDO, "{}"));
        repositorio.save(conversao);
        return conversao;
    }
}
