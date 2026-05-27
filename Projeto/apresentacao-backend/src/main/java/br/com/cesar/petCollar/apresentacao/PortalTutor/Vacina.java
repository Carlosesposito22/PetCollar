package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;

/**
 * Uma dose de vacina na carteira de um Paciente.
 * O {@code ciclo} agrupa doses da mesma vacina (ex.: "V10"); {@code doseNumero}/{@code totalDoses}
 * descrevem a posição no ciclo. O status é derivado de {@link #status()}.
 */
public class Vacina {

    private final String id;
    private final String pacienteId;
    private String ciclo;          // ex.: "V10", "Antirrábica", "Giardíase"
    private Integer doseNumero;    // ex.: 1, 2, 3 (null = dose única)
    private Integer totalDoses;    // ex.: 3      (null = dose única)
    private boolean aplicada;
    private LocalDate data;        // data de aplicação (se aplicada) ou agendamento (se pendente)
    private String medico;
    private String lote;

    public Vacina(String id, String pacienteId, String ciclo,
                  Integer doseNumero, Integer totalDoses,
                  boolean aplicada, LocalDate data, String medico, String lote) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.ciclo = ciclo;
        this.doseNumero = doseNumero;
        this.totalDoses = totalDoses;
        this.aplicada = aplicada;
        this.data = data;
        this.medico = medico;
        this.lote = lote;
    }

    public StatusVacina status() {
        if (aplicada) return StatusVacina.APLICADA;
        if (data != null && data.isBefore(LocalDate.now())) return StatusVacina.EM_ATRASO;
        return StatusVacina.PENDENTE;
    }

    public String rotulo() {
        if (doseNumero != null && totalDoses != null) {
            return ciclo + " - Dose " + doseNumero + "/" + totalDoses;
        }
        return ciclo;
    }

    public String id()           { return id; }
    public String pacienteId()   { return pacienteId; }
    public String ciclo()        { return ciclo; }
    public Integer doseNumero()  { return doseNumero; }
    public Integer totalDoses()  { return totalDoses; }
    public boolean aplicada()    { return aplicada; }
    public LocalDate data()      { return data; }
    public String medico()       { return medico; }
    public String lote()         { return lote; }
}
