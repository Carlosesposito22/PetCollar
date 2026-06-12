package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescricaoJpaRepository extends JpaRepository<PrescricaoJpa, String> {

    Optional<PrescricaoJpa> findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
            String pacienteId, String status);

    List<PrescricaoJpa> findByPacienteIdAndStatusInOrderByAtualizadoEmDesc(
            String pacienteId, List<String> status);

    List<PrescricaoJpa> findByTutorIdAndStatusOrderByAtualizadoEmDesc(
            String tutorId, String status);
}
