package br.com.cesar.petCollar.apresentacao.PortalTutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "vacinas")
public class VacinaJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String ciclo;

    private Integer doseNumero;
    private Integer totalDoses;

    @Column(nullable = false)
    private boolean aplicada;

    private LocalDate data;
    private String medico;
    private String lote;

    protected VacinaJpa() {}

    public static VacinaJpa fromDomain(Vacina v) {
        VacinaJpa jpa = new VacinaJpa();
        jpa.id          = v.id();
        jpa.pacienteId  = v.pacienteId();
        jpa.ciclo       = v.ciclo();
        jpa.doseNumero  = v.doseNumero();
        jpa.totalDoses  = v.totalDoses();
        jpa.aplicada    = v.aplicada();
        jpa.data        = v.data();
        jpa.medico      = v.medico();
        jpa.lote        = v.lote();
        return jpa;
    }

    public Vacina toDomain() {
        return new Vacina(id, pacienteId, ciclo, doseNumero, totalDoses,
                aplicada, data, medico, lote);
    }

    public String getId()        { return id; }
    public String getPacienteId(){ return pacienteId; }
}
