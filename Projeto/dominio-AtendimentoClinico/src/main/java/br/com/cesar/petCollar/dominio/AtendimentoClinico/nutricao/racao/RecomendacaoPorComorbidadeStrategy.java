package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

public class RecomendacaoPorComorbidadeStrategy implements EstrategiaRecomendacaoRacao {

    private static final int PONTUACAO_COBERTURA = 50;
    private static final int PONTUACAO_NEUTRA    = 5;

    @Override
    public String nome() { return "Comorbidade"; }

    @Override
    public int pontuar(Racao racao, PerfilNutricional perfil) {
        if (perfil.comorbidade() == Comorbidade.NENHUMA) return PONTUACAO_NEUTRA;
        if (racao.cobreComorbidade(perfil.comorbidade())) return PONTUACAO_COBERTURA;
        return 0;
    }
}
