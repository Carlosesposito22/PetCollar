package br.com.cesar.petCollar.dominio.Gamificacao.conquista;

import java.util.List;

public interface IBadgeRepositorio {
    void save(Badge badge);
    Badge findById(BadgeId id);
    List<Badge> findByChaveEvento(String chaveEvento);
    List<Badge> findAll();
}
