package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "configuracoes_protocolo")
public class ConfiguracaoProtocoloJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private int tempoLimiteEsperaMinutos;

    @Column(nullable = false)
    private int intervaloEntreTentativasMinutos;

    @Column(nullable = false)
    private int quantidadeMaximaTentativasPorCanal;

    @Column(nullable = false)
    private int versao;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    @ElementCollection
    @CollectionTable(name = "configuracao_canais", joinColumns = @JoinColumn(name = "configuracao_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "canal")
    private List<String> canaisHabilitados;

    @ElementCollection
    @CollectionTable(name = "configuracao_niveis", joinColumns = @JoinColumn(name = "configuracao_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "nivel")
    private List<String> niveisEscalonamento;

    protected ConfiguracaoProtocoloJpa() {}

    public static ConfiguracaoProtocoloJpa fromDomain(ConfiguracaoProtocolo c) {
        ConfiguracaoProtocoloJpa jpa = new ConfiguracaoProtocoloJpa();
        jpa.id = c.getId().getValor();
        jpa.tempoLimiteEsperaMinutos = c.getTempoLimiteEsperaMinutos();
        jpa.intervaloEntreTentativasMinutos = c.getIntervaloEntreTentativasMinutos();
        jpa.quantidadeMaximaTentativasPorCanal = c.getQuantidadeMaximaTentativasPorCanal();
        jpa.versao = c.getVersao();
        jpa.criadaEm = c.getCriadaEm();
        jpa.atualizadaEm = c.getAtualizadaEm();
        jpa.canaisHabilitados = c.getCanaisHabilitados().stream().map(CanalContato::name).toList();
        jpa.niveisEscalonamento = c.getNiveisEscalonamento().stream().map(NivelEscalonamento::name).toList();
        return jpa;
    }

    public ConfiguracaoProtocolo toDomain() {
        return new ConfiguracaoProtocolo(
            ConfiguracaoProtocoloId.de(id),
            tempoLimiteEsperaMinutos,
            canaisHabilitados.stream().map(CanalContato::valueOf).toList(),
            intervaloEntreTentativasMinutos,
            quantidadeMaximaTentativasPorCanal,
            niveisEscalonamento.stream().map(NivelEscalonamento::valueOf).toList(),
            versao,
            criadaEm,
            atualizadaEm);
    }
}
