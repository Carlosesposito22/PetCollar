package br.com.cesar.petCollar.apresentacao.PortalTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacinaJpaRepository extends JpaRepository<VacinaJpa, String> {

    List<VacinaJpa> findByPacienteId(String pacienteId);

    void deleteByPacienteId(String pacienteId);
}
