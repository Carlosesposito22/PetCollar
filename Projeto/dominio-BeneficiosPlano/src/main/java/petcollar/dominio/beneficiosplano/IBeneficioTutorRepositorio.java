package petcollar.dominio.beneficiosplano;

import petcollar.dominio.compartilhado.PlanoId;
import petcollar.dominio.compartilhado.TutorId;

import java.util.List;

public interface IBeneficioTutorRepositorio {
    void save(BeneficioTutor beneficioTutor);
    BeneficioTutor findById(BeneficioTutorId id);
    List<BeneficioTutor> findByTutorId(TutorId tutorId);
    List<BeneficioTutor> findByPlanoId(PlanoId planoId);
    List<BeneficioTutor> findByStatus(StatusBeneficio status);
    List<BeneficioTutor> findByBeneficioCatalogoId(BeneficioCatalogoId beneficioCatalogoId);
}

