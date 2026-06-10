package br.com.cesar.petCollar.dominio.Gamificacao.conquista;

import java.util.List;

public interface IConquistaTutorRepositorio {
    void save(ConquistaTutor conquista);
    ConquistaTutor findById(ConquistaId id);
    List<ConquistaTutor> findByTutorId(String tutorId);
    boolean existsByTutorEBadge(String tutorId, BadgeId badgeId);
}
