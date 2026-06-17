package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin;

import java.math.BigDecimal;
import java.util.Set;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

public class CriarRacaoUseCase {

    private final IRacaoRepositorio repositorio;

    public CriarRacaoUseCase(IRacaoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IRacaoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public Racao executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");
        Racao racao = new Racao(
                RacaoId.gerar(),
                entrada.fabricante, entrada.linha,
                entrada.densidadeCaloricaKcalPorKg,
                entrada.faixasIndicadas, entrada.portesIndicados,
                entrada.comorbidadesIndicadas);
        repositorio.salvar(racao);
        return racao;
    }

    public record Entrada(
            String fabricante,
            String linha,
            BigDecimal densidadeCaloricaKcalPorKg,
            Set<FaixaEtaria> faixasIndicadas,
            Set<Porte> portesIndicados,
            Set<Comorbidade> comorbidadesIndicadas) {}
}
