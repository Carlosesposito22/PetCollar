package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

public class ConsultaStatusProtocoloService {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;

    public ConsultaStatusProtocoloService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
    }

    public VisaoProtocolo montarVisao(AtendimentoId atendimentoId) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");
        ProtocoloInacessibilidade protocolo = protocoloRepositorio
            .buscarAtivoPorAtendimento(atendimentoId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Não há protocolo de inacessibilidade ativo para o atendimento informado."));
        return VisaoProtocolo.de(protocolo);
    }
}
