package petcollar.dominio.recepcaotriagem.paciente;

import java.util.Objects;

public final class AlertaComportamental {

    private final String descricao;
    private final boolean alertaVisualAltaVisibilidade;

    public AlertaComportamental(String descricao, boolean alertaVisualAltaVisibilidade) {
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição do alerta comportamental não pode ser vazia.");
        this.descricao = descricao;
        this.alertaVisualAltaVisibilidade = alertaVisualAltaVisibilidade;
    }

    public String getDescricao()                  { return descricao; }
    public boolean isAlertaVisualAltaVisibilidade(){ return alertaVisualAltaVisibilidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertaComportamental)) return false;
        AlertaComportamental other = (AlertaComportamental) o;
        return alertaVisualAltaVisibilidade == other.alertaVisualAltaVisibilidade
            && Objects.equals(descricao, other.descricao);
    }

    @Override
    public int hashCode() { return Objects.hash(descricao, alertaVisualAltaVisibilidade); }
}
