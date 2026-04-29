package petcollar.dominio.recepcaotriagem.paciente;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

public interface IPacienteRepositorio {
    void save(Paciente paciente);
    Paciente findById(PacienteId id);
    boolean existsByNomeAndEspecie(String nome, Especie especie);
}
