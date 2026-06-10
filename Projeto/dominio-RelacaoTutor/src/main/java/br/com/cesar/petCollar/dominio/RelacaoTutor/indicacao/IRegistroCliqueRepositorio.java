package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Optional;

public interface IRegistroCliqueRepositorio {

    void salvar(RegistroClique registro);

    /** Retorna o clique mais recente do indicado (Último Clique — RN-11). */
    Optional<RegistroClique> buscarUltimoPorCpf(CPF cpfIndicado);
}
