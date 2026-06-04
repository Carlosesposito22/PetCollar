package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.stereotype.Repository;
import petcollar.dominio.recepcaotriagem.triagem.IFilaAtendimentoRepositorio;
import petcollar.dominio.recepcaotriagem.triagem.PosicaoFila;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class FilaAtendimentoRepositorioEmMemoria implements IFilaAtendimentoRepositorio {

    private final ConcurrentMap<String, PosicaoFila> fila = new ConcurrentHashMap<>();

    @Override
    public void salvar(PosicaoFila posicao) {
        fila.put(posicao.getTriagemId().getValor(), posicao);
    }

    @Override
    public List<PosicaoFila> listarOrdenada() {
        return new ArrayList<>(fila.values());
    }

    @Override
    public void remover(TriagemId triagemId) {
        fila.remove(triagemId.getValor());
    }
}