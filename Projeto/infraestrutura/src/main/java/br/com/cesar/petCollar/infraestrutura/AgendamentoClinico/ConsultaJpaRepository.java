package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ConsultaJpaRepository extends JpaRepository<ConsultaJpa, String> {

    List<ConsultaJpa> findByPacienteId(String pacienteId);

    /** Consultas com qualquer dos status informados — usado pelo ACL de atendimentos (F-03). */
    List<ConsultaJpa> findByStatusIn(Collection<String> statuses);

    List<ConsultaJpa> findByPacienteIdAndStatusIn(String pacienteId, Collection<String> status);

    /** Consultas do médico cujo intervalo intersecta o período (base da disponibilidade — RN 4). */
    @Query("""
           select c from ConsultaJpa c
           where c.medicoId = :medicoId
             and c.horarioInicio < :fim
             and c.horarioFim > :inicio
           """)
    List<ConsultaJpa> buscarPorMedicoEPeriodo(@Param("medicoId") String medicoId,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim);

    /** Consultas do médico com qualquer dos status informados (independente de período). */
    List<ConsultaJpa> findByMedicoIdAndStatusIn(String medicoId, Collection<String> statuses);

    /** Existe consulta ativa do paciente sobreposta ao intervalo informado (RN 5). */
    @Query("""
           select case when count(c) > 0 then true else false end from ConsultaJpa c
           where c.pacienteId = :pacienteId
             and c.status <> 'CANCELADA'
             and c.horarioInicio < :fim
             and c.horarioFim > :inicio
           """)
    boolean existeConflito(@Param("pacienteId") String pacienteId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim);
}
