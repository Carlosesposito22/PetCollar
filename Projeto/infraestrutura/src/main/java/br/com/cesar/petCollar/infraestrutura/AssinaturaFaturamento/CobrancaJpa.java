package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Competencia;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA do agregado {@link Cobranca}. Ids de agregados externos (Tutor,
 * Plano) ficam apenas como o valor String do Id, segundo §6.2 do CLAUDE.md.
 * Status NÃO é persistido — é derivado dinamicamente pela entidade de domínio
 * conforme RN 4 ("recalculado a cada acesso").
 */
@Entity
@Table(name = "cobrancas")
public class CobrancaJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String planoId;

    /** Formato yyyy-MM (ex.: 2026-05). */
    @Column(nullable = false, length = 7)
    private String competencia;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoIndicacao;

    @Column(nullable = false)
    private LocalDate vencimento;

    private LocalDate dataPagamento;

    /** Juros fixados no momento da quitação (RN 4). Nulo enquanto não paga. */
    @Column(precision = 10, scale = 2)
    private BigDecimal jurosFixados;

    protected CobrancaJpa() {}

    public static CobrancaJpa fromDomain(Cobranca c) {
        CobrancaJpa j = new CobrancaJpa();
        j.id = c.getId().getValor();
        j.tutorId = c.getTutorId().getValor();
        j.planoId = c.getPlanoId().getValor();
        j.competencia = c.getCompetencia().getValor().toString();
        j.valorOriginal = c.getValorOriginal();
        j.descontoIndicacao = c.getDescontoIndicacao();
        j.vencimento = c.getVencimento();
        j.dataPagamento = c.getDataPagamento();
        j.jurosFixados = c.getJurosFixados();
        return j;
    }

    public Cobranca toDomain() {
        return new Cobranca(
                CobrancaId.de(id),
                TutorId.de(tutorId),
                PlanoId.de(planoId),
                Competencia.de(YearMonth.parse(competencia)),
                valorOriginal,
                descontoIndicacao,
                vencimento,
                dataPagamento,
                jurosFixados
        );
    }

    public String getId()                  { return id; }
    public String getTutorId()             { return tutorId; }
    public String getPlanoId()             { return planoId; }
    public String getCompetencia()         { return competencia; }
    public LocalDate getVencimento()       { return vencimento; }
    public LocalDate getDataPagamento()    { return dataPagamento; }
}
