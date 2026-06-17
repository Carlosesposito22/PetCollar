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

    /** Todas as rações ativas (não-desativadas). Usado pelo médico e tutor. */
    List<Racao> listarAtivas();

    /** Todas as rações, ativas e desativadas. Usado pelo admin no CRUD. */
    List<Racao> listarTodas();

    /** Quantidade de rações no catálogo — usado pelo seed para evitar reinserção. */
    long contar();
}
