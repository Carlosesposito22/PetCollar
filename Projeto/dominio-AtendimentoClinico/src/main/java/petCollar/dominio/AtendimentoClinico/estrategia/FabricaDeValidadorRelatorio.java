package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

public final class FabricaDeValidadorRelatorio {

    private FabricaDeValidadorRelatorio() {}

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
