package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.math.BigDecimal;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.ValorMensalidade;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA do agregado {@link Plano}. Id é persistido como String
 * (valor do PlanoId, gerado no domínio); valor monetário como NUMERIC(10,2).
 */
@Entity
@Table(name = "planos")
public class PlanoJpa {

    @Id
    private String id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensalidade;

    protected PlanoJpa() {}

    private PlanoJpa(String id, String nome, BigDecimal valorMensalidade) {
        this.id = id;
        this.nome = nome;
        this.valorMensalidade = valorMensalidade;
    }

    public static PlanoJpa fromDomain(Plano plano) {
        return new PlanoJpa(
                plano.getId().getValor(),
                plano.getNome(),
                plano.getMensalidade().getValor()
        );
    }

    public Plano toDomain() {
        return new Plano(
                PlanoId.de(id),
                nome,
                ValorMensalidade.de(valorMensalidade)
        );
    }

    public String getId()                  { return id; }
    public String getNome()                { return nome; }
    public BigDecimal getValorMensalidade(){ return valorMensalidade; }
}
