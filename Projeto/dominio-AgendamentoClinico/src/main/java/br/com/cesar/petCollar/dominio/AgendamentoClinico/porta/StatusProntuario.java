package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

/**
 * Read-model do status do prontuário, exposto pela porta de anticorrupção
 * {@link IConsultaProntuario}. Mantém AgendamentoClinico desacoplado do agregado
 * Prontuario (contexto AtendimentoClinico).
 */
public enum StatusProntuario {
    ATIVO,
    INATIVO
}
