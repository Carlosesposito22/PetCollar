package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import java.time.LocalDateTime;
import java.util.List;

public record ConfiguracaoProtocoloDTO(String id, int tempoLimiteEsperaMinutos,
                                       List<String> canaisHabilitados,
                                       int intervaloEntreTentativasMinutos,
                                       int quantidadeMaximaTentativasPorCanal,
                                       List<String> niveisEscalonamento, int versao,
                                       LocalDateTime criadaEm, LocalDateTime atualizadaEm) {

    public static ConfiguracaoProtocoloDTO de(ConfiguracaoProtocolo c) {
        return new ConfiguracaoProtocoloDTO(
            c.getId().getValor(),
            c.getTempoLimiteEsperaMinutos(),
            c.getCanaisHabilitados().stream().map(CanalContato::name).toList(),
            c.getIntervaloEntreTentativasMinutos(),
            c.getQuantidadeMaximaTentativasPorCanal(),
            c.getNiveisEscalonamento().stream().map(NivelEscalonamento::name).toList(),
            c.getVersao(),
            c.getCriadaEm(),
            c.getAtualizadaEm());
    }
}
