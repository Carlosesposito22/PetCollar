package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

public interface ICalculoProximaDoseStrategy {

    LocalDate calcularProximaData(LocalDate dataReferencia);

    String descricao();

    int intervaloDias();
}
