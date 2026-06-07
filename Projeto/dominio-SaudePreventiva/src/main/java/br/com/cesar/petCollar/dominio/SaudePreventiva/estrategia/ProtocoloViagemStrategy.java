package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

/**
 * Strategy concreta: Protocolo de Viagem.
 * Aplica intervalo de 30 dias — usado em viagens internacionais ou regiões
 * de risco onde a imunização prévia exige séries de doses mais curtas.
 */
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
