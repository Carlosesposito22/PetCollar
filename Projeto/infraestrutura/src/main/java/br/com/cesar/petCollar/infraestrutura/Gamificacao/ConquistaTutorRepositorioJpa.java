package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaTutor;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IConquistaTutorRepositorio;

@Repository
public class ConquistaTutorRepositorioJpa implements IConquistaTutorRepositorio {

    private final ConquistaTutorJpaRepository jpa;

    public ConquistaTutorRepositorioJpa(ConquistaTutorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(ConquistaTutor conquista) {
        jpa.save(ConquistaTutorJpa.fromDomain(conquista));
    }

    @Override
    public ConquistaTutor findById(ConquistaId id) {
        return jpa.findById(id.getValor()).map(ConquistaTutorJpa::toDomain).orElse(null);
    }

    @Override
    public List<ConquistaTutor> findByTutorId(String tutorId) {
        return jpa.findByTutorId(tutorId).stream().map(ConquistaTutorJpa::toDomain).toList();
    }

    @Override
    public boolean existsByTutorEBadge(String tutorId, BadgeId badgeId) {
        return jpa.existsByTutorIdAndBadgeId(tutorId, badgeId.getValor());
    }
}
