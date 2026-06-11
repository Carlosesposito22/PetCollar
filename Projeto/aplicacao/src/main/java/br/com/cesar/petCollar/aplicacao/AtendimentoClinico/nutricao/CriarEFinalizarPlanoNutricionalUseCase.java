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
 * Caso de uso atômico: cria o plano nutricional já com todos os dados +
 * finaliza imediatamente com a assinatura digital. Substitui o fluxo de
 * dois passos (Salvar Rascunho → Finalizar) pelo qual o médico só vê um
 * botão "Finalizar e Assinar". Se a assinatura falhar a validação, nada
 * é persistido — o agregado nunca chega a ir pro banco em estado parcial.
 */
public class CriarEFinalizarPlanoNutricionalUseCase {

    private final IPlanoNutricionalRepositorio repositorio;

    public CriarEFinalizarPlanoNutricionalUseCase(IPlanoNutricionalRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        this.repositorio = repositorio;
    }

    public PlanoNutricional executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");

        // Se há um plano FINALIZADO vigente para este paciente, ele será
        // substituído pelo novo (regra 1-ativo-por-paciente). O anterior
        // permanece no banco como SUBSTITUIDO para auditoria e histórico.
        repositorio.buscarFinalizadoAtivoDoPaciente(entrada.pacienteId)
                .ifPresent(anterior -> {
                    anterior.marcarComoSubstituido();
                    repositorio.salvar(anterior);
                });

        PlanoNutricional plano = new PlanoNutricional(
                PlanoNutricionalId.gerar(),
                entrada.pacienteId, entrada.tutorId, entrada.medicoResponsavel,
                entrada.parametros);

        if (entrada.cronograma != null) plano.alterarCronograma(entrada.cronograma);
        plano.substituirObservacoes(entrada.observacoes);
        plano.vincularRacao(entrada.racaoId);
        plano.registrarJustificativaDivergencia(entrada.justificativaDivergencia);
        plano.finalizar(entrada.medicoResponsavel, entrada.imagemAssinaturaBase64);

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
            String justificativaDivergencia,
            String imagemAssinaturaBase64
    ) {}
}
