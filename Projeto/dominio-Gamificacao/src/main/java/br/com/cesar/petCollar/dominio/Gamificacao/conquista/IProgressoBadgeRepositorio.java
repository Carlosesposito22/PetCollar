package br.com.cesar.petCollar.dominio.Gamificacao.conquista;

import java.util.List;

public interface IProgressoBadgeRepositorio {
    void save(ProgressoBadge progresso);
    ProgressoBadge findById(ProgressoBadgeId id);
    ProgressoBadge findByTutorEBadge(String tutorId, BadgeId badgeId);
    List<ProgressoBadge> findByTutorId(String tutorId);
}
