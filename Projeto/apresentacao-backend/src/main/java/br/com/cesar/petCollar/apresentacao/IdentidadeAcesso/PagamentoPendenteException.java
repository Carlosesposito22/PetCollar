package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public class PagamentoPendenteException extends RuntimeException {

    private final String contaId;

    public PagamentoPendenteException(String contaId) {
        super("Acesso será liberado somente após confirmação do pagamento.");
        this.contaId = contaId;
    }

    public String getContaId() {
        return contaId;
    }
}
