package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;

import java.util.List;

/**
 * Resultado consolidado da execução de uma etapa do protocolo, devolvido pelo
 * método template {@link EtapaProtocoloService#executar}. Reúne o nome da etapa,
 * as tentativas de contato registradas durante a execução e um indicador de
 * sucesso (alguém respondeu) — a etapa de escalonamento não registra tentativas
 * de contato e devolve a lista vazia.
 */
public final class ResultadoEtapa {

    private final String nomeDaEtapa;
    private final List<TentativaContato> tentativas;
    private final boolean houveSucesso;

    public ResultadoEtapa(String nomeDaEtapa, List<TentativaContato> tentativas, boolean houveSucesso) {
        if (nomeDaEtapa == null || nomeDaEtapa.isBlank())
            throw new IllegalArgumentException("Nome da etapa não pode ser vazio.");
        this.nomeDaEtapa = nomeDaEtapa;
        this.tentativas = List.copyOf(tentativas);
        this.houveSucesso = houveSucesso;
    }

    public String getNomeDaEtapa()                 { return nomeDaEtapa; }
    public List<TentativaContato> getTentativas()  { return tentativas; }
    public boolean houveSucesso()                  { return houveSucesso; }

    /** {@code true} quando a etapa não teve destinatários a contatar (ex.: escalonamento). */
    public boolean semContato() { return tentativas.isEmpty(); }
}
