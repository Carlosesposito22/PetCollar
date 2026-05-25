package petCollar.dominio.AtendimentoClinico.relatorio;

import java.util.List;

public class GeracaoEvolucaoComparativaService {

    private final IRelatorioClinicoRepositorio repositorio;

    public GeracaoEvolucaoComparativaService(IRelatorioClinicoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public RelatorioClinico consolidarSinaisVitais(RelatorioClinicoId relatorioId,
                                                    SinaisVitais sinaisVitais) {
        RelatorioClinico relatorio = repositorio.findById(relatorioId);
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório clínico não encontrado com o id informado.");

        relatorio.registrarSinaisVitais(sinaisVitais);
        repositorio.save(relatorio);
        return relatorio;
    }

    public RelatorioClinico gerarEvolucaoComparativa(RelatorioClinicoId relatorioId,
                                                      double pesoAnteriorKg) {
        RelatorioClinico relatorio = repositorio.findById(relatorioId);
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório clínico não encontrado com o id informado.");

        if (relatorio.getSinaisVitais() == null)
            throw new IllegalStateException(
                    "Os sinais vitais do atendimento atual devem ser registrados antes de gerar a evolução comparativa.");

        List<RelatorioClinico> historico = repositorio.findByPacienteId(relatorio.getPacienteId());
        boolean possuiHistorico = historico.stream()
                .anyMatch(r -> !r.getId().equals(relatorioId));

        EvolucaoComparativa evolucao;
        if (!possuiHistorico) {
            evolucao = new EvolucaoComparativa(0.0, "Primeiro atendimento registrado.");
        } else {
            double pesoAtual = relatorio.getSinaisVitais().getPesoKg();
            double variacaoPeso = Math.round((pesoAtual - pesoAnteriorKg) * 100.0) / 100.0;
            String resumo = gerarResumoTextual(variacaoPeso);
            evolucao = new EvolucaoComparativa(variacaoPeso, resumo);
        }

        relatorio.registrarEvolucaoComparativa(evolucao);
        repositorio.save(relatorio);
        return relatorio;
    }

    public EvolucaoComparativa calcularEvolucao(double pesoAtualKg, double pesoAnteriorKg) {
        if (pesoAtualKg <= 0)
            throw new IllegalArgumentException("Peso atual deve ser maior que zero.");
        if (pesoAnteriorKg <= 0)
            throw new IllegalArgumentException("Peso anterior deve ser maior que zero.");

        double variacao = Math.round((pesoAtualKg - pesoAnteriorKg) * 100.0) / 100.0;
        String resumo = gerarResumoTextual(variacao);
        return new EvolucaoComparativa(variacao, resumo);
    }

    private String gerarResumoTextual(double variacaoPeso) {
        if (variacaoPeso > 0)
            return String.format("Paciente apresentou ganho de peso de %.2f kg em relação ao atendimento anterior.", variacaoPeso);
        if (variacaoPeso < 0)
            return String.format("Paciente apresentou perda de peso de %.2f kg em relação ao atendimento anterior.", Math.abs(variacaoPeso));
        return "Paciente manteve o peso estável em relação ao atendimento anterior.";
    }
}
