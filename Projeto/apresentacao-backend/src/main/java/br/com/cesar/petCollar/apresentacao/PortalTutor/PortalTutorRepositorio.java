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

    // Financeiro
    List<Mensalidade> listarMensalidadesDoTutor(String tutorId);
    Optional<Mensalidade> buscarMensalidade(String id);
    void salvarMensalidade(Mensalidade mensalidade);
    Plano planoDoTutor(String tutorId);

    String novoId();
}
