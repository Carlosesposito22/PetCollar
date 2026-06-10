package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficioCatalogoJpaRepository extends JpaRepository<BeneficioCatalogoJpa, String> {
    List<BeneficioCatalogoJpa> findByPlanoId(String planoId);
    List<BeneficioCatalogoJpa> findByAtivo(boolean ativo);
}
