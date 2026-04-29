package petcollar.dominio.recepcaotriagem.paciente;

import java.util.List;

public interface IPacienteRepositorio {
    void save(Paciente paciente);
    Paciente findById(PacienteId id);
    boolean existsByNomeAndEspecie(String nome, Especie especie);
}
