package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import java.util.ArrayList;
import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao.ComparacaoEvolutivaNutricionalService;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao.EvolucaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.IPlanoNutricionalRepositorio;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

/**
 * Caso de uso: para um paciente, devolve o histórico completo de planos
 * finalizados + a lista de {@link EvolucaoNutricional} entre cada par
 * sequencial (do mais antigo para o mais novo).
 *
 * <p>A UI usa o histórico para o mini-gráfico e os deltas para o card
 * "Evolução desde o último plano".
 */
public class CompararEvolucaoNutricionalUseCase {

    private final IPlanoNutricionalRepositorio repositorio;
    private final ComparacaoEvolutivaNutricionalService servico;

    public CompararEvolucaoNutricionalUseCase(IPlanoNutricionalRepositorio repositorio,
                                              ComparacaoEvolutivaNutricionalService servico) {
        if (repositorio == null)
            throw new IllegalArgumentException("IPlanoNutricionalRepositorio é obrigatório.");
        if (servico == null)
            throw new IllegalArgumentException("ComparacaoEvolutivaNutricionalService é obrigatório.");
        this.repositorio = repositorio;
        this.servico = servico;
    }

    public Resultado executar(PacienteId pacienteId) {
        if (pacienteId == null) throw new IllegalArgumentException("PacienteId é obrigatório.");

        // Repositório devolve do mais recente para o mais antigo — invertemos
        // para calcular a evolução cronológica.
        List<PlanoNutricional> historico = new ArrayList<>(
                repositorio.listarFinalizadosDoPaciente(pacienteId));
        java.util.Collections.reverse(historico);

        List<EvolucaoNutricional> evolucoes = new ArrayList<>();
        for (int i = 1; i < historico.size(); i++) {
            evolucoes.add(servico.comparar(historico.get(i - 1), historico.get(i)));
        }
        return new Resultado(historico, evolucoes);
    }

    public record Resultado(List<PlanoNutricional> historico, List<EvolucaoNutricional> evolucoes) {}
}
