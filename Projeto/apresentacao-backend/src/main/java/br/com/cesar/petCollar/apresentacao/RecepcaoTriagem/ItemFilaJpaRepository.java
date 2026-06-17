package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemFilaJpaRepository extends JpaRepository<ItemFilaJpa, String> {

    boolean existsByPacienteId(String pacienteId);

    List<ItemFilaJpa> findByMedicoId(String medicoId);

    void deleteByPacienteId(String pacienteId);
}
