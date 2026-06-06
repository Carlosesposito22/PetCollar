package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusTentativa;

import java.util.Objects;

/**
 * Value Object devolvido por {@link IServicoCanalContato} ao executar uma tentativa
 * de contato: o {@link StatusTentativa} resultante e a mensagem de retorno do canal.
 */
public final class ResultadoContato {

    private final StatusTentativa status;
    private final String mensagem;

    public ResultadoContato(StatusTentativa status, String mensagem) {
        if (status == null)
            throw new IllegalArgumentException("Status do resultado não pode ser nulo.");
        this.status = status;
        this.mensagem = mensagem == null ? "" : mensagem;
    }

    public static ResultadoContato sucesso(String mensagem) {
        return new ResultadoContato(StatusTentativa.EXECUTADA_COM_SUCESSO, mensagem);
    }

    public static ResultadoContato semResposta(String mensagem) {
        return new ResultadoContato(StatusTentativa.SEM_RESPOSTA, mensagem);
    }

    public static ResultadoContato falhaTecnica(String mensagem) {
        return new ResultadoContato(StatusTentativa.FALHA_TECNICA, mensagem);
    }

    public StatusTentativa getStatus() { return status; }
    public String getMensagem()        { return mensagem; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultadoContato)) return false;
        ResultadoContato outro = (ResultadoContato) o;
        return status == outro.status && Objects.equals(mensagem, outro.mensagem);
    }

    @Override
    public int hashCode() { return Objects.hash(status, mensagem); }
}
