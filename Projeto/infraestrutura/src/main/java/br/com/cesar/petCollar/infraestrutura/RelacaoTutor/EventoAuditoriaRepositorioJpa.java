package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.EventoAuditoria;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IEventoAuditoriaRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventoAuditoriaRepositorioJpa implements IEventoAuditoriaRepositorio {

    private final EventoAuditoriaJpaRepository jpa;

    public EventoAuditoriaRepositorioJpa(EventoAuditoriaJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(EventoAuditoria evento) {
        jpa.save(EventoAuditoriaJpa.fromDomain(evento));
    }

    @Override
    public List<EventoAuditoria> listarPorTutor(TutorId tutorId) {
        return jpa.findByTutorIdOrderByTimestampDesc(tutorId.getValor()).stream()
                  .map(EventoAuditoriaJpa::toDomain)
                  .toList();
    }

    @Override
    public List<EventoAuditoria> listarPorIndicacao(IndicacaoId indicacaoId) {
        return jpa.findByIndicacaoIdOrderByTimestampDesc(indicacaoId.getValor()).stream()
                  .map(EventoAuditoriaJpa::toDomain)
                  .toList();
    }
}
