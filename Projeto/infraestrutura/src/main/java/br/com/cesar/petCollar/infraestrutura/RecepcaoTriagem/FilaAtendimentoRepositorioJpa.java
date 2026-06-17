package br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem;

import petcollar.dominio.recepcaotriagem.triagem.IFilaAtendimentoRepositorio;
import petcollar.dominio.recepcaotriagem.triagem.PosicaoFila;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FilaAtendimentoRepositorioJpa implements IFilaAtendimentoRepositorio {

    private final FilaAtendimentoJpaRepository jpa;

    public FilaAtendimentoRepositorioJpa(FilaAtendimentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(PosicaoFila posicao) {
        jpa.save(PosicaoFilaJpa.fromDomain(posicao));
    }

    @Override
    public List<PosicaoFila> listarOrdenada() {
        return jpa.findAll().stream()
                .map(PosicaoFilaJpa::toDomain)
                .toList();
    }

    @Override
    public void remover(TriagemId triagemId) {
        jpa.deleteById(triagemId.getValor());
    }
}
