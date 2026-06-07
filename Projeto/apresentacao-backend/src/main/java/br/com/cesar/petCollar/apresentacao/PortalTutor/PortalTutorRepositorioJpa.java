package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.infraestrutura.SaudePreventiva.CicloVacinalJpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter JPA de {@link PortalTutorRepositorio} para Pacientes.
 * Ao remover um paciente, realiza cascade nos ciclos vacinais via
 * {@link CicloVacinalJpaRepository}.
 */
@Repository
public class PortalTutorRepositorioJpa implements PortalTutorRepositorio {

    private final PacienteJpaRepository pacientes;
    private final CicloVacinalJpaRepository ciclosVacinais;

    public PortalTutorRepositorioJpa(PacienteJpaRepository pacientes,
                                      CicloVacinalJpaRepository ciclosVacinais) {
        this.pacientes      = pacientes;
        this.ciclosVacinais = ciclosVacinais;
    }

    @Override
    public List<Paciente> listarPacientesDoTutor(String tutorId) {
        return pacientes.findByTutorId(tutorId).stream()
                .map(PacienteJpa::toDomain)
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .toList();
    }

    @Override
    public Optional<Paciente> buscarPaciente(String id) {
        return pacientes.findById(id).map(PacienteJpa::toDomain);
    }

    @Override
    public void salvarPaciente(Paciente paciente) {
        pacientes.save(PacienteJpa.fromDomain(paciente));
    }

    @Override
    @Transactional
    public void removerPaciente(String id) {
        ciclosVacinais.deleteByPacienteId(id);
        pacientes.deleteById(id);
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
