package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ciclos_vacinais")
public class CicloVacinalJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String nomeCiclo;

    @Column(nullable = false)
    private int totalDoses;

    @Column(nullable = false)
    private String tipoProtocolo;

    private Integer intervaloDias;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cicloId")
    @OrderBy("doseNumero ASC")
    private List<DoseVacinalJpa> doses = new ArrayList<>();

    protected CicloVacinalJpa() {}

    public static CicloVacinalJpa fromDomain(CicloVacinal c) {
        CicloVacinalJpa jpa = new CicloVacinalJpa();
        jpa.id             = c.getId().getValor();
        jpa.pacienteId     = c.getPacienteId().getValor();
        jpa.nomeCiclo      = c.getNomeCiclo();
        jpa.totalDoses     = c.getTotalDoses();
        jpa.tipoProtocolo  = c.getTipoProtocolo().name();
        jpa.intervaloDias  = c.getIntervaloDias();
        jpa.doses          = c.getDoses().stream()
                              .map(d -> DoseVacinalJpa.fromDomain(d, jpa.id))
                              .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    public CicloVacinal toDomain() {
        List<DoseVacinal> dosesDeReconstrucao = doses.stream()
            .map(DoseVacinalJpa::toDomain)
            .toList();
        return new CicloVacinal(
            VacinaId.de(id),
            PacienteId.de(pacienteId),
            nomeCiclo,
            totalDoses,
            TipoProtocolo.valueOf(tipoProtocolo),
            intervaloDias,
            dosesDeReconstrucao);
    }

    public String getId()        { return id; }
    public String getPacienteId(){ return pacienteId; }
    public String getNomeCiclo() { return nomeCiclo; }
}
