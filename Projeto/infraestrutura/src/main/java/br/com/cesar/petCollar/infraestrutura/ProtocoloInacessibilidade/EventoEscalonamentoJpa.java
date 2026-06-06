package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamentoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA da subentidade {@link EventoEscalonamento} (filho do agregado
 * {@link br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade}),
 * registro auditável de cada avanço de nível (RN 7).
 */
@Entity
@Table(name = "eventos_escalonamento")
public class EventoEscalonamentoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String nivel;                  // NivelEscalonamento.name()

    @Column(columnDefinition = "TEXT")
    private String motivo;

    private String responsavelAcionadoId;  // referência cross-agregado, pode ser nula

    @Column(nullable = false)
    private LocalDateTime ocorridoEm;

    protected EventoEscalonamentoJpa() {}

    public static EventoEscalonamentoJpa fromDomain(EventoEscalonamento e) {
        EventoEscalonamentoJpa jpa = new EventoEscalonamentoJpa();
        jpa.id = e.getId().getValor();
        jpa.nivel = e.getNivel().name();
        jpa.motivo = e.getMotivo();
        jpa.responsavelAcionadoId = e.getResponsavelAcionadoId();
        jpa.ocorridoEm = e.getOcorridoEm();
        return jpa;
    }

    public EventoEscalonamento toDomain() {
        return new EventoEscalonamento(
            EventoEscalonamentoId.de(id),
            NivelEscalonamento.valueOf(nivel),
            motivo,
            responsavelAcionadoId,
            ocorridoEm);
    }
}
