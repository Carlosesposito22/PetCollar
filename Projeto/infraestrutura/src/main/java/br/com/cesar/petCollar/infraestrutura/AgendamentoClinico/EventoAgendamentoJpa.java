package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.EventoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.TipoEventoAgendamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_agendamento")
public class EventoAgendamentoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime ocorridoEm;

    @Column(columnDefinition = "TEXT")
    private String detalhe;

    protected EventoAgendamentoJpa() {}

    public static EventoAgendamentoJpa fromDomain(EventoAgendamento e) {
        EventoAgendamentoJpa jpa = new EventoAgendamentoJpa();
        jpa.tipo = e.getTipo().name();
        jpa.ocorridoEm = e.getOcorridoEm();
        jpa.detalhe = e.getDetalhe();
        return jpa;
    }

    public EventoAgendamento toDomain() {
        return new EventoAgendamento(TipoEventoAgendamento.valueOf(tipo), ocorridoEm, detalhe);
    }
}
