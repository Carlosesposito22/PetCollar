package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

public enum StatusProtocolo {
    INATIVO,
    ATIVADO,
    EM_TENTATIVA_TUTOR,
    EM_TENTATIVA_SECUNDARIOS,
    EM_ESCALONAMENTO,
    ENCERRADO_COM_SUCESSO,
    ENCERRADO_POR_ESGOTAMENTO
}
