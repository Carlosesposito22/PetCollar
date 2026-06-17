package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo.CalculadoraValor;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo.DescontoIndicacaoDecorator;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo.JurosSimplesDecorator;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo.ValorBase;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class Cobranca {

    private final CobrancaId id;
    private final TutorId tutorId;
    private final PlanoId planoId;
    private final Competencia competencia;
    private final BigDecimal valorOriginal;
    private final BigDecimal descontoIndicacao;
    private final LocalDate vencimento;
    private LocalDate dataPagamento;

    private BigDecimal jurosFixados;

    public Cobranca(CobrancaId id, TutorId tutorId, PlanoId planoId,
                    Competencia competencia, BigDecimal valorOriginal,
                    BigDecimal descontoIndicacao, LocalDate vencimento) {
        if (id == null)             throw new IllegalArgumentException("Id da cobrança não pode ser nulo.");
        if (tutorId == null)        throw new IllegalArgumentException("TutorId não pode ser nulo.");
        if (planoId == null)        throw new IllegalArgumentException("PlanoId não pode ser nulo.");
        if (competencia == null)    throw new IllegalArgumentException("Competência não pode ser nula.");
        if (valorOriginal == null || valorOriginal.signum() <= 0)
            throw new IllegalArgumentException("Valor original deve ser maior que zero.");
        if (vencimento == null)     throw new IllegalArgumentException("Vencimento não pode ser nulo.");

        this.id = id;
        this.tutorId = tutorId;
        this.planoId = planoId;
        this.competencia = competencia;
        this.valorOriginal = valorOriginal;
        this.descontoIndicacao = descontoIndicacao == null ? BigDecimal.ZERO : descontoIndicacao;
        this.vencimento = vencimento;
    }

    public Cobranca(CobrancaId id, TutorId tutorId, PlanoId planoId,
                    Competencia competencia, BigDecimal valorOriginal,
                    BigDecimal descontoIndicacao, LocalDate vencimento,
                    LocalDate dataPagamento, BigDecimal jurosFixados) {
        this.id = id;
        this.tutorId = tutorId;
        this.planoId = planoId;
        this.competencia = competencia;
        this.valorOriginal = valorOriginal;
        this.descontoIndicacao = descontoIndicacao == null ? BigDecimal.ZERO : descontoIndicacao;
        this.vencimento = vencimento;
        this.dataPagamento = dataPagamento;
        this.jurosFixados = jurosFixados;
    }

    public StatusCobranca status() {
        if (dataPagamento != null) return StatusCobranca.PAGA;
        if (vencimento.isBefore(LocalDate.now())) return StatusCobranca.EM_ATRASO;
        return StatusCobranca.PENDENTE;
    }

    public int diasAtraso() {
        if (status() != StatusCobranca.EM_ATRASO) return 0;
        return (int) ChronoUnit.DAYS.between(vencimento, LocalDate.now());
    }

    public CalculadoraValor montarCalculadora() {
        CalculadoraValor calc = new ValorBase(valorOriginal);
        if (descontoIndicacao.signum() > 0) {
            calc = new DescontoIndicacaoDecorator(calc, descontoIndicacao);
        }
        if (status() == StatusCobranca.EM_ATRASO) {
            calc = new JurosSimplesDecorator(calc, diasAtraso());
        }
        return calc;
    }

    public BigDecimal valorAtualizado() {
        return montarCalculadora().calcular();
    }

    public BigDecimal juros() {
        if (status() == StatusCobranca.PAGA) {
            return jurosFixados == null ? BigDecimal.ZERO : jurosFixados;
        }
        if (status() != StatusCobranca.EM_ATRASO) return BigDecimal.ZERO;
        return new JurosSimplesDecorator(
                descontoIndicacao.signum() > 0
                        ? new DescontoIndicacaoDecorator(new ValorBase(valorOriginal), descontoIndicacao)
                        : new ValorBase(valorOriginal),
                diasAtraso()
        ).apenasJuros();
    }

    public void registrarPagamento() {
        if (status() == StatusCobranca.PAGA)
            throw new IllegalStateException("Cobrança já foi paga.");
        this.jurosFixados = juros();
        this.dataPagamento = LocalDate.now();
    }

    public CobrancaId getId()              { return id; }
    public TutorId getTutorId()            { return tutorId; }
    public PlanoId getPlanoId()            { return planoId; }
    public Competencia getCompetencia()    { return competencia; }
    public BigDecimal getValorOriginal()   { return valorOriginal; }
    public BigDecimal getDescontoIndicacao() { return descontoIndicacao; }
    public LocalDate getVencimento()       { return vencimento; }
    public LocalDate getDataPagamento()    { return dataPagamento; }
    public BigDecimal getJurosFixados()    { return jurosFixados; }
}
