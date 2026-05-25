package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public class ContaSuspensaException extends RuntimeException {
    public ContaSuspensaException() {
        super("Conta suspensa. Entre em contato com o suporte.");
    }
}
