package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import java.util.List;

public record RequisicaoConfigurarProtocoloDTO(int tempoLimiteEsperaMinutos,
                                               List<String> canaisHabilitados,
                                               int intervaloEntreTentativasMinutos,
                                               int quantidadeMaximaTentativasPorCanal,
                                               List<String> niveisEscalonamento) {}
