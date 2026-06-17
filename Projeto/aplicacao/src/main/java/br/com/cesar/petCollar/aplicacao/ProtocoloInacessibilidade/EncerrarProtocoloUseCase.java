package br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

/**
 * Caso de uso de encerramento manual do protocolo de inacessibilidade.
 * Localiza o protocolo, aplica a transição {@code encerrarComSucesso} na
 * máquina de estados do agregado e persiste o resultado.
 */
public class EncerrarProtocoloUseCase {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;

    public EncerrarProtocoloUseCase(IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos é obrigatório.");
        this.protocoloRepositorio = protocoloRepositorio;
    }

    public ProtocoloInacessibilidade executar(ProtocoloId protocoloId, MotivoEncerramento motivo) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");
        if (motivo == null)
            throw new IllegalArgumentException("Motivo de encerramento não pode ser nulo.");
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        protocolo.encerrarComSucesso(motivo);
        protocoloRepositorio.salvar(protocolo);
        return protocolo;
    }
}
