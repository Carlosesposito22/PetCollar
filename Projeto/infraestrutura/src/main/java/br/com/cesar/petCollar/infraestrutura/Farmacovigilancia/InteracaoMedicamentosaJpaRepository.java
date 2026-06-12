package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InteracaoMedicamentosaJpaRepository
        extends JpaRepository<InteracaoMedicamentosaJpa, String> {

    List<InteracaoMedicamentosaJpa> findByMedicamentoAIdInAndMedicamentoBIdIn(
            List<String> idsA, List<String> idsB);
}
