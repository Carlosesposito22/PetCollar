package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin;

import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;

public class ListarRacoesAdminUseCase {

    private final IRacaoRepositorio repositorio;

    public ListarRacoesAdminUseCase(IRacaoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IRacaoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public List<Racao> executar() {
        return repositorio.listarTodas();
    }
}
