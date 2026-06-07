package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

/** Status de uma dose vacinal, derivado a partir das datas registradas (RN-073). */
public enum StatusDoseVacinal {
    /** Dose confirmada pelo veterinário com data e lote. */
    APLICADA,
    /** Dose agendada para uma data futura. */
    PENDENTE,
    /** Data de agendamento já passou sem confirmação de aplicação. */
    EM_ATRASO
}
