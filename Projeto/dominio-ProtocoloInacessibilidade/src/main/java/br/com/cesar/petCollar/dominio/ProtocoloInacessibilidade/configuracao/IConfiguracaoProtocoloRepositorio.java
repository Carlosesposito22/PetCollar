package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao;

import java.util.List;
import java.util.Optional;

public interface IConfiguracaoProtocoloRepositorio {

    void salvar(ConfiguracaoProtocolo configuracao);

    Optional<ConfiguracaoProtocolo> buscarVigente();

    Optional<ConfiguracaoProtocolo> buscarPorId(ConfiguracaoProtocoloId id);

    List<ConfiguracaoProtocolo> listarHistorico();
}
