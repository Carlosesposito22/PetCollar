package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TriagemJpaRepository extends JpaRepository<TriagemJpa, String> {
    List<TriagemJpa> findByPacienteIdAndStatus(String pacienteId, String status);
    List<TriagemJpa> findByTutorId(String tutorId);
}