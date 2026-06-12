package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

/**
 * Estados da {@link Prescricao}. Espelha o ciclo da F-11 — uma vez
 * FINALIZADA, o agregado é imutável; ao emitir uma nova para o mesmo
 * paciente no mesmo atendimento, a anterior é marcada SUBSTITUIDA
 * (auditoria preservada).
 */
public enum StatusPrescricao {
    FINALIZADA,
    SUBSTITUIDA
}
