package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

/**
 * Estágio de vida do paciente, derivado da idade em anos. Filtros simples;
 * regras específicas por espécie poderiam refinar isto no futuro.
 */
public enum FaixaEtaria {
    FILHOTE, ADULTO, SENIOR;

    public static FaixaEtaria de(int idadeAnos) {
        if (idadeAnos < 0)
            throw new IllegalArgumentException("Idade não pode ser negativa.");
        if (idadeAnos < 1) return FILHOTE;
        if (idadeAnos <= 7) return ADULTO;
        return SENIOR;
    }
}
