package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfiguracaoProtocolo {

    private final ConfiguracaoProtocoloId id;
    private int tempoLimiteEsperaMinutos;
    private List<CanalContato> canaisHabilitados;
    private int intervaloEntreTentativasMinutos;
    private int quantidadeMaximaTentativasPorCanal;
    private List<NivelEscalonamento> niveisEscalonamento;
    private int versao;
    private final LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public ConfiguracaoProtocolo(ConfiguracaoProtocoloId id, int tempoLimiteEsperaMinutos,
                                 List<CanalContato> canaisHabilitados,
                                 int intervaloEntreTentativasMinutos,
                                 int quantidadeMaximaTentativasPorCanal,
                                 List<NivelEscalonamento> niveisEscalonamento) {
        if (id == null)
            throw new IllegalArgumentException("Id da configuração não pode ser nulo.");
        this.id = id;
        this.versao = 1;
        this.criadaEm = LocalDateTime.now();
        this.atualizadaEm = this.criadaEm;
        aplicarParametros(tempoLimiteEsperaMinutos, canaisHabilitados,
            intervaloEntreTentativasMinutos, quantidadeMaximaTentativasPorCanal, niveisEscalonamento);
    }

    public ConfiguracaoProtocolo(ConfiguracaoProtocoloId id, int tempoLimiteEsperaMinutos,
                                 List<CanalContato> canaisHabilitados,
                                 int intervaloEntreTentativasMinutos,
                                 int quantidadeMaximaTentativasPorCanal,
                                 List<NivelEscalonamento> niveisEscalonamento, int versao,
                                 LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        this.id = id;
        this.tempoLimiteEsperaMinutos = tempoLimiteEsperaMinutos;
        this.canaisHabilitados = new ArrayList<>(canaisHabilitados);
        this.intervaloEntreTentativasMinutos = intervaloEntreTentativasMinutos;
        this.quantidadeMaximaTentativasPorCanal = quantidadeMaximaTentativasPorCanal;
        this.niveisEscalonamento = new ArrayList<>(niveisEscalonamento);
        this.versao = versao;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    public void atualizar(int tempoLimiteEsperaMinutos, List<CanalContato> canaisHabilitados,
                          int intervaloEntreTentativasMinutos, int quantidadeMaximaTentativasPorCanal,
                          List<NivelEscalonamento> niveisEscalonamento) {
        aplicarParametros(tempoLimiteEsperaMinutos, canaisHabilitados,
            intervaloEntreTentativasMinutos, quantidadeMaximaTentativasPorCanal, niveisEscalonamento);
        this.versao++;
        this.atualizadaEm = LocalDateTime.now();
    }

    private void aplicarParametros(int tempoLimiteEsperaMinutos, List<CanalContato> canaisHabilitados,
                                   int intervaloEntreTentativasMinutos,
                                   int quantidadeMaximaTentativasPorCanal,
                                   List<NivelEscalonamento> niveisEscalonamento) {
        this.tempoLimiteEsperaMinutos = tempoLimiteEsperaMinutos;
        this.canaisHabilitados = canaisHabilitados == null ? List.of() : new ArrayList<>(canaisHabilitados);
        this.intervaloEntreTentativasMinutos = intervaloEntreTentativasMinutos;
        this.quantidadeMaximaTentativasPorCanal = quantidadeMaximaTentativasPorCanal;
        this.niveisEscalonamento = niveisEscalonamento == null ? List.of() : new ArrayList<>(niveisEscalonamento);
        validarConsistencia();
    }

    public void validarConsistencia() {
        if (tempoLimiteEsperaMinutos <= 0)
            throw new IllegalArgumentException("O tempo limite de espera deve ser maior que zero.");
        if (intervaloEntreTentativasMinutos <= 0)
            throw new IllegalArgumentException("O intervalo entre tentativas deve ser maior que zero.");
        if (quantidadeMaximaTentativasPorCanal <= 0)
            throw new IllegalArgumentException("A quantidade máxima de tentativas por canal deve ser maior que zero.");
        if (canaisHabilitados.isEmpty())
            throw new IllegalArgumentException("Deve haver ao menos um canal de contato habilitado.");
        if (canaisHabilitados.size() != canaisHabilitados.stream().distinct().count())
            throw new IllegalArgumentException("Não pode haver canais de contato repetidos.");
        if (niveisEscalonamento.isEmpty())
            throw new IllegalArgumentException("Deve haver ao menos um nível de escalonamento habilitado.");
        for (int i = 1; i < niveisEscalonamento.size(); i++) {
            if (niveisEscalonamento.get(i).ordem() <= niveisEscalonamento.get(i - 1).ordem())
                throw new IllegalArgumentException(
                    "Os níveis de escalonamento devem estar ordenados por prioridade crescente.");
        }
    }

    public ConfiguracaoProtocoloId getId()           { return id; }
    public int getTempoLimiteEsperaMinutos()         { return tempoLimiteEsperaMinutos; }
    public int getIntervaloEntreTentativasMinutos()  { return intervaloEntreTentativasMinutos; }
    public int getQuantidadeMaximaTentativasPorCanal() { return quantidadeMaximaTentativasPorCanal; }
    public int getVersao()                           { return versao; }
    public LocalDateTime getCriadaEm()               { return criadaEm; }
    public LocalDateTime getAtualizadaEm()           { return atualizadaEm; }

    public List<CanalContato> getCanaisHabilitados() {
        return Collections.unmodifiableList(canaisHabilitados);
    }

    public List<NivelEscalonamento> getNiveisEscalonamento() {
        return Collections.unmodifiableList(niveisEscalonamento);
    }
}
