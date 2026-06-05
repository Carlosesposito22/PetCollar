package br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade;

/**
 * Agregado simples que descreve uma especialidade clínica usada para filtrar os
 * médicos disponíveis ao agendar uma consulta (RN 2).
 */
public class Especialidade {

    private final EspecialidadeId id;
    private final String nome;
    private final String descricao;

    public Especialidade(EspecialidadeId id, String nome, String descricao) {
        if (id == null)
            throw new IllegalArgumentException("Id da especialidade não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome da especialidade não pode ser vazio.");
        this.id = id;
        this.nome = nome.trim();
        this.descricao = descricao;
    }

    public EspecialidadeId getId() { return id; }
    public String getNome()        { return nome; }
    public String getDescricao()   { return descricao; }
}
