package petcollar.dominio.assinaturafaturamento.indicacao;

import petcollar.dominio.assinaturafaturamento.StatusAssinatura;

public class GeracaoLinkIndicacaoService {

    private static final String URL_BASE = "https://petcollar.app/indicar";

    private final ILinkIndicacaoRepositorio repositorio;

    public GeracaoLinkIndicacaoService(ILinkIndicacaoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public LinkIndicacao gerarOuObterLink(String tutorId, String cpfTutor, StatusAssinatura statusConta) {
        if (statusConta != StatusAssinatura.ATIVA)
            throw new IllegalStateException(
                    "O painel de indicação está disponível apenas para Tutores com conta ATIVA.");

        LinkIndicacao existente = repositorio.findByTutorId(tutorId);
        if (existente != null)
            return existente;

        CodigoIndicacao codigo = CodigoIndicacao.gerar();
        UrlCompartilhamento url = new UrlCompartilhamento(URL_BASE, codigo);
        LinkIndicacao novoLink = new LinkIndicacao(
                LinkIndicacaoId.gerar(), tutorId, cpfTutor, codigo, url);
        repositorio.save(novoLink);
        return novoLink;
    }
}
