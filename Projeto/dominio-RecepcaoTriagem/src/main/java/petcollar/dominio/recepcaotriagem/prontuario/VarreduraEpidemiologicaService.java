package petcollar.dominio.recepcaotriagem.prontuario;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import java.util.List;


public class VarreduraEpidemiologicaService {

    private static final int DIAS_HISTORICO_INFECTANTE = 40;

    private final IDiagnosticoRepositorio diagnosticoRepositorio;

    public VarreduraEpidemiologicaService(IDiagnosticoRepositorio diagnosticoRepositorio) {
        this.diagnosticoRepositorio = diagnosticoRepositorio;
    }


    public void executarVarredura(Tutor tutor, ResultadoBusca resultado) {
        if (tutor == null)
            throw new IllegalArgumentException(
                "Tutor não pode ser nulo para execução da varredura epidemiológica.");
        if (resultado == null)
            throw new IllegalArgumentException(
                "ResultadoBusca não pode ser nulo para execução da varredura epidemiológica.");

        List<PacienteId> pacientes = tutor.getPacientesVinculados();

        FilaEpidemiologicaIterator iterator = new FilaEpidemiologicaIterator(
            pacientes, diagnosticoRepositorio, DIAS_HISTORICO_INFECTANTE);

        if (iterator.possuiInfectante()) {
            resultado.ativarAlertaEpidemiologico();
        }
    }


    public java.util.List<PacienteId> listarPacientesInfectantes(Tutor tutor) {
        if (tutor == null)
            throw new IllegalArgumentException("Tutor não pode ser nulo.");

        java.util.List<PacienteId> infectantes = new java.util.ArrayList<>();

        FilaEpidemiologicaIterator iterator = new FilaEpidemiologicaIterator(
            tutor.getPacientesVinculados(), diagnosticoRepositorio, DIAS_HISTORICO_INFECTANTE);

        while (iterator.hasNext()) {
            infectantes.add(iterator.next());
        }

        return infectantes;
    }
}