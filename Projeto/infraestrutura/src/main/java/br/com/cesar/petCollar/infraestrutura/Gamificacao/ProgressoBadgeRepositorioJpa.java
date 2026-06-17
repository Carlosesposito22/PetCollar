package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IProgressoBadgeRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadgeId;

@Repository
public class ProgressoBadgeRepositorioJpa implements IProgressoBadgeRepositorio {

    private final ProgressoBadgeJpaRepository jpa;

    public ProgressoBadgeRepositorioJpa(ProgressoBadgeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(ProgressoBadge progresso) {
        jpa.save(ProgressoBadgeJpa.fromDomain(progresso));
    }

    @Override
    public ProgressoBadge findById(ProgressoBadgeId id) {
        return jpa.findById(id.getValor()).map(ProgressoBadgeJpa::toDomain).orElse(null);
    }

    @Override
    public ProgressoBadge findByTutorEBadge(String tutorId, BadgeId badgeId) {
        return jpa.findByTutorIdAndBadgeId(tutorId, badgeId.getValor()).map(ProgressoBadgeJpa::toDomain).orElse(null);
    }

    @Override
    public List<ProgressoBadge> findByTutorId(String tutorId) {
        return jpa.findByTutorId(tutorId).stream().map(ProgressoBadgeJpa::toDomain).toList();
    }
}
