package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;



import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação provisória em memória de {@link IConfiguracaoProtocoloRepositorio}.
 * A vigente é a de maior versão; o histórico das anteriores é preservado.
 */
// @Repository removido — substituído por adapter JPA
public class ConfiguracaoProtocoloRepositorioEmMemoria implements IConfiguracaoProtocoloRepositorio {

    private final List<ConfiguracaoProtocolo> historico = new CopyOnWriteArrayList<>();

    @Override
    public void salvar(ConfiguracaoProtocolo configuracao) {
        historico.removeIf(c -> c.getId().equals(configuracao.getId()));
        historico.add(configuracao);
    }

    @Override
    public Optional<ConfiguracaoProtocolo> buscarVigente() {
        return historico.stream().max(Comparator.comparingInt(ConfiguracaoProtocolo::getVersao));
    }

    @Override
    public Optional<ConfiguracaoProtocolo> buscarPorId(ConfiguracaoProtocoloId id) {
        return historico.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    @Override
    public List<ConfiguracaoProtocolo> listarHistorico() {
        return List.copyOf(historico);
    }
}
