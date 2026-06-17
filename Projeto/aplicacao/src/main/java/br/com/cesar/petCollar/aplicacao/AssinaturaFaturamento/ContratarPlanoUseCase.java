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
        return executar(tutorId, planoId, null);
    }

    public Cobranca executar(TutorId tutorId, PlanoId planoId, BigDecimal percentualDesconto) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");

        if (!cobrancaRepositorio.listarPorTutor(tutorId).isEmpty()) {

            return cobrancaRepositorio.listarPorTutor(tutorId).get(0);
        }

        Plano plano = planoRepositorio.buscarPorId(planoId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + planoId));

        LocalDate hoje = LocalDate.now();
        BigDecimal valor = plano.getMensalidade().getValor();
        BigDecimal descontoAbsoluto = (percentualDesconto != null && percentualDesconto.signum() > 0)
                ? valor.multiply(percentualDesconto)
                : null;

        Cobranca inicial = new Cobranca(
                CobrancaId.gerar(), tutorId, plano.getId(),
                Competencia.de(YearMonth.from(hoje)),
                valor, descontoAbsoluto,
                hoje
        );
        inicial.registrarPagamento();
        cobrancaRepositorio.salvar(inicial);
        return inicial;
    }
}
