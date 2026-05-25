package petcollar.dominio.recepcaotriagem.paciente;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public class TagueamentoAutomaticoService {

    private static final List<String> RACAS_BRAQUICEFALICAS = List.of(
            "PUG", "BULLDOG", "BOXER", "SHIH TZU", "LHASA APSO", "PEKINGESE", "BOSTON TERRIER"
    );

    private final IPacienteRepositorio repositorio;

    public TagueamentoAutomaticoService(IPacienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Paciente aplicarTagsAutomaticas(PacienteId id) {
        Paciente paciente = repositorio.findById(id);
        if (paciente == null)
            throw new IllegalArgumentException("Paciente não encontrado com o id informado.");

        IdadeCalculada idade = IdadeCalculada.calcular(paciente.getDataNascimento(), paciente.getEspecie());

        if (idade.isFilhote()) {
            paciente.adicionarTag(new TagVisual("FILHOTE", "Animal filhote", TipoTag.FAIXA_ETARIA, false));
        }
        if (idade.isGeriatrico()) {
            paciente.adicionarTag(new TagVisual("GERIATRICO", "Animal geriátrico", TipoTag.FAIXA_ETARIA, false));
        }

        String racaUpper = paciente.getRaca().toUpperCase();
        if (RACAS_BRAQUICEFALICAS.contains(racaUpper)) {
            paciente.adicionarTag(new TagVisual("BRAQUICEFALICO", "Raça braquicefálica", TipoTag.RACA, true));
        }

        repositorio.save(paciente);
        return paciente;
    }
}
