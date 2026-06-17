package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

public class ProtocoloViagemStrategy implements ICalculoProximaDoseStrategy {

    private static final int INTERVALO_DIAS = 30;

    @Override
    public LocalDate calcularProximaData(LocalDate dataReferencia) {
        if (dataReferencia == null)
            throw new IllegalArgumentException("Data de referência não pode ser nula.");
        return dataReferencia.plusDays(INTERVALO_DIAS);
    }

    @Override
    public String descricao() {
        return "Protocolo de Viagem — dose a cada " + INTERVALO_DIAS + " dias";
    }

    @Override
    public int intervaloDias() {
        return INTERVALO_DIAS;
    }
}
