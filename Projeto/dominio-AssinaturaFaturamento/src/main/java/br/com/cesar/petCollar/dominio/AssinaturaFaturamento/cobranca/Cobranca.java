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

/**
 * Agregado Cobranca (F-07). Representa uma fatura mensal vinculada a um Tutor e
 * a um Plano contratado. Status é derivado dinamicamente da relação entre
 * {@code vencimento}/{@code dataPagamento} e a data atual, em vez de ser
 * persistido — segue a RN 4 ("recalculado a cada acesso").
 *
 * <p>O cálculo do <strong>valor atualizado</strong> usa uma cadeia de
 * <em>Decorators</em> ({@link CalculadoraValor}), permitindo combinar
 * dinamicamente desconto por indicação (F-04) e juros simples (RN 4) sem
 * inflar a entidade com regras condicionais.
 */
public class Cobranca {

    private final CobrancaId id;
    private final TutorId tutorId;
    private final PlanoId planoId;
    private final Competencia competencia;
    private final BigDecimal valorOriginal;
    private final BigDecimal descontoIndicacao;
    private final LocalDate vencimento;
    private LocalDate dataPagamento;
    /** Juros fixados no momento da quitação — preservam o registro histórico (RN 4). */
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

    /** Construtor de RECONSTRUÇÃO — usado pelos adapters de persistência. */
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

    // ── Status (derivado) ────────────────────────────────────────────────────

    public StatusCobranca status() {
        if (dataPagamento != null) return StatusCobranca.PAGA;
        if (vencimento.isBefore(LocalDate.now())) return StatusCobranca.EM_ATRASO;
        return StatusCobranca.PENDENTE;
    }

    public int diasAtraso() {
        if (status() != StatusCobranca.EM_ATRASO) return 0;
        return (int) ChronoUnit.DAYS.between(vencimento, LocalDate.now());
    }

    // ── Cálculo de valor (Decorator) ─────────────────────────────────────────

    /**
     * Constrói a cadeia de cálculo apropriada para o estado atual da cobrança.
     * Ordem da chain: <strong>ValorBase → Desconto → Juros</strong>, ou seja,
     * desconto é aplicado antes dos juros incidirem (o que faz mais sentido
     * financeiramente: juros incidem sobre o valor já descontado).
     */
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

    /** Valor que o tutor deve hoje (já com juros e descontos aplicados). */
    public BigDecimal valorAtualizado() {
        return montarCalculadora().calcular();
    }

    /**
     * Componente "Juros" do valor atualizado — em registros pagos, devolve os
     * juros que ficaram fixados no momento da quitação (RN 4).
     */
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

    // ── Operações de negócio ─────────────────────────────────────────────────

    /**
     * Registra o pagamento da cobrança. Fixa os juros calculados no momento da
     * quitação (RN 4: "juros são fixados permanentemente no registro").
     */
    public void registrarPagamento() {
        if (status() == StatusCobranca.PAGA)
            throw new IllegalStateException("Cobrança já foi paga.");
        this.jurosFixados = juros();
        this.dataPagamento = LocalDate.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

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
