package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

/**
 * Strategy concreta: Reforço Anual.
 * Aplica intervalo de 12 meses — padrão para vacinas de manutenção
 * (Antirrábica, V10 adulto, Giardíase).
 */
public class ProtocoloReforcoAnualStrategy implements ICalculoProximaDoseStrategy {

    private static final int MESES = 12;

    @Override
    public LocalDate calcularProximaData(LocalDate dataReferencia) {
        if (dataReferencia == null)
            throw new IllegalArgumentException("Data de referência não pode ser nula.");
        return dataReferencia.plusMonths(MESES);
    }

    @Override
    public String descricao() {
        return "Reforço Anual — dose a cada " + MESES + " meses";
    }

    @Override
    public int intervaloDias() {
        return 365;
    }
}
