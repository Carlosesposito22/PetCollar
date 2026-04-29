package petcollar.dominio.recepcaotriagem.triagem;

import petcollar.dominio.recepcaotriagem.paciente.PacienteId;

import java.util.List;

public interface ITriagemRepositorio {
    void save(Triagem triagem);
    Triagem findById(TriagemId id);
    List<Triagem> findByPacienteId(PacienteId pacienteId);
    List<Triagem> findByStatus(StatusTriagem status);
}
