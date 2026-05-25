package petcollar.dominio.atendimentoclinico.relatorio;

// TODO: placeholder mínimo criado para destravar a build.
// Substituir pela referência ao agregado real de Médico quando o domínio expuser o tipo.
public record MedicoId(String valor) {
    public MedicoId {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("MedicoId não pode ser vazio.");
        }
    }
}
