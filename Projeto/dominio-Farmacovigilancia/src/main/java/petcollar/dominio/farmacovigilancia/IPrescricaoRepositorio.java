package petcollar.dominio.farmacovigilancia;

import java.util.List;

public interface IPrescricaoRepositorio {
    void save(Prescricao prescricao);
    Prescricao findById(PrescricaoId id);
    List<Prescricao> findByStatus(StatusPrescricao status);
}
