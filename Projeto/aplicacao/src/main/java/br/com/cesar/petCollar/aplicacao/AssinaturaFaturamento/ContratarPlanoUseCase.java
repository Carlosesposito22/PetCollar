package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Competencia;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.IPlanoRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Caso de uso F-07 — contratação de plano com confirmação imediata do boleto
 * inicial. Cria a primeira cobrança já como PAGA (representa o pagamento de
 * entrada que disparou a ativação da conta).
 *
 * <p>Idempotente: se o tutor já tiver cobranças, não recria — apenas devolve
 * a 1ª existente.
 */
public class ContratarPlanoUseCase {

    private final IPlanoRepositorio planoRepositorio;
    private final ICobrancaRepositorio cobrancaRepositorio;

    public ContratarPlanoUseCase(IPlanoRepositorio planoRepositorio,
                                 ICobrancaRepositorio cobrancaRepositorio) {
        if (planoRepositorio == null)
            throw new IllegalArgumentException("IPlanoRepositorio é obrigatório.");
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        this.planoRepositorio = planoRepositorio;
        this.cobrancaRepositorio = cobrancaRepositorio;
    }

    public Cobranca executar(TutorId tutorId, PlanoId planoId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");

        if (!cobrancaRepositorio.listarPorTutor(tutorId).isEmpty()) {
            // Idempotente: tutor já contratou — devolve a primeira existente.
            return cobrancaRepositorio.listarPorTutor(tutorId).get(0);
        }

        Plano plano = planoRepositorio.buscarPorId(planoId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + planoId));

        LocalDate hoje = LocalDate.now();
        BigDecimal valor = plano.getMensalidade().getValor();

        Cobranca inicial = new Cobranca(
                CobrancaId.gerar(), tutorId, plano.getId(),
                Competencia.de(YearMonth.from(hoje)),
                valor, null,
                hoje
        );
        inicial.registrarPagamento(); // boleto inicial entra já pago
        cobrancaRepositorio.salvar(inicial);
        return inicial;
    }
}
