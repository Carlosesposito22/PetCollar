package petcollar.dominio.recepcaotriagem.prontuario;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public interface IDiagnosticoRepositorio {
    List<String> findInfectantesUltimos(int dias, PacienteId pacienteId);
}
