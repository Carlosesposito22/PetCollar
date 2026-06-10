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

    /** Histórico de planos finalizados do paciente, mais recente primeiro. */
    List<PlanoNutricional> listarFinalizadosDoPaciente(PacienteId pacienteId);

    /** Todos os planos finalizados do tutor (em todos os pacientes dele). */
    List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId);
}
