package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.StatusCobranca;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IDescontoFaturaPort;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

/**
 * Adapter cross-context: aplica desconto de indicação (RN-5) na próxima fatura em aberto
 * do Tutor indicador, acessando o repositório de cobranças de AssinaturaFaturamento.
 * A verificação de método de pagamento retorna sempre {@code false} enquanto o
 * bounded context de Pagamentos não expuser a consulta real (RN-8 — placeholder).
 */
@Component
public class DescontoFaturaAdapter implements IDescontoFaturaPort {

    private static final Logger log = LoggerFactory.getLogger(DescontoFaturaAdapter.class);

    private final ICobrancaRepositorio cobrancaRepositorio;

    public DescontoFaturaAdapter(ICobrancaRepositorio cobrancaRepositorio) {
        this.cobrancaRepositorio = cobrancaRepositorio;
    }

    @Override
    public Optional<String> aplicarDescontoProximaFatura(TutorId tutorId, BigDecimal percentual) {
        Optional<Cobranca> proxima = cobrancaRepositorio.listarPorTutor(tutorId).stream()
            .filter(c -> c.status() == StatusCobranca.PENDENTE
                      || c.status() == StatusCobranca.EM_ATRASO)
            .min(Comparator.comparing(Cobranca::getVencimento));

        if (proxima.isEmpty()) {
            log.warn("[DESCONTO-FATURA] Nenhuma fatura em aberto para o Tutor {} (RN-5).",
                     tutorId.getValor());
            return Optional.empty();
        }

        Cobranca cobranca = proxima.get();
        BigDecimal desconto = cobranca.getValorOriginal().multiply(percentual);
        CobrancaId id = cobranca.getId();

        Cobranca comDesconto = new Cobranca(
            id, cobranca.getTutorId(), cobranca.getPlanoId(),
            cobranca.getCompetencia(),
            cobranca.getValorOriginal(),
            desconto,
            cobranca.getVencimento(),
            cobranca.getDataPagamento(),
            cobranca.getJurosFixados()
        );
        cobrancaRepositorio.salvar(comDesconto);
        log.info("[DESCONTO-FATURA] Desconto de {}% aplicado na cobrança {} do Tutor {} (RN-5).",
                 percentual.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(),
                 id.getValor(), tutorId.getValor());
        return Optional.of(id.getValor());
    }

    @Override
    public boolean metodoPagamentoCoincideComIndicador(TutorId tutorId, String tokenMetodoPagamento) {
        // Placeholder: a verificação real exigiria integração com o gateway de pagamentos.
        // Retorna false (sem bloqueio) até a integração ser implementada.
        log.debug("[DESCONTO-FATURA] Verificação de método de pagamento não implementada (RN-8). Retornando false.");
        return false;
    }
}
