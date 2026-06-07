package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import java.time.LocalDate;

/**
 * Entidade que representa uma dose individual dentro de um {@link CicloVacinal}.
 * Pertence ao agregado — nunca é referenciada fora do ciclo pelo Id.
 *
 * <p>Padrão State: {@link #status()} protege transições; apenas o médico
 * pode {@link #aplicar(LocalDate, String, String)} a dose (RN-078).
 */
public class DoseVacinal {

    private final VacinaId id;
    private final int doseNumero;
    private LocalDate dataAgendada;
    private LocalDate dataAplicacao;
    private String medico;
    private String lote;

    /** Construtor de criação — dose recém-agendada. */
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

    // Construtor de RECONSTRUÇÃO — usado pela infraestrutura ao recarregar do banco.
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

    /**
     * Confirma a aplicação da dose pelo veterinário (RN-074, RN-078).
     * Somente pode ser chamado uma única vez por dose.
     */
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

    /**
     * Computa o status da dose a partir das datas (RN-073).
     * APLICADA → verde; PENDENTE → amarelo; EM_ATRASO → vermelho.
     */
    public StatusDoseVacinal status() {
        if (dataAplicacao != null)        return StatusDoseVacinal.APLICADA;
        if (dataAgendada.isBefore(LocalDate.now())) return StatusDoseVacinal.EM_ATRASO;
        return StatusDoseVacinal.PENDENTE;
    }

    /** Retorna a data mais relevante: aplicação se confirmada, senão a data agendada. */
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
