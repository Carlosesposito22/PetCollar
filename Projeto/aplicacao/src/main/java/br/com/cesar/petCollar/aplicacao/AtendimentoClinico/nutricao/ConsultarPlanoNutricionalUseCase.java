package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ConsultarPlanoNutricionalUseCase {

    private final IPlanoNutricionalRepositorio repositorio;

    public ConsultarPlanoNutricionalUseCase(IPlanoNutricionalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public Optional<PlanoNutricional> buscarPorId(PlanoNutricionalId id) {
        return repositorio.buscarPorId(id);
    }

    public Optional<PlanoNutricional> buscarRascunhoDoPaciente(PacienteId pacienteId) {
        return repositorio.buscarRascunhoDoPaciente(pacienteId);
    }

    public List<PlanoNutricional> listarFinalizadosDoPaciente(PacienteId pacienteId) {
        return repositorio.listarFinalizadosDoPaciente(pacienteId);
    }

    public List<PlanoNutricional> listarAtivosDoTutor(TutorId tutorId) {
        return repositorio.listarAtivosDoTutor(tutorId);
    }

    public List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId) {
        return repositorio.listarFinalizadosDoTutor(tutorId);
    }

    public Optional<PlanoNutricional> buscarVigenteDoPaciente(PacienteId pacienteId) {
        return repositorio.buscarFinalizadoAtivoDoPaciente(pacienteId);
    }
}
