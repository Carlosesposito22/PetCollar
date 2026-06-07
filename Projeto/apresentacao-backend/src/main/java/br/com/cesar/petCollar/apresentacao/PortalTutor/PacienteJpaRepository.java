package br.com.cesar.petCollar.apresentacao.PortalTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteJpaRepository extends JpaRepository<PacienteJpa, String> {

    List<PacienteJpa> findByTutorId(String tutorId);
}
