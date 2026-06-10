package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

/**
 * Fábrica que seleciona a {@link IValidadorRelatorioStrategy} adequada para um
 * {@link TipoRelatorio} (padrão Factory Method).
 *
 * <p>Encapsula o mapeamento tipo → estratégia, mantendo o
 * {@link petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinicoService}
 * desacoplado das implementações concretas de validação.
 */
public final class FabricaDeValidadorRelatorio {

    private FabricaDeValidadorRelatorio() {}

    /**
     * Cria a estratégia de validação adequada ao tipo de relatório.
     *
     * @param tipo tipo de consulta associado ao relatório (não nulo)
     * @return instância de {@link IValidadorRelatorioStrategy} pronta para uso
     */
    public static IValidadorRelatorioStrategy criar(TipoRelatorio tipo) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de relatório não pode ser nulo.");
        return switch (tipo) {
            case ROTINEIRO  -> new ValidadorRelatorioRotineiroStrategy();
            case CIRURGICO  -> new ValidadorRelatorioCirurgicoStrategy();
            case PREVENTIVO -> new ValidadorRelatorioPreventivStrategy();
        };
    }
}
