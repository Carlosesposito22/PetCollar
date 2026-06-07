package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiretivaConsentimentoRepositorioJpa implements IDiretivaConsentimentoRepositorio {

    private final DiretivaConsentimentoJpaRepository jpa;

    public DiretivaConsentimentoRepositorioJpa(DiretivaConsentimentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<TipoConduta> listarCondutasAutorizadas(PacienteId pacienteId) {
        return jpa.findByPacienteId(pacienteId.getValor())
                .map(DiretivaConsentimentoJpa::getCondutas)
                .orElseGet(List::of);
    }

    @Override
    public boolean verificarAutorizacao(PacienteId pacienteId, TipoConduta conduta) {
        return jpa.findByPacienteId(pacienteId.getValor())
                .map(d -> d.getCondutas().contains(conduta))
                .orElse(false);
    }
}
