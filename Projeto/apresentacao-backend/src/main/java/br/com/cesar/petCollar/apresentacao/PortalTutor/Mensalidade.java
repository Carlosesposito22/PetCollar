package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Mensalidade do Plano contratado pelo Tutor (F-07).
 * Juros simples de 0,033% ao dia, calculados dinamicamente a cada leitura.
 */
public class Mensalidade {

    /** Taxa diária de juros simples definida pelo domínio (0,033% a.d.). */
    public static final BigDecimal TAXA_JUROS_DIARIA = new BigDecimal("0.00033");

    private final String id;
    private final String tutorId;
    private final YearMonth competencia;
    private final BigDecimal valorOriginal;
    private final BigDecimal descontoIndicacao;
    private final LocalDate vencimento;
    private LocalDate dataPagamento;

    public Mensalidade(String id, String tutorId, YearMonth competencia,
                       BigDecimal valorOriginal, BigDecimal descontoIndicacao,
                       LocalDate vencimento, LocalDate dataPagamento) {
        this.id = id;
        this.tutorId = tutorId;
        this.competencia = competencia;
        this.valorOriginal = valorOriginal;
        this.descontoIndicacao = descontoIndicacao == null ? BigDecimal.ZERO : descontoIndicacao;
        this.vencimento = vencimento;
        this.dataPagamento = dataPagamento;
    }

    public StatusMensalidade status() {
        if (dataPagamento != null) return StatusMensalidade.PAGO;
        if (vencimento.isBefore(LocalDate.now())) return StatusMensalidade.EM_ATRASO;
        return StatusMensalidade.PENDENTE;
    }

    public int diasAtraso() {
        if (status() != StatusMensalidade.EM_ATRASO) return 0;
        return (int) ChronoUnit.DAYS.between(vencimento, LocalDate.now());
    }

    public BigDecimal juros() {
        int dias = diasAtraso();
        if (dias == 0) return BigDecimal.ZERO;
        return valorOriginal
                .multiply(TAXA_JUROS_DIARIA)
                .multiply(BigDecimal.valueOf(dias))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorAtualizado() {
        return valorOriginal
                .subtract(descontoIndicacao)
                .add(juros())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void marcarPaga() {
        this.dataPagamento = LocalDate.now();
    }

    public String id()                       { return id; }
    public String tutorId()                  { return tutorId; }
    public YearMonth competencia()           { return competencia; }
    public BigDecimal valorOriginal()        { return valorOriginal; }
    public BigDecimal descontoIndicacao()    { return descontoIndicacao; }
    public LocalDate vencimento()            { return vencimento; }
    public LocalDate dataPagamento()         { return dataPagamento; }
}
