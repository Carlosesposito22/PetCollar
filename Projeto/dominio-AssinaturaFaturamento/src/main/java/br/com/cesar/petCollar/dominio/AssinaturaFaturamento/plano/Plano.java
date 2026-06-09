package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

/**
 * Agregado Plano (F-07). Representa o catálogo de planos comercializados pela
 * clínica — nome comercial + valor da mensalidade. Identidade compartilhada via
 * {@link PlanoId} para permitir referências de outros contextos sem dependência.
 */
public class Plano {

    private final PlanoId id;
    private String nome;
    private ValorMensalidade mensalidade;

    public Plano(PlanoId id, String nome, ValorMensalidade mensalidade) {
        if (id == null)
            throw new IllegalArgumentException("Id do plano não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do plano não pode ser vazio.");
        if (mensalidade == null)
            throw new IllegalArgumentException("Mensalidade do plano não pode ser nula.");
        this.id = id;
        this.nome = nome;
        this.mensalidade = mensalidade;
    }

    public void alterar(String nome, ValorMensalidade mensalidade) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do plano não pode ser vazio.");
        if (mensalidade == null)
            throw new IllegalArgumentException("Mensalidade do plano não pode ser nula.");
        this.nome = nome;
        this.mensalidade = mensalidade;
    }

    public PlanoId getId()                  { return id; }
    public String getNome()                 { return nome; }
    public ValorMensalidade getMensalidade(){ return mensalidade; }
}
