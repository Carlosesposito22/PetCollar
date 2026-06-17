package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Competencia;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.StatusCobranca;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IDescontoFaturaPort;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

            proxima = criarProximaFatura(tutorId);
            if (proxima.isEmpty()) {
                log.warn("[DESCONTO-FATURA] Nenhuma fatura encontrada para o Tutor {} — não foi possível aplicar desconto (RN-5).",
                         tutorId.getValor());
                return Optional.empty();
            }
            log.info("[DESCONTO-FATURA] Nova fatura criada para o Tutor {} para aplicação do desconto.", tutorId.getValor());
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

    private Optional<Cobranca> criarProximaFatura(TutorId tutorId) {
        List<Cobranca> historico = cobrancaRepositorio.listarPorTutor(tutorId);
        if (historico.isEmpty()) return Optional.empty();

        Cobranca ultima = historico.stream()
            .max(Comparator.comparing(Cobranca::getVencimento))
            .get();

        LocalDate proximoVencimento = ultima.getVencimento().plusMonths(1);
        Cobranca nova = new Cobranca(
            CobrancaId.gerar(),
            tutorId,
            ultima.getPlanoId(),
            Competencia.de(YearMonth.from(proximoVencimento)),
            ultima.getValorOriginal(),
            null,
            proximoVencimento
        );
        cobrancaRepositorio.salvar(nova);
        return Optional.of(nova);
    }

    @Override
    public boolean metodoPagamentoCoincideComIndicador(TutorId tutorId, String tokenMetodoPagamento) {

        log.debug("[DESCONTO-FATURA] Verificação de método de pagamento não implementada (RN-8). Retornando false.");
        return false;
    }
}
