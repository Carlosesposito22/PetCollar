package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;

import java.util.Collections;
import java.util.List;

public final class ResponsavelSecundario {

    private final ResponsavelSecundarioId id;
    private final String nome;
    private final int prioridade;
    private final List<CanalContato> canais;

    public ResponsavelSecundario(ResponsavelSecundarioId id, String nome, int prioridade,
                                 List<CanalContato> canais) {
        if (id == null)
            throw new IllegalArgumentException("Id do responsável secundário não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do responsável secundário não pode ser vazio.");
        if (canais == null || canais.isEmpty())
            throw new IllegalArgumentException("O responsável secundário deve ter ao menos um canal de contato.");
        this.id = id;
        this.nome = nome;
        this.prioridade = prioridade;
        this.canais = List.copyOf(canais);
    }

    public ResponsavelSecundarioId getId()   { return id; }
    public String getNome()                  { return nome; }
    public int getPrioridade()               { return prioridade; }

    public List<CanalContato> getCanais() {
        return Collections.unmodifiableList(canais);
    }
}
