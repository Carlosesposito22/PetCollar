package br.com.cesar.petCollar.aplicacao.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;

/**
 * Caso de uso de confirmação de conversão de indicação via webhook do gateway
 * de pagamentos (RN-4 / RN-5 / RN-6 / RN-8 / RN-9). Orquestra o fluxo
 * automático do Programa de Indicação delegando ao ProgramaIndicacaoService.
 */
public class ConfirmarConversaoIndicacaoUseCase {

    private final ProgramaIndicacaoService programaIndicacao;

    public ConfirmarConversaoIndicacaoUseCase(ProgramaIndicacaoService programaIndicacao) {
        if (programaIndicacao == null)
            throw new IllegalArgumentException("ProgramaIndicacaoService é obrigatório.");
        this.programaIndicacao = programaIndicacao;
    }

    public void executar(IndicacaoId indicacaoId, String tokenMetodoPagamento) {
        if (indicacaoId == null)
            throw new IllegalArgumentException("Id da indicação é obrigatório.");
        programaIndicacao.confirmarConversao(indicacaoId, tokenMetodoPagamento);
    }
}
