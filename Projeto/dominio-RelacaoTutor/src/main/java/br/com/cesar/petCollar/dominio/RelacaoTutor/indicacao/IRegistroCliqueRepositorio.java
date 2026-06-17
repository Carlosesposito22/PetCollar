package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Optional;

public interface IRegistroCliqueRepositorio {

    void salvar(RegistroClique registro);

    Optional<RegistroClique> buscarUltimoPorCpf(CPF cpfIndicado);
}
