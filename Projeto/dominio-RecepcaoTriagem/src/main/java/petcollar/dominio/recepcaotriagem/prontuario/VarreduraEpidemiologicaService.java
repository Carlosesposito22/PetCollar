package petcollar.dominio.recepcaotriagem.prontuario;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public class VarreduraEpidemiologicaService {

    private static final int DIAS_HISTORICO_INFECTANTE = 40;

    private final IDiagnosticoRepositorio diagnosticoRepositorio;

    public VarreduraEpidemiologicaService(IDiagnosticoRepositorio diagnosticoRepositorio) {
        this.diagnosticoRepositorio = diagnosticoRepositorio;
    }

    /**
     * Verifica se algum animal do tutor teve diagnóstico de doença infectocontagiosa
     * nos últimos 40 dias. Se sim, ativa o alerta epidemiológico no ResultadoBusca.
     * RN-004, RN-005, RN-006, RN-007
     */
    public void executarVarredura(Tutor tutor, ResultadoBusca resultado) {
        if (tutor == null)
            throw new IllegalArgumentException(
                "Tutor não pode ser nulo para execução da varredura epidemiológica.");
        if (resultado == null)
            throw new IllegalArgumentException(
                "ResultadoBusca não pode ser nulo para execução da varredura epidemiológica.");

        for (PacienteId pacienteId : tutor.getPacientesVinculados()) {
            List<String> infectantes = diagnosticoRepositorio
                    .findInfectantesUltimos(DIAS_HISTORICO_INFECTANTE, pacienteId);
            if (infectantes != null && !infectantes.isEmpty()) {
                resultado.ativarAlertaEpidemiologico();
                return;
            }
        }
    }
}
