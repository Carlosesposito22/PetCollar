package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

/**
 * Pontua rações cuja indicação inclua o porte do paciente.
 */
public class RecomendacaoPorPorteStrategy implements EstrategiaRecomendacaoRacao {

    private static final int PONTUACAO_INDICADA = 15;

    @Override
    public String nome() { return "Porte"; }

    @Override
    public int pontuar(Racao racao, PerfilNutricional perfil) {
        return racao.compativelComPorte(perfil.porte()) ? PONTUACAO_INDICADA : 0;
    }
}
