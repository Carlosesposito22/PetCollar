package petcollar.dominio.recepcaotriagem.triagem;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public interface ITriagemRepositorio {
    void save(Triagem triagem);
    Triagem findById(TriagemId id);
    List<Triagem> findByPacienteId(PacienteId pacienteId);
    List<Triagem> findByStatus(StatusTriagem status);
}
