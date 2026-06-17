package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.util.List;
import java.util.Optional;

public interface IRacaoRepositorio {

    void salvar(Racao racao);

    Optional<Racao> buscarPorId(RacaoId id);

    List<Racao> listarAtivas();

    List<Racao> listarTodas();

    long contar();
}
