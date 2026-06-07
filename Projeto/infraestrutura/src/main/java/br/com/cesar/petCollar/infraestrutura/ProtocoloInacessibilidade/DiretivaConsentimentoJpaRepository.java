package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiretivaConsentimentoJpaRepository
        extends JpaRepository<DiretivaConsentimentoJpa, String> {

    Optional<DiretivaConsentimentoJpa> findByPacienteId(String pacienteId);
}
