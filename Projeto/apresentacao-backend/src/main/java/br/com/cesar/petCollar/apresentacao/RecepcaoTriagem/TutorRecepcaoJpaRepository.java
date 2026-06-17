package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TutorRecepcaoJpaRepository extends JpaRepository<TutorRecepcaoJpa, String> {
    Optional<TutorRecepcaoJpa> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    List<TutorRecepcaoJpa> findByEmailIgnoreCase(String email);
}
