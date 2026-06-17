package br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.VisaoProtocolo;

public class ConsultarStatusProtocoloUseCase {

    private final ConsultaStatusProtocoloService statusService;

    public ConsultarStatusProtocoloUseCase(ConsultaStatusProtocoloService statusService) {
        if (statusService == null)
            throw new IllegalArgumentException("ConsultaStatusProtocoloService é obrigatório.");
        this.statusService = statusService;
    }

    public VisaoProtocolo executar(AtendimentoId atendimentoId) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");
        return statusService.montarVisao(atendimentoId);
    }
}
