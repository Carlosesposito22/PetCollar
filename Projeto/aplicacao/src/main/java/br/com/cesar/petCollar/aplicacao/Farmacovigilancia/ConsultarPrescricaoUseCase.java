package br.com.cesar.petCollar.aplicacao.Farmacovigilancia;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.IPrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ConsultarPrescricaoUseCase {

    private final IPrescricaoRepositorio repositorio;

    public ConsultarPrescricaoUseCase(IPrescricaoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPrescricaoRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public Optional<Prescricao> buscarPorId(PrescricaoId id) {
        return repositorio.buscarPorId(id);
    }

    public Optional<Prescricao> buscarVigenteDoPaciente(PacienteId pacienteId) {
        return repositorio.buscarVigenteDoPaciente(pacienteId);
    }

    public List<Prescricao> listarHistoricoDoPaciente(PacienteId pacienteId) {
        return repositorio.listarHistoricoDoPaciente(pacienteId);
    }

    public List<Prescricao> listarAtivasDoTutor(TutorId tutorId) {
        return repositorio.listarAtivasDoTutor(tutorId);
    }
}
