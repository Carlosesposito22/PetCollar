package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "doses_vacinais")
public class DoseVacinalJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String cicloId;

    @Column(nullable = false)
    private int doseNumero;

    @Column(nullable = false)
    private LocalDate dataAgendada;

    private LocalDate dataAplicacao;
    private String medico;
    private String lote;

    protected DoseVacinalJpa() {}

    public static DoseVacinalJpa fromDomain(DoseVacinal d, String cicloId) {
        DoseVacinalJpa jpa = new DoseVacinalJpa();
        jpa.id             = d.getId().getValor();
        jpa.cicloId        = cicloId;
        jpa.doseNumero     = d.getDoseNumero();
        jpa.dataAgendada   = d.getDataAgendada();
        jpa.dataAplicacao  = d.getDataAplicacao();
        jpa.medico         = d.getMedico();
        jpa.lote           = d.getLote();
        return jpa;
    }

    public DoseVacinal toDomain() {
        return new DoseVacinal(
            VacinaId.de(id), doseNumero, dataAgendada, dataAplicacao, medico, lote);
    }

    public String getId()     { return id; }
    public String getCicloId(){ return cicloId; }
}
