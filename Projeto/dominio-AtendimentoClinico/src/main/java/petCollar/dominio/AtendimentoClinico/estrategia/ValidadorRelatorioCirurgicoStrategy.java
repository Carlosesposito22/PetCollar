package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinico;
import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

public class ValidadorRelatorioCirurgicoStrategy implements IValidadorRelatorioStrategy {

    @Override
    public void validarParaAssinatura(RelatorioClinico relatorio) {
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório não pode ser nulo.");
        if (relatorio.getDiagnosticoTecnico() == null || relatorio.getDiagnosticoTecnico().isBlank())
            throw new IllegalStateException(
                "O diagnóstico técnico é obrigatório para assinar o relatório cirúrgico.");
        if (relatorio.getResumoParaTutor() == null || relatorio.getResumoParaTutor().isBlank())
            throw new IllegalStateException(
                "O resumo para o tutor é obrigatório para assinar o relatório cirúrgico.");
        if (relatorio.getCuidadosPosOperatorios() == null || relatorio.getCuidadosPosOperatorios().isBlank())
            throw new IllegalStateException(
                "Os cuidados pós-operatórios são obrigatórios para assinar o relatório cirúrgico (RN-124).");
        if (relatorio.getTempoRecuperacaoEstimado() == null || relatorio.getTempoRecuperacaoEstimado().isBlank())
            throw new IllegalStateException(
                "O tempo de recuperação estimado é obrigatório para assinar o relatório cirúrgico (RN-124).");
    }

    @Override
    public String descricao() {
        return "Procedimento Cirúrgico — exige diagnóstico, resumo, cuidados pós-operatórios e tempo de recuperação";
    }

    @Override
    public TipoRelatorio tipo() {
        return TipoRelatorio.CIRURGICO;
    }
}
