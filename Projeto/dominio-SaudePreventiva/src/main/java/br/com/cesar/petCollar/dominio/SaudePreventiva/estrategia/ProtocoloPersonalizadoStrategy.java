package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

/**
 * Strategy concreta: Protocolo Personalizado.
 * O intervalo em dias é definido pelo médico veterinário no momento da criação
 * do ciclo vacinal, permitindo protocolos específicos não cobertos pelos demais.
 */
public class ProtocoloPersonalizadoStrategy implements ICalculoProximaDoseStrategy {

    private final int intervaloDias;

    public ProtocoloPersonalizadoStrategy(int intervaloDias) {
        if (intervaloDias <= 0)
            throw new IllegalArgumentException("Intervalo de dias deve ser maior que zero.");
        this.intervaloDias = intervaloDias;
    }

    @Override
    public LocalDate calcularProximaData(LocalDate dataReferencia) {
        if (dataReferencia == null)
            throw new IllegalArgumentException("Data de referência não pode ser nula.");
        return dataReferencia.plusDays(intervaloDias);
    }

    @Override
    public String descricao() {
        return "Protocolo Personalizado — dose a cada " + intervaloDias + " dias";
    }

    @Override
    public int intervaloDias() {
        return intervaloDias;
    }
}
