package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.IPlanoRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.PublicadorDeAlteracoesPlano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.ValorMensalidade;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

import java.util.List;

/**
 * Caso de uso de administração de planos (F-07 — gestão do catálogo). Criação
 * de novos planos e alteração de nome/mensalidade de planos existentes. Ao
 * alterar, dispara o {@link PublicadorDeAlteracoesPlano} para que os
 * observadores registrados (ex.: {@code NotificacaoAlteracaoPlanoObservador})
 * reajam à mudança sem acoplamento direto (padrão Observer, CLAUDE.md §8).
 */
public class GerenciarPlanoUseCase {

    private final IPlanoRepositorio planoRepositorio;
    private final PublicadorDeAlteracoesPlano publicadorDeAlteracoesPlano;

    public GerenciarPlanoUseCase(IPlanoRepositorio planoRepositorio,
                                  PublicadorDeAlteracoesPlano publicadorDeAlteracoesPlano) {
        if (planoRepositorio == null)
            throw new IllegalArgumentException("IPlanoRepositorio não pode ser nulo.");
        if (publicadorDeAlteracoesPlano == null)
            throw new IllegalArgumentException("PublicadorDeAlteracoesPlano não pode ser nulo.");
        this.planoRepositorio = planoRepositorio;
        this.publicadorDeAlteracoesPlano = publicadorDeAlteracoesPlano;
    }

    public Plano criar(String nome, String valorMensalidade) {
        Plano plano = new Plano(PlanoId.gerar(), nome, ValorMensalidade.de(valorMensalidade));
        planoRepositorio.salvar(plano);
        return plano;
    }

    public Plano alterar(PlanoId id, String nome, String valorMensalidade) {
        Plano plano = planoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + id.getValor()));
        plano.alterar(nome, ValorMensalidade.de(valorMensalidade));
        planoRepositorio.salvar(plano);
        publicadorDeAlteracoesPlano.publicar(plano);
        return plano;
    }

    public List<Plano> listar() {
        return planoRepositorio.listar();
    }

    public void excluir(PlanoId id) {
        Plano plano = planoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + id.getValor()));
        planoRepositorio.remover(plano.getId());
    }
}
