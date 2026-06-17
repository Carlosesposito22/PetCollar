package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin;

import java.math.BigDecimal;
import java.util.Set;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

/**
 * Caso de uso do admin: atualiza os campos editáveis de uma ração existente.
 * Não muda o status ativo/desativada — para isso há use cases dedicados.
 */
public class AtualizarRacaoUseCase {

    private final IRacaoRepositorio repositorio;

    public AtualizarRacaoUseCase(IRacaoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IRacaoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public Racao executar(RacaoId id, Entrada entrada) {
        if (id == null) throw new IllegalArgumentException("RacaoId é obrigatório.");
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");
        Racao racao = repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Ração não encontrada: " + id));
        racao.editar(entrada.fabricante, entrada.linha,
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
