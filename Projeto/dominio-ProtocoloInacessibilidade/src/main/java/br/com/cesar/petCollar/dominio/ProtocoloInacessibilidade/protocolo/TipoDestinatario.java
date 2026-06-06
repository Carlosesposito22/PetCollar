package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

/**
 * Distingue o destinatário de uma {@link TentativaContato}: o tutor principal ou
 * um responsável secundário cadastrado (RN 4).
 */
public enum TipoDestinatario {
    TUTOR_PRINCIPAL,
    RESPONSAVEL_SECUNDARIO
}
