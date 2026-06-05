package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CobrancaJpaRepository extends JpaRepository<CobrancaJpa, String> {

    List<CobrancaJpa> findByTutorIdOrderByVencimentoDesc(String tutorId);

    /**
     * Conta cobranças do tutor cuja {@code vencimento} já passou e ainda não
     * possuem {@code dataPagamento} — equivalente a status EM_ATRASO no domínio.
     */
    @Query("""
            SELECT COUNT(c) FROM CobrancaJpa c
            WHERE c.tutorId = :tutorId
              AND c.dataPagamento IS NULL
              AND c.vencimento < :hoje
            """)
    long contarEmAtraso(@Param("tutorId") String tutorId, @Param("hoje") LocalDate hoje);
}
