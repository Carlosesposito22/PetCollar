package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

public class ConfirmarPagamentoCobrancaUseCase {

    public static final String EVENTO_PAGAMENTO_CONFIRMADO = "pagamento_confirmado";

    private final ICobrancaRepositorio cobrancaRepositorio;
    private final PublicadorDeEventosDoTutor publicadorDeEventos;

    public ConfirmarPagamentoCobrancaUseCase(ICobrancaRepositorio cobrancaRepositorio,
                                             PublicadorDeEventosDoTutor publicadorDeEventos) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        if (publicadorDeEventos == null)
            throw new IllegalArgumentException("PublicadorDeEventosDoTutor é obrigatório.");
        this.cobrancaRepositorio = cobrancaRepositorio;
        this.publicadorDeEventos = publicadorDeEventos;
    }

    public Cobranca executar(TutorId tutorId, CobrancaId cobrancaId) {
        if (tutorId == null)   throw new IllegalArgumentException("TutorId é obrigatório.");
        if (cobrancaId == null) throw new IllegalArgumentException("CobrancaId é obrigatório.");

        Cobranca cobranca = cobrancaRepositorio.buscarPorId(cobrancaId)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada: " + cobrancaId));

        if (!cobranca.getTutorId().equals(tutorId)) {

            throw new IllegalArgumentException("Cobrança não encontrada: " + cobrancaId);
        }

        cobranca.registrarPagamento();
        cobrancaRepositorio.salvar(cobranca);
        publicadorDeEventos.publicar(tutorId, EVENTO_PAGAMENTO_CONFIRMADO);
        return cobranca;
    }
}
