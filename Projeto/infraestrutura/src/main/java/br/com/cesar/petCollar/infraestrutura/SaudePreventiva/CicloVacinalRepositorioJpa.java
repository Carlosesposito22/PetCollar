package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.ICicloVacinalRepositorio;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA que implementa {@link ICicloVacinalRepositorio} (§6.4).
 * Traduz entre o domínio {@link CicloVacinal} e a entidade {@link CicloVacinalJpa}.
 */
@Repository
public class CicloVacinalRepositorioJpa implements ICicloVacinalRepositorio {

    private final CicloVacinalJpaRepository jpa;
    private final DoseVacinalJpaRepository doseJpa;

    public CicloVacinalRepositorioJpa(CicloVacinalJpaRepository jpa, DoseVacinalJpaRepository doseJpa) {
        this.jpa     = jpa;
        this.doseJpa = doseJpa;
    }

    @Override
    public void salvar(CicloVacinal ciclo) {
        jpa.save(CicloVacinalJpa.fromDomain(ciclo));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CicloVacinal> buscarPorId(VacinaId id) {
        return jpa.findById(id.getValor()).map(CicloVacinalJpa::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CicloVacinal> listarPorPaciente(PacienteId pacienteId) {
        return jpa.findByPacienteId(pacienteId.getValor()).stream()
                  .map(CicloVacinalJpa::toDomain)
                  .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CicloVacinal> buscarPorPacienteENomeCiclo(PacienteId pacienteId, String nomeCiclo) {
        return jpa.findByPacienteIdAndNomeCicloIgnoreCase(pacienteId.getValor(), nomeCiclo)
                  .map(CicloVacinalJpa::toDomain);
    }

    @Override
    @Transactional
    public void remover(VacinaId id) {
        doseJpa.deleteByCicloId(id.getValor());
        jpa.deletarPorId(id.getValor());
    }

    @Override
    @Transactional
    public void removerPorPaciente(PacienteId pacienteId) {
        // DELETE em massa via JPQL: remove primeiro as doses (filhas) e depois os
        // ciclos, evitando o UPDATE SET cicloId=NULL do orphanRemoval (cicloId é NOT NULL).
        jpa.deletarDosesPorPaciente(pacienteId.getValor());
        jpa.deletarCiclosPorPaciente(pacienteId.getValor());
    }
}
