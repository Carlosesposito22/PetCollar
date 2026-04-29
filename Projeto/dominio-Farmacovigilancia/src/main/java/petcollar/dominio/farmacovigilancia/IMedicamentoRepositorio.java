package petcollar.dominio.farmacovigilancia;

import java.util.List;

public interface IMedicamentoRepositorio {
    void save(Medicamento medicamento);
    Medicamento findById(MedicamentoId id);
    List<Medicamento> findAll();
}
