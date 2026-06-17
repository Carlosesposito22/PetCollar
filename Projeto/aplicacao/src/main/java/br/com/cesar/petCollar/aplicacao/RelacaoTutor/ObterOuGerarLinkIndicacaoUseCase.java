package br.com.cesar.petCollar.aplicacao.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ObterOuGerarLinkIndicacaoUseCase {

    private final ProgramaIndicacaoService programaIndicacao;

    public ObterOuGerarLinkIndicacaoUseCase(ProgramaIndicacaoService programaIndicacao) {
        if (programaIndicacao == null)
            throw new IllegalArgumentException("ProgramaIndicacaoService é obrigatório.");
        this.programaIndicacao = programaIndicacao;
    }

    public LinkIndicacao executar(TutorId tutorId, boolean contaAtiva) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId é obrigatório.");
        return programaIndicacao.obterOuGerarLink(tutorId, contaAtiva);
    }
}
