package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA da interface de domínio {@link IConfiguracaoProtocoloRepositorio}.
 */
@Repository
public class ConfiguracaoProtocoloRepositorioJpa implements IConfiguracaoProtocoloRepositorio {

    private final ConfiguracaoProtocoloJpaRepository jpa;

    public ConfiguracaoProtocoloRepositorioJpa(ConfiguracaoProtocoloJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void salvar(ConfiguracaoProtocolo configuracao) {
        jpa.save(ConfiguracaoProtocoloJpa.fromDomain(configuracao));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracaoProtocolo> buscarVigente() {
        return jpa.findTopByOrderByVersaoDesc().map(ConfiguracaoProtocoloJpa::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracaoProtocolo> buscarPorId(ConfiguracaoProtocoloId id) {
        return jpa.findById(id.getValor()).map(ConfiguracaoProtocoloJpa::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracaoProtocolo> listarHistorico() {
        return jpa.findAll().stream().map(ConfiguracaoProtocoloJpa::toDomain).toList();
    }
}
