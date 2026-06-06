package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stand-in da porta {@link IResponsavelSecundarioRepositorio} (anticorrupção para o
 * contexto RelacaoTutor). Mantém os responsáveis em memória, sempre devolvidos
 * ordenados por prioridade (RN 4). Será substituído pelo adapter real quando
 * RelacaoTutor expuser o cadastro de responsáveis.
 */
@Repository
public class ResponsavelSecundarioRepositorioEmMemoria implements IResponsavelSecundarioRepositorio {

    private final ConcurrentMap<String, List<ResponsavelSecundario>> porPaciente = new ConcurrentHashMap<>();

    public void cadastrar(PacienteId pacienteId, ResponsavelSecundario responsavel) {
        porPaciente.computeIfAbsent(pacienteId.getValor(), k -> new CopyOnWriteArrayList<>())
            .add(responsavel);
    }

    @Override
    public List<ResponsavelSecundario> listarPorPaciente(PacienteId pacienteId) {
        return porPaciente.getOrDefault(pacienteId.getValor(), List.of()).stream()
            .sorted(Comparator.comparingInt(ResponsavelSecundario::getPrioridade))
            .toList();
    }
}
