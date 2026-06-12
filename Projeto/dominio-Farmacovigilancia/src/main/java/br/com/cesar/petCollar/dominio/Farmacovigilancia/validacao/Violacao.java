package br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao;

/**
 * Resultado de uma verificação de segurança da prescrição. Carrega o nível
 * ({@link Nivel#BLOQUEIO} impede a assinatura; {@link Nivel#ALERTA} é apenas
 * informativo), um código para categorizar e uma mensagem humana.
 */
public record Violacao(Nivel nivel, String codigo, String mensagem) {

    public enum Nivel { BLOQUEIO, ALERTA }

    public Violacao {
        if (nivel == null) throw new IllegalArgumentException("Nível é obrigatório.");
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código da violação é obrigatório.");
        if (mensagem == null || mensagem.isBlank())
            throw new IllegalArgumentException("Mensagem da violação é obrigatória.");
    }

    public static Violacao bloqueio(String codigo, String mensagem) {
        return new Violacao(Nivel.BLOQUEIO, codigo, mensagem);
    }

    public static Violacao alerta(String codigo, String mensagem) {
        return new Violacao(Nivel.ALERTA, codigo, mensagem);
    }
}
