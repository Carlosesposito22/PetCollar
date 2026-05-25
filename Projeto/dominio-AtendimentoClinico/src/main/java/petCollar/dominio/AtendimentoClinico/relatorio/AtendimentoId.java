package petcollar.dominio.atendimentoclinico.relatorio;

// TODO: placeholder mínimo criado para destravar a build.
// Substituir pela referência ao agregado real de Atendimento quando o domínio expuser o tipo.
public record AtendimentoId(String valor) {
    public AtendimentoId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("AtendimentoId não pode ser vazio.");
        }
    }
}
