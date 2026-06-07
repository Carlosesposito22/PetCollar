package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

/**
 * Protocolo clínico que determina qual estratégia de cálculo
 * será usada para prever a próxima dose (RN-075, RN-082).
 * Cada valor corresponde a uma implementação de {@link br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.ICalculoProximaDoseStrategy}.
 */
public enum TipoProtocolo {
    /** Ciclo de filhote — doses a cada 21 dias (ex.: V10, V8). */
    FILHOTE,
    /** Reforço anual — dose a cada 12 meses (ex.: Antirrábica, V10 adulto). */
    REFORCO_ANUAL,
    /** Protocolo de viagem — doses a cada 30 dias. */
    VIAGEM,
    /** Intervalo personalizado definido pelo médico veterinário. */
    PERSONALIZADO
}
