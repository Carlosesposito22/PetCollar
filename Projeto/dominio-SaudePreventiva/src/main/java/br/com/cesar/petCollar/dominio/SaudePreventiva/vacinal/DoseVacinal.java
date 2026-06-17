package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import java.time.LocalDate;

public class DoseVacinal {

    private final VacinaId id;
    private final int doseNumero;
    private LocalDate dataAgendada;
    private LocalDate dataAplicacao;
    private String medico;
    private String lote;

    public DoseVacinal(VacinaId id, int doseNumero, LocalDate dataAgendada) {
        if (id == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");
        if (doseNumero < 1)
            throw new IllegalArgumentException("Número da dose deve ser maior que zero.");
        if (dataAgendada == null)
            throw new IllegalArgumentException("Data agendada da dose não pode ser nula.");
        this.id           = id;
        this.doseNumero   = doseNumero;
        this.dataAgendada = dataAgendada;
    }

    public DoseVacinal(VacinaId id, int doseNumero, LocalDate dataAgendada,
                       LocalDate dataAplicacao, String medico, String lote) {
        if (id == null)
            throw new IllegalArgumentException("Id da dose não pode ser nulo.");
        if (doseNumero < 1)
            throw new IllegalArgumentException("Número da dose deve ser maior que zero.");
        if (dataAgendada == null)
            throw new IllegalArgumentException("Data agendada da dose não pode ser nula.");
        this.id            = id;
        this.doseNumero    = doseNumero;
        this.dataAgendada  = dataAgendada;
        this.dataAplicacao = dataAplicacao;
        this.medico        = medico;
        this.lote          = lote;
    }

    public void aplicar(LocalDate dataAplicacao, String medico, String lote) {
        if (this.dataAplicacao != null)
            throw new IllegalStateException("Esta dose já foi aplicada em " + this.dataAplicacao + ".");
        if (dataAplicacao == null)
            throw new IllegalArgumentException("Data de aplicação não pode ser nula.");
        if (medico == null || medico.isBlank())
            throw new IllegalArgumentException("Nome do médico veterinário não pode ser vazio.");
        if (lote == null || lote.isBlank())
            throw new IllegalArgumentException("Lote/Selo digital não pode ser vazio.");
        this.dataAplicacao = dataAplicacao;
        this.medico        = medico;
        this.lote          = lote;
        this.dataAgendada  = dataAplicacao;
    }

    public void reagendar(LocalDate novaData) {
        if (this.dataAplicacao != null)
            throw new IllegalStateException("Não é possível reagendar uma dose já aplicada em " + this.dataAplicacao + ".");
        if (novaData == null)
            throw new IllegalArgumentException("Nova data da dose não pode ser nula.");
        this.dataAgendada = novaData;
    }

    public StatusDoseVacinal status() {
        if (dataAplicacao != null)        return StatusDoseVacinal.APLICADA;
        if (dataAgendada.isBefore(LocalDate.now())) return StatusDoseVacinal.EM_ATRASO;
        return StatusDoseVacinal.PENDENTE;
    }

    public LocalDate dataEfetiva() {
        return dataAplicacao != null ? dataAplicacao : dataAgendada;
    }

    public boolean estaAplicada()  { return dataAplicacao != null; }
    public boolean estaEmAtraso()  { return status() == StatusDoseVacinal.EM_ATRASO; }

    public VacinaId getId()           { return id; }
    public int getDoseNumero()        { return doseNumero; }
    public LocalDate getDataAgendada(){ return dataAgendada; }
    public LocalDate getDataAplicacao(){ return dataAplicacao; }
    public String getMedico()         { return medico; }
    public String getLote()           { return lote; }
}
