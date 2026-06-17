package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

public class RecomendacaoPorPorteStrategy implements EstrategiaRecomendacaoRacao {

    private static final int PONTUACAO_INDICADA = 15;

    @Override
    public String nome() { return "Porte"; }

    @Override
    public int pontuar(Racao racao, PerfilNutricional perfil) {
        return racao.compativelComPorte(perfil.porte()) ? PONTUACAO_INDICADA : 0;
    }
}
