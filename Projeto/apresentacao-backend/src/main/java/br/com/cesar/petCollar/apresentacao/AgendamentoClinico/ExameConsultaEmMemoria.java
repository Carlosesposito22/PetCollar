package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusExame;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Stand-in em memória da porta {@link IConsultaExame}. Mantém os exames por consulta
 * de origem e suporta confirmação / laudo (RN 9), garantindo a idempotência da
 * conclusão (RN 12 — não reprocessa exame já concluído).
 */
@Component
public class ExameConsultaEmMemoria implements IConsultaExame {

    private static final class Registro {
        final String consultaOrigemId;
        final String exameId;
        String descricao;
        StatusExame status;
        String laudo;

        Registro(String consultaOrigemId, String exameId, String descricao, StatusExame status) {
            this.consultaOrigemId = consultaOrigemId;
            this.exameId = exameId;
            this.descricao = descricao;
            this.status = status;
        }
    }

    private final ConcurrentMap<String, Registro> exames = new ConcurrentHashMap<>();

    public void cadastrarExame(ConsultaId consultaOrigemId, String exameId, String descricao) {
        exames.put(exameId, new Registro(
            consultaOrigemId.getValor(), exameId, descricao, StatusExame.SOLICITADO));
    }

    @Override
    public List<ExameResumo> listarPorConsultaOrigem(ConsultaId consultaOrigemId) {
        List<ExameResumo> resultado = new ArrayList<>();
        for (Registro r : exames.values()) {
            if (r.consultaOrigemId.equals(consultaOrigemId.getValor())) {
                resultado.add(new ExameResumo(r.exameId, r.descricao, r.status));
            }
        }
        return resultado;
    }

    @Override
    public long contarConcluidosPorConsultaOrigem(ConsultaId consultaOrigemId) {
        return exames.values().stream()
            .filter(r -> r.consultaOrigemId.equals(consultaOrigemId.getValor()))
            .filter(r -> r.status == StatusExame.CONCLUIDO)
            .count();
    }

    @Override
    public void confirmar(String exameId) {
        concluir(exameId, null);
    }

    @Override
    public void registrarLaudo(String exameId, String laudo) {
        if (laudo == null || laudo.isBlank())
            throw new IllegalArgumentException("Laudo não pode ser vazio.");
        concluir(exameId, laudo);
    }

    private void concluir(String exameId, String laudo) {
        Registro r = exames.get(exameId);
        if (r == null)
            throw new IllegalArgumentException("Exame não encontrado: " + exameId);
        if (r.status == StatusExame.CONCLUIDO)
            return;                       // RN 12 — idempotente, evita evento duplicado
        r.status = StatusExame.CONCLUIDO;
        if (laudo != null) {
            r.laudo = laudo;
        }
    }
}
