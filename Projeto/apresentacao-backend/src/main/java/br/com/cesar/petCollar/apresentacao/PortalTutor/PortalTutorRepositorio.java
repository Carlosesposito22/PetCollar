package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.util.List;
import java.util.Optional;

public interface PortalTutorRepositorio {

    // Pacientes
    List<Paciente> listarPacientesDoTutor(String tutorId);
    Optional<Paciente> buscarPaciente(String id);
    void salvarPaciente(Paciente paciente);
    void removerPaciente(String id);

    // Vacinas
    List<Vacina> listarVacinasDoPaciente(String pacienteId);
    void salvarVacina(Vacina vacina);

    String novoId();
}
