package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IPrescricaoRepositorio {

    void salvar(Prescricao prescricao);

    Optional<Prescricao> buscarPorId(PrescricaoId id);

    Optional<Prescricao> buscarVigenteDoPaciente(PacienteId pacienteId);

    List<Prescricao> listarHistoricoDoPaciente(PacienteId pacienteId);

    List<Prescricao> listarAtivasDoTutor(TutorId tutorId);
}
