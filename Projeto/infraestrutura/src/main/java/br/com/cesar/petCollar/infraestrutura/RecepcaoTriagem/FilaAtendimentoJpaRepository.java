package br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FilaAtendimentoJpaRepository extends JpaRepository<PosicaoFilaJpa, String> {
}
