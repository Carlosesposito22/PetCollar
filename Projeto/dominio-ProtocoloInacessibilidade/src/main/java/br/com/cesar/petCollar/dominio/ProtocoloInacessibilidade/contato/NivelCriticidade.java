package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato;

/**
 * Criticidade de uma etapa do protocolo, usada para dimensionar as notificações
 * enviadas aos envolvidos (RN 9). Cresce conforme o protocolo avança: tentativa
 * inicial ao tutor é {@code BAIXA}; acionamento de responsáveis secundários é
 * {@code MEDIA}; escalonamento administrativo é {@code ALTA}; escalonamento
 * clínico/direção é {@code CRITICA}.
 */
public enum NivelCriticidade {
    BAIXA,
    MEDIA,
    ALTA,
    CRITICA
}
