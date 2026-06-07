package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;

/**
 * Fábrica que seleciona a {@link ICalculoProximaDoseStrategy} correta
 * para um {@link TipoProtocolo} (padrão Factory Method).
 *
 * <p>Encapsula o mapeamento enum → estratégia, mantendo o
 * {@link br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService}
 * desacoplado das implementações concretas.
 */
public final class FabricaDeProtocolo {

    private FabricaDeProtocolo() {}

    /**
     * Cria a estratégia adequada ao protocolo.
     *
     * @param tipo         protocolo clínico definido pelo veterinário
     * @param intervaloDias intervalo em dias usado apenas quando {@code tipo == PERSONALIZADO}
     * @return instância de {@link ICalculoProximaDoseStrategy} pronta para uso
     */
    public static ICalculoProximaDoseStrategy criar(TipoProtocolo tipo, Integer intervaloDias) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de protocolo não pode ser nulo.");
        return switch (tipo) {
            case FILHOTE       -> new ProtocoloFilhoteStrategy();
            case REFORCO_ANUAL -> new ProtocoloReforcoAnualStrategy();
            case VIAGEM        -> new ProtocoloViagemStrategy();
            case PERSONALIZADO -> new ProtocoloPersonalizadoStrategy(
                (intervaloDias != null && intervaloDias > 0) ? intervaloDias : 30
            );
        };
    }
}
