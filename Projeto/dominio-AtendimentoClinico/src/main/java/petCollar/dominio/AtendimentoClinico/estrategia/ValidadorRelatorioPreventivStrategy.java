package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinico;
import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

/**
 * Strategy concreta: Consulta Preventiva (check-up, vacinação, revisão de rotina).
 * Exige apenas o resumo para o tutor; diagnóstico é opcional para atendimentos
 * sem achado patológico significativo.
 */
public class ValidadorRelatorioPreventivStrategy implements IValidadorRelatorioStrategy {

    @Override
    public void validarParaAssinatura(RelatorioClinico relatorio) {
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório não pode ser nulo.");
        if (relatorio.getResumoParaTutor() == null || relatorio.getResumoParaTutor().isBlank())
            throw new IllegalStateException(
                "O resumo para o tutor é obrigatório para assinar o relatório de consulta preventiva.");
    }

    @Override
    public String descricao() {
        return "Consulta Preventiva — exige resumo para o tutor";
    }

    @Override
    public TipoRelatorio tipo() {
        return TipoRelatorio.PREVENTIVO;
    }
}
