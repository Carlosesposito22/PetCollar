package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

/**
 * Config do ProtocoloInacessibilidade migrada para
 * infraestrutura.ProtocoloInacessibilidade.
 * Os services de domínio, o seed operacional da ConfiguracaoProtocolo e os
 * adapters JPA vivem lá. Os beans de ACL/fake (ServicoCanalContatoDispatcher,
 * ServicoCanalTelefoneFake, etc.) são detectados automaticamente por @Service
 * neste pacote.
 */
public final class ProtocoloInacessibilidadeConfig {
    private ProtocoloInacessibilidadeConfig() {}
}
