package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacienteRecepcaoJpaRepository extends JpaRepository<PacienteRecepcaoJpa, String> {
    List<PacienteRecepcaoJpa> findByTutorId(String tutorId);
}