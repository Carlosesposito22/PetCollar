package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.util.List;
import java.util.Optional;

public interface PortalTutorRepositorio {

    List<Paciente> listarPacientesDoTutor(String tutorId);
    Optional<Paciente> buscarPaciente(String id);
    void salvarPaciente(Paciente paciente);
    void removerPaciente(String id);

    String novoId();
}
