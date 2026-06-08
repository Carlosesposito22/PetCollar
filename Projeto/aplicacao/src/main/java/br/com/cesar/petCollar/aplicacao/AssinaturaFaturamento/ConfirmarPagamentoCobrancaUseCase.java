package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

/**
 * Caso de uso de confirmação de pagamento de uma cobrança. Reforça a
 * propriedade: a cobrança deve pertencer ao tutor que a requisita. Publica o
 * evento "pagamento_confirmado" (padrão Observer) — quem reage a isso, como a
 * Gamificação avaliando badges de fidelidade, é decidido no wiring da infra,
 * não aqui.
 */
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
            // Comportamento defensivo: trata como não encontrada para não vazar
            // existência de cobranças de outros tutores.
            throw new IllegalArgumentException("Cobrança não encontrada: " + cobrancaId);
        }

        cobranca.registrarPagamento(); // idempotência protegida pela própria entidade
        cobrancaRepositorio.salvar(cobranca);
        publicadorDeEventos.publicar(tutorId, EVENTO_PAGAMENTO_CONFIRMADO);
        return cobranca;
    }
}
