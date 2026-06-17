package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.Badge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IBadgeRepositorio;

@Repository
public class BadgeRepositorioJpa implements IBadgeRepositorio {

    private final BadgeJpaRepository jpa;

    public BadgeRepositorioJpa(BadgeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Badge badge) {
        jpa.save(BadgeJpa.fromDomain(badge));
    }

    @Override
    public Badge findById(BadgeId id) {
        return jpa.findById(id.getValor()).map(BadgeJpa::toDomain).orElse(null);
    }

    @Override
    public List<Badge> findByChaveEvento(String chaveEvento) {
        return jpa.findByChaveEvento(chaveEvento).stream().map(BadgeJpa::toDomain).toList();
    }

    @Override
    public List<Badge> findAll() {
        return jpa.findAll().stream().map(BadgeJpa::toDomain).toList();
    }
}
