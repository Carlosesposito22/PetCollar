package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.util.List;

public interface IEventoAuditoriaRepositorio {

    void salvar(EventoAuditoria evento);

    List<EventoAuditoria> listarPorTutor(TutorId tutorId);

    List<EventoAuditoria> listarPorIndicacao(IndicacaoId indicacaoId);
}
