package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import java.time.LocalDate;

/**
 * Interface Strategy para cálculo preditivo da próxima dose vacinal (RN-075, RN-082).
 *
 * <p>Padrão Strategy: define o contrato de um algoritmo de cálculo intercambiável.
 * Cada implementação encapsula o intervalo biológico de um protocolo clínico diferente,
 * permitindo que {@link br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal}
 * calcule previsões sem conhecer o protocolo concreto.
 *
 * <p>Implementações: {@link ProtocoloFilhoteStrategy}, {@link ProtocoloReforcoAnualStrategy},
 * {@link ProtocoloViagemStrategy}, {@link ProtocoloPersonalizadoStrategy}.
 * Selecionadas via {@link FabricaDeProtocolo}.
 */
public interface ICalculoProximaDoseStrategy {

    /**
     * Calcula a data prevista para a próxima dose a partir de uma data de referência.
     *
     * @param dataReferencia data da última dose aplicada ou agendada (não nula)
     * @return data prevista para a próxima dose
     */
    LocalDate calcularProximaData(LocalDate dataReferencia);

    /** Descrição legível do protocolo para exibição na interface. */
    String descricao();

    /** Intervalo em dias que esta estratégia aplica. */
    int intervaloDias();
}
