package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.List;

public interface IPlanoNutricionalRepositorio {
    void save(PlanoNutricional plano);
    PlanoNutricional findById(PlanoNutricionalId id);
    List<PlanoNutricional> findByPacienteId(String pacienteId);
    boolean existsById(PlanoNutricionalId id);
}
