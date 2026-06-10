package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeJpaRepository extends JpaRepository<BadgeJpa, String> {
    List<BadgeJpa> findByChaveEvento(String chaveEvento);
}
