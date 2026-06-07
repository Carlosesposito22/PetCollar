package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
public class ResponsavelSecundarioRepositorioJpa implements IResponsavelSecundarioRepositorio {

    private final ResponsavelSecundarioJpaRepository jpa;

    public ResponsavelSecundarioRepositorioJpa(ResponsavelSecundarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ResponsavelSecundario> listarPorPaciente(PacienteId pacienteId) {
        return jpa.findByPacienteId(pacienteId.getValor()).stream()
                .map(ResponsavelSecundarioJpa::toDomain)
                .sorted(Comparator.comparingInt(ResponsavelSecundario::getPrioridade))
                .toList();
    }
}
