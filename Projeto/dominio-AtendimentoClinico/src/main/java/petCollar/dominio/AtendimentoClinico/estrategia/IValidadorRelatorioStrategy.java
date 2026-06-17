package petCollar.dominio.AtendimentoClinico.estrategia;

import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinico;
import petCollar.dominio.AtendimentoClinico.relatorio.TipoRelatorio;

public interface IValidadorRelatorioStrategy {

    void validarParaAssinatura(RelatorioClinico relatorio);

    String descricao();

    TipoRelatorio tipo();
}
