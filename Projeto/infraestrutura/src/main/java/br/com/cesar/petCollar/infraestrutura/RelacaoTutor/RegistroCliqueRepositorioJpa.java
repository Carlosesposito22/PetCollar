package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IRegistroCliqueRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.RegistroClique;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RegistroCliqueRepositorioJpa implements IRegistroCliqueRepositorio {

    private final RegistroCliqueJpaRepository jpa;

    public RegistroCliqueRepositorioJpa(RegistroCliqueJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(RegistroClique clique) {
        jpa.save(RegistroCliqueJpa.fromDomain(clique));
    }

    @Override
    public Optional<RegistroClique> buscarUltimoPorCpf(CPF cpf) {
        return jpa.findTopByCpfIndicadoOrderByTimestampDesc(cpf.getValor())
                  .map(RegistroCliqueJpa::toDomain);
    }
}
