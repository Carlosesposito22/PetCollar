package br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;

/**
 * Caso de uso de ativação manual do protocolo de inacessibilidade (RN 1/12).
 * Delega ao {@link AtivacaoProtocoloService} preservando a idempotência: se já
 * existe um protocolo ativo para o atendimento, o mesmo é retornado sem criar
 * um duplicado.
 */
public class AtivarProtocoloUseCase {

    private final AtivacaoProtocoloService ativacaoService;

    public AtivarProtocoloUseCase(AtivacaoProtocoloService ativacaoService) {
        if (ativacaoService == null)
            throw new IllegalArgumentException("AtivacaoProtocoloService é obrigatório.");
        this.ativacaoService = ativacaoService;
    }

    public ProtocoloInacessibilidade executar(AtendimentoId atendimentoId) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");
        return ativacaoService.ativarManualmente(atendimentoId);
    }
}
