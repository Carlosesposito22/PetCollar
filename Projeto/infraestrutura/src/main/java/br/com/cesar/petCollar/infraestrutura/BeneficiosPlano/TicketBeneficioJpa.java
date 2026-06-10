package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.CodigoGUID;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusTicket;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficioId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA do agregado {@link TicketBeneficio}. BeneficioTutorId fica
 * apenas como o valor String do Id (agregado externo, §6.2 do CLAUDE.md);
 * StatusTicket e CodigoGUID são persistidos como String.
 */
@Entity
@Table(name = "tickets_beneficio")
public class TicketBeneficioJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String beneficioTutorId;

    @Column(nullable = false, unique = true)
    private String codigoGUID;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime geradoEm;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    private LocalDateTime apresentadoEm;
    private LocalDateTime utilizadoEm;
    private LocalDateTime expiradoEm;
    private LocalDateTime canceladoEm;

    protected TicketBeneficioJpa() {}

    public static TicketBeneficioJpa fromDomain(TicketBeneficio t) {
        TicketBeneficioJpa j = new TicketBeneficioJpa();
        j.id = t.getId().getValor();
        j.beneficioTutorId = t.getBeneficioTutorId().getValor();
        j.codigoGUID = t.getCodigoGUID().getValor();
        j.status = t.getStatus().name();
        j.geradoEm = t.getGeradoEm();
        j.expiraEm = t.getExpiraEm();
        j.apresentadoEm = t.getApresentadoEm();
        j.utilizadoEm = t.getUtilizadoEm();
        j.expiradoEm = t.getExpiradoEm();
        j.canceladoEm = t.getCanceladoEm();
        return j;
    }

    public TicketBeneficio toDomain() {
        return new TicketBeneficio(
                TicketBeneficioId.de(id),
                BeneficioTutorId.de(beneficioTutorId),
                CodigoGUID.de(codigoGUID),
                StatusTicket.valueOf(status),
                geradoEm,
                expiraEm,
                apresentadoEm,
                utilizadoEm,
                expiradoEm,
                canceladoEm
        );
    }

    public String getId()               { return id; }
    public String getBeneficioTutorId() { return beneficioTutorId; }
    public String getCodigoGUID()       { return codigoGUID; }
    public String getStatus()           { return status; }
}
