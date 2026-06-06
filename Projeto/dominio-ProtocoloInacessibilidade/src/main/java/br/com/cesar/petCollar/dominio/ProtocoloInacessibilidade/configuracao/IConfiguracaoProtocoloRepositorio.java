package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao;

import java.util.List;
import java.util.Optional;

/**
 * Repositório (porta) do agregado {@link ConfiguracaoProtocolo}. A configuração
 * vigente é a versão mais recente; o histórico das anteriores é preservado para
 * auditoria.
 */
public interface IConfiguracaoProtocoloRepositorio {

    void salvar(ConfiguracaoProtocolo configuracao);

    /** A configuração atualmente em vigor (maior versão), consultada pela RN 1. */
    Optional<ConfiguracaoProtocolo> buscarVigente();

    Optional<ConfiguracaoProtocolo> buscarPorId(ConfiguracaoProtocoloId id);

    List<ConfiguracaoProtocolo> listarHistorico();
}
