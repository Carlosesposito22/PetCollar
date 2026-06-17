package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import java.math.BigDecimal;
import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.EstrategiaRecomendacaoRacao.PerfilNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoRecomendada;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RecomendacaoRacaoService;

public class RecomendarRacoesUseCase {

    private static final int TOP_N_PADRAO = 3;

    private final RecomendacaoRacaoService servico;

    public RecomendarRacoesUseCase(RecomendacaoRacaoService servico) {
        if (servico == null)
            throw new IllegalArgumentException("RecomendacaoRacaoService é obrigatório.");
        this.servico = servico;
    }

    public List<RacaoRecomendada> executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");
        PerfilNutricional perfil = new PerfilNutricional(
                Porte.de(entrada.pesoIdealKg),
                FaixaEtaria.de(entrada.idadeAnos),
                entrada.comorbidade);
        int n = entrada.topN <= 0 ? TOP_N_PADRAO : entrada.topN;
        return servico.recomendarTop(perfil, n);
    }

    public record Entrada(BigDecimal pesoIdealKg, int idadeAnos, Comorbidade comorbidade, int topN) {}
}
