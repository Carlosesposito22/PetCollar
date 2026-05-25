package petcollar.dominio.atendimentoclinico.relatorio;

// TODO: placeholder mínimo criado para destravar a build.
// Substituir pela referência ao agregado real de Paciente quando o domínio expuser o tipo.
public record PacienteId(String valor) {
    public PacienteId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PacienteId não pode ser vazio.");
        }
    }
}
