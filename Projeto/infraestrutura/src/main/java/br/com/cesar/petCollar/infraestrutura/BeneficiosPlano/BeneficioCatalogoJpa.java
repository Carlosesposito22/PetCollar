package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficios_catalogo")
public class BeneficioCatalogoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String planoId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private String periodoRenovacao;

    @Column(nullable = false)
    private int limiteUsosPorPeriodo;

    @Column(nullable = false)
    private int carenciaDias;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    protected BeneficioCatalogoJpa() {}

    public static BeneficioCatalogoJpa fromDomain(BeneficioCatalogo b) {
        BeneficioCatalogoJpa j = new BeneficioCatalogoJpa();
        j.id = b.getId().getValor();
        j.planoId = b.getPlanoId().getValor();
        j.nome = b.getNome();
        j.periodoRenovacao = b.getPeriodoRenovacao().name();
        j.limiteUsosPorPeriodo = b.getLimiteUsosPorPeriodo();
        j.carenciaDias = b.getCarenciaDias();
        j.ativo = b.isAtivo();
        j.criadoEm = b.getCriadoEm();
        j.atualizadoEm = b.getAtualizadoEm();
        return j;
    }

    public BeneficioCatalogo toDomain() {
        return new BeneficioCatalogo(
                BeneficioCatalogoId.de(id),
                PlanoId.de(planoId),
                nome,
                PeriodoRenovacao.valueOf(periodoRenovacao),
                limiteUsosPorPeriodo,
                carenciaDias,
                ativo,
                criadoEm,
                atualizadoEm
        );
    }

    public String getId()       { return id; }
    public String getPlanoId()  { return planoId; }
    public boolean isAtivo()    { return ativo; }
}
