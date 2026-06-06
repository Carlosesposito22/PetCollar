package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

/**
 * Tipos de conduta clínica que podem exigir consentimento prévio do tutor durante o
 * protocolo de inacessibilidade (RN 10). A autorização é avaliada contra as
 * diretivas assinadas pelo tutor, consultadas via
 * {@link IDiretivaConsentimentoRepositorio}.
 */
public enum TipoConduta {
    PROCEDIMENTO_INVASIVO,
    MEDICACAO_CONTROLADA,
    INTERNACAO,
    PROCEDIMENTO_ELETIVO,
    EUTANASIA
}
