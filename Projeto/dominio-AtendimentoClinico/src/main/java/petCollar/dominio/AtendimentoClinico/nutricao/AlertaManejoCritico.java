package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.Objects;

public final class AlertaManejoCritico {

    private final boolean ativo;
    private final TipoAlertaManejo tipo;
    private final String mensagem;

    public AlertaManejoCritico(boolean ativo, TipoAlertaManejo tipo, String mensagem) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de alerta não pode ser nulo.");
        if (ativo && (mensagem == null || mensagem.isBlank()))
            throw new IllegalArgumentException(
                "Mensagem não pode ser vazia quando alerta está ativo.");

        this.ativo = ativo;
        this.tipo = tipo;
        this.mensagem = mensagem != null ? mensagem : "";
    }

    public static AlertaManejoCritico nenhum() {
        return new AlertaManejoCritico(false, TipoAlertaManejo.NENHUM, "");
    }

    public static AlertaManejoCritico obesidade(String mensagem) {
        return new AlertaManejoCritico(true, TipoAlertaManejo.OBESIDADE, mensagem);
    }

    public static AlertaManejoCritico caquexia(String mensagem) {
        return new AlertaManejoCritico(true, TipoAlertaManejo.CAQUEXIA, mensagem);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public TipoAlertaManejo getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertaManejoCritico)) return false;
        AlertaManejoCritico that = (AlertaManejoCritico) o;
        return ativo == that.ativo
                && tipo == that.tipo
                && Objects.equals(mensagem, that.mensagem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ativo, tipo, mensagem);
    }

    @Override
    public String toString() {
        return "AlertaManejoCritico{" +
                "ativo=" + ativo +
                ", tipo=" + tipo +
                ", mensagem='" + mensagem + '\'' +
                '}';
    }
}
