package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.IEspecialidadeRepositorio;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementação provisória em memória de {@link IEspecialidadeRepositorio}.
 */
@Repository
public class EspecialidadeRepositorioEmMemoria implements IEspecialidadeRepositorio {

    private final ConcurrentMap<String, Especialidade> especialidades = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<MedicoId>> medicosPorEspecialidade = new ConcurrentHashMap<>();

    public void cadastrar(Especialidade especialidade, List<MedicoId> medicos) {
        especialidades.put(especialidade.getId().getValor(), especialidade);
        medicosPorEspecialidade.put(especialidade.getId().getValor(), List.copyOf(medicos));
    }

    @Override
    public List<Especialidade> listarTodas() {
        return List.copyOf(especialidades.values());
    }

    @Override
    public Optional<Especialidade> buscarPorId(EspecialidadeId id) {
        return Optional.ofNullable(especialidades.get(id.getValor()));
    }

    @Override
    public List<MedicoId> listarMedicosDaEspecialidade(EspecialidadeId id) {
        return medicosPorEspecialidade.getOrDefault(id.getValor(), List.of());
    }
}
