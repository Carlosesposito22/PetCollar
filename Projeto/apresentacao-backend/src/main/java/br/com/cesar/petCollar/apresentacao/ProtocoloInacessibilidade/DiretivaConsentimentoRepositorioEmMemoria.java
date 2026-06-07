package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;



import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stand-in da porta {@link IDiretivaConsentimentoRepositorio} (anticorrupção para o
 * contexto AtendimentoClinico). Mantém em memória as condutas previamente
 * autorizadas pelo tutor (RN 10). Será substituído pelo adapter real quando
 * AtendimentoClinico expuser as diretivas assinadas.
 */
// @Repository removido — substituído por adapter JPA
public class DiretivaConsentimentoRepositorioEmMemoria implements IDiretivaConsentimentoRepositorio {

    private final ConcurrentMap<String, List<TipoConduta>> autorizadasPorPaciente = new ConcurrentHashMap<>();

    public void autorizar(PacienteId pacienteId, TipoConduta conduta) {
        autorizadasPorPaciente.computeIfAbsent(pacienteId.getValor(), k -> new CopyOnWriteArrayList<>())
            .add(conduta);
    }

    @Override
    public List<TipoConduta> listarCondutasAutorizadas(PacienteId pacienteId) {
        return List.copyOf(autorizadasPorPaciente.getOrDefault(pacienteId.getValor(), List.of()));
    }

    @Override
    public boolean verificarAutorizacao(PacienteId pacienteId, TipoConduta conduta) {
        return autorizadasPorPaciente.getOrDefault(pacienteId.getValor(), List.of()).contains(conduta);
    }
}
