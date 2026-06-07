package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponsavelSecundarioJpaRepository
        extends JpaRepository<ResponsavelSecundarioJpa, String> {

    List<ResponsavelSecundarioJpa> findByPacienteId(String pacienteId);
}
