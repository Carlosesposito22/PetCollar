package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Credenciais inválidas.");
    }
}
