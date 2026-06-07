package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

/**
 * Strategy concreta: Ciclo de Filhote.
 * Aplica intervalo de 21 dias entre doses — padrão para vacinas polivalentes
 * (V10, V8) durante o ciclo primário de filhotes.
 */
public class ProtocoloFilhoteStrategy implements ICalculoProximaDoseStrategy {

    private static final int INTERVALO_DIAS = 21;

    @Override
    public LocalDate calcularProximaData(LocalDate dataReferencia) {
        if (dataReferencia == null)
            throw new IllegalArgumentException("Data de referência não pode ser nula.");
        return dataReferencia.plusDays(INTERVALO_DIAS);
    }

    @Override
    public String descricao() {
        return "Ciclo de Filhote — dose a cada " + INTERVALO_DIAS + " dias";
    }

    @Override
    public int intervaloDias() {
        return INTERVALO_DIAS;
    }
}
