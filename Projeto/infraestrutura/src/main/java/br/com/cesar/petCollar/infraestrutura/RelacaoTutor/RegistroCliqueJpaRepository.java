package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroCliqueJpaRepository extends JpaRepository<RegistroCliqueJpa, String> {
    Optional<RegistroCliqueJpa> findTopByCpfIndicadoOrderByTimestampDesc(String cpfIndicado);
}
