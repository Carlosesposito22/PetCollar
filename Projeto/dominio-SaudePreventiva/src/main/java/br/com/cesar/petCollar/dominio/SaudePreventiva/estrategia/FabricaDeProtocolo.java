package br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;

public final class FabricaDeProtocolo {

    private FabricaDeProtocolo() {}

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
