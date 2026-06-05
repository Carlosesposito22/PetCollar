package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Caso de uso de confirmação de pagamento de uma cobrança. Reforça a
 * propriedade: a cobrança deve pertencer ao tutor que a requisita.
 */
public class ConfirmarPagamentoCobrancaUseCase {

    private final ICobrancaRepositorio cobrancaRepositorio;

    public ConfirmarPagamentoCobrancaUseCase(ICobrancaRepositorio cobrancaRepositorio) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        this.cobrancaRepositorio = cobrancaRepositorio;
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
        return cobranca;
    }
}
