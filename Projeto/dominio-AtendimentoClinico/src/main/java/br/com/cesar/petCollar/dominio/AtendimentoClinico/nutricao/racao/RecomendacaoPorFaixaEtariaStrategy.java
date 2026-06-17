package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

public class RecomendacaoPorFaixaEtariaStrategy implements EstrategiaRecomendacaoRacao {

    private static final int PONTUACAO_INDICADA = 20;

    @Override
    public String nome() { return "FaixaEtaria"; }

    @Override
    public int pontuar(Racao racao, PerfilNutricional perfil) {
        return racao.compativelComFaixa(perfil.faixaEtaria()) ? PONTUACAO_INDICADA : 0;
    }
}
