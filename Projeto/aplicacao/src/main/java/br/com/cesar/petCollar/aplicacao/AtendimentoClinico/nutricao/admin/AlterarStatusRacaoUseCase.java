package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

/**
 * Caso de uso do admin: ativa ou desativa uma ração (soft-delete). Permite
 * consultar previamente quantos planos a referenciam para o aviso de
 * impacto.
 */
public class AlterarStatusRacaoUseCase {

    private final IRacaoRepositorio repositorio;
    private final IPlanoNutricionalRepositorio planosRepositorio;

    public AlterarStatusRacaoUseCase(IRacaoRepositorio repositorio,
                                     IPlanoNutricionalRepositorio planosRepositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IRacaoRepositorio é obrigatório.");
        if (planosRepositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        this.repositorio = repositorio;
        this.planosRepositorio = planosRepositorio;
    }

    public Racao desativar(RacaoId id) {
        Racao racao = exigirRacao(id);
        racao.desativar();
        repositorio.salvar(racao);
        return racao;
    }

    public Racao reativar(RacaoId id) {
        Racao racao = exigirRacao(id);
        racao.reativar();
        repositorio.salvar(racao);
        return racao;
    }

    /** Conta planos que prescreveram esta ração — útil para o aviso de impacto. */
    public long contarPlanosUsando(RacaoId id) {
        if (id == null) throw new IllegalArgumentException("RacaoId é obrigatório.");
        return planosRepositorio.contarPlanosComRacao(id);
    }

    private Racao exigirRacao(RacaoId id) {
        if (id == null) throw new IllegalArgumentException("RacaoId é obrigatório.");
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Ração não encontrada: " + id));
    }
}
