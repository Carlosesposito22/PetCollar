package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IPlanoNutricionalRepositorio {

    void salvar(PlanoNutricional plano);

    Optional<PlanoNutricional> buscarPorId(PlanoNutricionalId id);

    Optional<PlanoNutricional> buscarRascunhoDoPaciente(PacienteId pacienteId);

    Optional<PlanoNutricional> buscarFinalizadoAtivoDoPaciente(PacienteId pacienteId);

    List<PlanoNutricional> listarFinalizadosDoPaciente(PacienteId pacienteId);

    List<PlanoNutricional> listarAtivosDoTutor(TutorId tutorId);

    List<PlanoNutricional> listarFinalizadosDoTutor(TutorId tutorId);

    long contarPlanosComRacao(RacaoId racaoId);
}
