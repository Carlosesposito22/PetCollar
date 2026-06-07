package br.com.cesar.petCollar.apresentacao.PortalTutor;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter JPA de {@link PortalTutorRepositorio}. Substitui
 * {@link PortalTutorRepositorioEmMemoria} quando o banco está ativo.
 */
@Repository
public class PortalTutorRepositorioJpa implements PortalTutorRepositorio {

    private final PacienteJpaRepository pacientes;
    private final VacinaJpaRepository vacinas;

    public PortalTutorRepositorioJpa(PacienteJpaRepository pacientes,
                                     VacinaJpaRepository vacinas) {
        this.pacientes = pacientes;
        this.vacinas   = vacinas;
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
        vacinas.deleteByPacienteId(id);
        pacientes.deleteById(id);
    }

    @Override
    public List<Vacina> listarVacinasDoPaciente(String pacienteId) {
        return vacinas.findByPacienteId(pacienteId).stream()
                .map(VacinaJpa::toDomain)
                .sorted((a, b) -> {
                    if (a.data() == null) return 1;
                    if (b.data() == null) return -1;
                    return a.data().compareTo(b.data());
                })
                .toList();
    }

    @Override
    public void salvarVacina(Vacina vacina) {
        vacinas.save(VacinaJpa.fromDomain(vacina));
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
