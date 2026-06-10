package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petCollar.dominio.AtendimentoClinico.relatorio.IRelatorioClinicoRepositorio;
import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinico;
import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinicoId;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RelatorioClinicoRepositorioEmMemoria implements IRelatorioClinicoRepositorio {

    private final Map<String, RelatorioClinico> armazenamento = new LinkedHashMap<>();

    @Override
    public void salvar(RelatorioClinico relatorio) {
        if (relatorio == null)
            throw new IllegalArgumentException("Relatório não pode ser nulo.");
        armazenamento.put(relatorio.getId().getValor(), relatorio);
    }

    @Override
    public Optional<RelatorioClinico> buscarPorId(RelatorioClinicoId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazenamento.get(id.getValor()));
    }

    @Override
    public List<RelatorioClinico> listarPorPaciente(PacienteId pacienteId) {
        if (pacienteId == null) return List.of();
        return armazenamento.values().stream()
            .filter(r -> r.getPacienteId().equals(pacienteId))
            .sorted(Comparator.comparing(RelatorioClinico::getCriadoEm).reversed())
            .toList();
    }

    @Override
    public List<RelatorioClinico> listarPorAtendimento(AtendimentoId atendimentoId) {
        if (atendimentoId == null) return List.of();
        return armazenamento.values().stream()
            .filter(r -> r.getAtendimentoId().equals(atendimentoId))
            .toList();
    }

    @Override
    public boolean existePorAtendimento(AtendimentoId atendimentoId) {
        if (atendimentoId == null) return false;
        return armazenamento.values().stream()
            .anyMatch(r -> r.getAtendimentoId().equals(atendimentoId));
    }

    @Override
    public List<RelatorioClinico> buscarUltimos3PorPaciente(PacienteId pacienteId) {
        if (pacienteId == null) return List.of();
        return armazenamento.values().stream()
            .filter(r -> r.getPacienteId().equals(pacienteId))
            .filter(r -> r.getSinaisVitais() != null)
            .sorted(Comparator.comparing(RelatorioClinico::getCriadoEm).reversed())
            .limit(3)
            .toList();
    }
}
