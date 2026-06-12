package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoJpaRepository extends JpaRepository<MedicamentoJpa, String> {
}
