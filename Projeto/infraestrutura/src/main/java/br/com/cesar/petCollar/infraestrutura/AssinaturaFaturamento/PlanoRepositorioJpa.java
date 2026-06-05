package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.IPlanoRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

/**
 * Adapter JPA da interface de domínio {@link IPlanoRepositorio}. Traduz domínio
 * ↔ entidade via {@code fromDomain}/{@code toDomain} (§6.4 do CLAUDE.md).
 */
@Repository
public class PlanoRepositorioJpa implements IPlanoRepositorio {

    private final PlanoJpaRepository jpa;

    public PlanoRepositorioJpa(PlanoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(Plano plano) {
        jpa.save(PlanoJpa.fromDomain(plano));
    }

    @Override
    public Optional<Plano> buscarPorId(PlanoId id) {
        return jpa.findById(id.getValor()).map(PlanoJpa::toDomain);
    }

    @Override
    public Optional<Plano> buscarPorNome(String nome) {
        return jpa.findByNomeIgnoreCase(nome).map(PlanoJpa::toDomain);
    }

    @Override
    public List<Plano> listar() {
        return jpa.findAll().stream().map(PlanoJpa::toDomain).toList();
    }
}
