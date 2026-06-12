package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IPrescricaoRepositorio {

    void salvar(Prescricao prescricao);

    Optional<Prescricao> buscarPorId(PrescricaoId id);

    /** Prescrição FINALIZADA vigente do paciente — no máximo 1. */
    Optional<Prescricao> buscarVigenteDoPaciente(PacienteId pacienteId);

    /** Histórico completo (FINALIZADA + SUBSTITUIDA), mais recente primeiro. */
    List<Prescricao> listarHistoricoDoPaciente(PacienteId pacienteId);

    /** Prescrições vigentes (uma por paciente) de todos os pets do tutor. */
    List<Prescricao> listarAtivasDoTutor(TutorId tutorId);
}
