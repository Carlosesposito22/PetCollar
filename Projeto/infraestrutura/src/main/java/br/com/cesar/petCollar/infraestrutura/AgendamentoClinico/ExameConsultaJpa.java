package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusExame;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adapter JPA da porta {@link IConsultaExame} — exames persistidos na tabela
 * {@code exames}. Suporta exibição (RN 8), confirmação/laudo (RN 9) e a contagem de
 * concluídos que libera o retorno (RN 10), garantindo idempotência (RN 12).
 */
@Repository
public class ExameConsultaJpa implements IConsultaExame {

    private final ExameJpaRepository jpa;

    public ExameConsultaJpa(ExameJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ExameResumo> listarPorConsultaOrigem(ConsultaId consultaOrigemId) {
        return jpa.findByConsultaOrigemId(consultaOrigemId.getValor()).stream()
            .map(ExameJpa::toResumo)
            .toList();
    }

    @Override
    public long contarConcluidosPorConsultaOrigem(ConsultaId consultaOrigemId) {
        return jpa.countByConsultaOrigemIdAndStatus(
            consultaOrigemId.getValor(), StatusExame.CONCLUIDO.name());
    }

    @Override
    @Transactional
    public void confirmar(String exameId) {
        concluir(exameId, null);
    }

    @Override
    @Transactional
    public void registrarLaudo(String exameId, String laudo) {
        if (laudo == null || laudo.isBlank())
            throw new IllegalArgumentException("Laudo não pode ser vazio.");
        concluir(exameId, laudo);
    }

    private void concluir(String exameId, String laudo) {
        ExameJpa exame = jpa.findById(exameId)
            .orElseThrow(() -> new IllegalArgumentException("Exame não encontrado: " + exameId));
        if (StatusExame.CONCLUIDO.name().equals(exame.getStatus()))
            return;                       // RN 12 — idempotente, evita evento duplicado
        exame.concluir(laudo);
        jpa.save(exame);
    }
}
