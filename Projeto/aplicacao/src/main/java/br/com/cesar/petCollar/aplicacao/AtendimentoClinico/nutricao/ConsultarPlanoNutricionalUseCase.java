package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Consultas read-only do contexto Nutrição. Usado tanto pelo médico (acessa
 * planos do paciente) quanto pelo tutor (lista de planos finalizados dele).
 */
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

    /** Para o tutor: somente planos ATIVOS (1 por paciente — o vigente). */
    public List<PlanoNutricional> listarAtivosDoTutor(TutorId tutorId) {
        return repositorio.listarAtivosDoTutor(tutorId);
    }

    public List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId) {
        return repositorio.listarFinalizadosDoTutor(tutorId);
    }

    /**
     * Plano vigente do paciente — usado pelo médico para pré-preencher a
     * tela e avisar de substituição antes de assinar uma nova prescrição.
     */
    public Optional<PlanoNutricional> buscarVigenteDoPaciente(PacienteId pacienteId) {
        return repositorio.buscarFinalizadoAtivoDoPaciente(pacienteId);
    }
}
