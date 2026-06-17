package br.com.cesar.petCollar.aplicacao.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;

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
