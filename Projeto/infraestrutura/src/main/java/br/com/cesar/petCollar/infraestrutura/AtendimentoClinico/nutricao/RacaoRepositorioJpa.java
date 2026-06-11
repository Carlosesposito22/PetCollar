package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.IRacaoRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

/**
 * Adapter JPA do {@link IRacaoRepositorio}.
 */
@Repository
public class RacaoRepositorioJpa implements IRacaoRepositorio {

    private final RacaoJpaRepository jpa;

    public RacaoRepositorioJpa(RacaoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(Racao racao) {
        jpa.save(RacaoJpa.fromDomain(racao));
    }

    @Override
    public Optional<Racao> buscarPorId(RacaoId id) {
        return jpa.findById(id.getValor()).map(RacaoJpa::toDomain);
    }

    @Override
    public List<Racao> listarTodas() {
        return jpa.findAll().stream().map(RacaoJpa::toDomain).toList();
    }

    @Override
    public long contar() {
        return jpa.count();
    }
}
