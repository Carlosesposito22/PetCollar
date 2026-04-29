package petcollar.dominio.assinaturafaturamento.indicacao;

public class ValidacaoFraudeService {

    private final IConversaoIndicacaoRepositorio repositorio;

    public ValidacaoFraudeService(IConversaoIndicacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public ConversaoIndicacao validarAutoIndicacao(ConversaoIndicacao conversao, String cpfIndicador) {
        if (conversao.getCpfIndicado().equals(cpfIndicador)) {
            conversao.adicionarEvento(
                    new EventoRastreio(TipoEventoRastreio.FRAUDE_DETECTADA_AUTO_INDICACAO, "{}"));
            conversao.invalidarFraude();
            repositorio.save(conversao);
        }
        return conversao;
    }

    public ConversaoIndicacao validarCpfJaConvertido(ConversaoIndicacao conversao) {
        if (repositorio.existsByCpfIndicadoEStatusConfirmada(conversao.getCpfIndicado())) {
            conversao.invalidarCpfDuplicado();
            repositorio.save(conversao);
        }
        return conversao;
    }

    public ConversaoIndicacao validarMetodoPagamentoDuplicado(ConversaoIndicacao conversao,
                                                              String tutorIndicadorId) {
        AssinaturaMetodoPagamento metodo = conversao.getMetodoPagamento();
        if (metodo != null && repositorio.existsByHashMetodoForTutorIndicador(
                metodo.getHashMetodo(), tutorIndicadorId)) {
            conversao.adicionarEvento(
                    new EventoRastreio(TipoEventoRastreio.FRAUDE_DETECTADA_METODO_DUPLICADO, "{}"));
            conversao.invalidarFraude();
            repositorio.save(conversao);
        }
        return conversao;
    }
}
