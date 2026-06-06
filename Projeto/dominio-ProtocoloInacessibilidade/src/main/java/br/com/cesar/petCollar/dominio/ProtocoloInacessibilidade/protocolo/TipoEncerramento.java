package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

/**
 * Motivo pelo qual um protocolo foi encerrado, parte do VO
 * {@link MotivoEncerramento}.
 */
public enum TipoEncerramento {
    SUCESSO_TUTOR,
    SUCESSO_SECUNDARIO,
    ESGOTAMENTO,
    INTERVENCAO_MANUAL
}
