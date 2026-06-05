package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.util.List;
import java.util.Optional;

/**
 * Repositório em memória do Portal do Tutor para Pacientes e Vacinas.
 * F-07 (Assinatura/Financeiro) <strong>não vive mais aqui</strong> — está em
 * {@code dominio-AssinaturaFaturamento} + adapters JPA em {@code infraestrutura}.
 */
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
