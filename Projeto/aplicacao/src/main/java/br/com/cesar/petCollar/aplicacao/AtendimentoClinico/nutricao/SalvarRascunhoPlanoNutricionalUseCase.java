package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.CronogramaTransicao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ObservacaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Caso de uso que cria um novo rascunho ou atualiza o rascunho aberto do
 * paciente. Mantém a invariante "no máximo 1 rascunho aberto por paciente":
 * se já existir um RASCUNHO, ele é atualizado; caso contrário, novo plano.
 */
public class SalvarRascunhoPlanoNutricionalUseCase {

    private final IPlanoNutricionalRepositorio repositorio;

    public SalvarRascunhoPlanoNutricionalUseCase(IPlanoNutricionalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public PlanoNutricional executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");

        PlanoNutricional plano = repositorio
                .buscarRascunhoDoPaciente(entrada.pacienteId)
                .orElseGet(() -> new PlanoNutricional(
                        PlanoNutricionalId.gerar(),
                        entrada.pacienteId, entrada.tutorId, entrada.medicoResponsavel,
                        entrada.parametros));

        plano.alterarParametros(entrada.parametros);
        if (entrada.cronograma != null) plano.alterarCronograma(entrada.cronograma);
        plano.substituirObservacoes(entrada.observacoes);
        plano.vincularRacao(entrada.racaoId); // null aceito (desvincula)
        plano.registrarJustificativaDivergencia(entrada.justificativaDivergencia);

        repositorio.salvar(plano);
        return plano;
    }

    public record Entrada(
            PacienteId pacienteId,
            TutorId tutorId,
            MedicoId medicoResponsavel,
            ParametrosPaciente parametros,
            CronogramaTransicao cronograma,
            List<ObservacaoNutricional> observacoes,
            RacaoId racaoId,
            String justificativaDivergencia
    ) {}
}
