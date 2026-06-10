package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

/**
 * Item da lista de observações nutricionais (F-11 RN 7).
 * Ex.: "Fornecer água fresca à vontade", "Dividir a porção em 2-3 refeições".
 */
public record ObservacaoNutricional(String texto) {
    public ObservacaoNutricional {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException("Observação não pode ser vazia.");
        texto = texto.trim();
        if (texto.length() > 500)
            throw new IllegalArgumentException("Observação não pode passar de 500 caracteres.");
    }
}
