package br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.OrquestradorEtapasProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.ResultadoEtapa;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

/**
 * Caso de uso que avança o protocolo de inacessibilidade para a próxima etapa
 * (RN 2/4/6). Localiza o protocolo pelo id, delega ao
 * {@link OrquestradorEtapasProtocolo} — que decide qual fase executar conforme
 * a máquina de estados — e devolve o resultado da etapa.
 */
public class ExecutarEtapaProtocoloUseCase {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final OrquestradorEtapasProtocolo orquestrador;

    public ExecutarEtapaProtocoloUseCase(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                         OrquestradorEtapasProtocolo orquestrador) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos é obrigatório.");
        if (orquestrador == null)
            throw new IllegalArgumentException("OrquestradorEtapasProtocolo é obrigatório.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.orquestrador = orquestrador;
    }

    public ResultadoEtapa executar(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        return orquestrador.executarProximaEtapa(protocolo);
    }
}
