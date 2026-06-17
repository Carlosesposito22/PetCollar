package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ConsultaJpaRepository extends JpaRepository<ConsultaJpa, String> {

    List<ConsultaJpa> findByPacienteId(String pacienteId);

    List<ConsultaJpa> findByStatusIn(Collection<String> statuses);

    List<ConsultaJpa> findByPacienteIdAndStatusIn(String pacienteId, Collection<String> status);

    @Query("""
           select c from ConsultaJpa c
           where c.medicoId = :medicoId
             and c.horarioInicio < :fim
             and c.horarioFim > :inicio
           """)
    List<ConsultaJpa> buscarPorMedicoEPeriodo(@Param("medicoId") String medicoId,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim);

    List<ConsultaJpa> findByMedicoIdAndStatusIn(String medicoId, Collection<String> statuses);

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
