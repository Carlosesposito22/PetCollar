package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinico;
import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

/**
 * Interface Strategy para validação de completude do relatório clínico antes
 * da assinatura digital (RN-120 / RN-124).
 *
 * <p>Padrão Strategy: cada tipo de consulta (Rotineiro, Cirúrgico, Preventivo)
 * define seus próprios critérios de preenchimento obrigatório. O
 * {@link petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinicoService}
 * delega a validação à estratégia selecionada pela {@link FabricaDeValidadorRelatorio},
 * permanecendo desacoplado de cada tipo concreto.
 *
 * <p>Implementações: {@link ValidadorRelatorioRotineiroStrategy},
 * {@link ValidadorRelatorioCirurgicoStrategy}, {@link ValidadorRelatorioPreventivStrategy}.
 */
public interface IValidadorRelatorioStrategy {

    /**
     * Valida se o relatório está completo para receber assinatura digital.
     *
     * @param relatorio relatório a ser validado (não nulo)
     * @throws IllegalStateException se algum campo obrigatório para este tipo não estiver preenchido
     */
    void validarParaAssinatura(RelatorioClinico relatorio);

    /** Descrição legível do tipo de relatório para exibição na interface. */
    String descricao();

    /** Tipo de consulta que esta estratégia atende. */
    TipoRelatorio tipo();
}
