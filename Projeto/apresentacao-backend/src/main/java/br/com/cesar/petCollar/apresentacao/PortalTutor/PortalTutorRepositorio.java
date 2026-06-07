package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.util.List;
import java.util.Optional;

/**
 * Repositório do Portal do Tutor — exclusivamente para Pacientes.
 * Ciclos vacinais agora são gerenciados por {@code ICicloVacinalRepositorio}
 * no módulo {@code dominio-SaudePreventiva}.
 */
public interface PortalTutorRepositorio {

    // Pacientes
    List<Paciente> listarPacientesDoTutor(String tutorId);
    Optional<Paciente> buscarPaciente(String id);
    void salvarPaciente(Paciente paciente);
    void removerPaciente(String id);

    String novoId();
}
