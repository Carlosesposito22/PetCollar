package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IPlanoNutricionalRepositorio {

    void salvar(PlanoNutricional plano);

    Optional<PlanoNutricional> buscarPorId(PlanoNutricionalId id);

    /** Rascunho aberto do paciente (no máximo 1 — RN de fluxo). */
    Optional<PlanoNutricional> buscarRascunhoDoPaciente(PacienteId pacienteId);

    /**
     * Plano FINALIZADO vigente do paciente — no máximo 1, pois ao finalizar
     * um novo o anterior é marcado como SUBSTITUIDO. {@link Optional#empty()}
     * quando o paciente nunca recebeu prescrição nutricional.
     */
    Optional<PlanoNutricional> buscarFinalizadoAtivoDoPaciente(PacienteId pacienteId);

    /** Histórico de planos finalizados do paciente, mais recente primeiro. */
    List<PlanoNutricional> listarFinalizadosDoPaciente(PacienteId pacienteId);

    /** Planos vigentes (FINALIZADO ativo) do tutor — um por paciente dele. */
    List<PlanoNutricional> listarAtivosDoTutor(TutorId tutorId);

    /** Todos os planos finalizados do tutor (em todos os pacientes dele). */
    List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId);
}
