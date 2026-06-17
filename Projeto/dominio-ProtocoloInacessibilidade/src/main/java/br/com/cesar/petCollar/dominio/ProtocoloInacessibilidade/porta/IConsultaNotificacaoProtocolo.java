package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import java.util.List;

public interface IConsultaNotificacaoProtocolo {

    List<RegistroNotificacaoProtocolo> listarPorProtocolo(String protocoloId);
}
