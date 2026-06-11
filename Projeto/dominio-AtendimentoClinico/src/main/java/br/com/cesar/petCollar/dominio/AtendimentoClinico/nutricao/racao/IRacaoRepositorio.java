package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída do catálogo de rações. A implementação vive na infra
 * ({@code RacaoRepositorioJpa}) e é populada via seed (CommandLineRunner).
 */
public interface IRacaoRepositorio {

    void salvar(Racao racao);

    Optional<Racao> buscarPorId(RacaoId id);

    List<Racao> listarTodas();

    /** Quantidade de rações no catálogo — usado pelo seed para evitar reinserção. */
    long contar();
}
