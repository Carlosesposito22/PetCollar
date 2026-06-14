package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;
import br.com.cesar.petCollar.infraestrutura.SaudePreventiva.CicloVacinalJpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter JPA de {@link PortalTutorRepositorio} para Pacientes.
 * Ao remover um paciente, realiza cascade nos ciclos vacinais via
 * {@link CicloVacinalJpaRepository}.
 */
@Repository
public class PortalTutorRepositorioJpa implements PortalTutorRepositorio {

    private final PacienteJpaRepository pacientes;
    private final CicloVacinalJpaRepository ciclosVacinais;
    private final TutorRecepcaoJpaRepository tutoresRecepcao;

    public PortalTutorRepositorioJpa(PacienteJpaRepository pacientes,
                                      CicloVacinalJpaRepository ciclosVacinais,
                                      TutorRecepcaoJpaRepository tutoresRecepcao) {
        this.pacientes       = pacientes;
        this.ciclosVacinais  = ciclosVacinais;
        this.tutoresRecepcao = tutoresRecepcao;
    }

    @Override
    public List<Paciente> listarPacientesDoTutor(String tutorId) {
        // O portal identifica o tutor pelo e-mail de login. A recepção pode ter criado
        // o mesmo tutor sem e-mail, usando um UUID como chave. Buscamos por todos os
        // identificadores válidos para unificar a visão de pacientes.
        List<String> chaves = new ArrayList<>();
        chaves.add(tutorId);
        tutoresRecepcao.findByEmailIgnoreCase(tutorId).stream()
                .map(t -> t.getId())
                .filter(id -> !id.equalsIgnoreCase(tutorId))
                .forEach(chaves::add);

        LinkedHashMap<String, Paciente> porId = new LinkedHashMap<>();
        pacientes.findByTutorIdIn(chaves).stream()
                .map(PacienteJpa::toDomain)
                .forEach(p -> porId.putIfAbsent(p.id(), p));
        return porId.values().stream()
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .toList();
    }

    @Override
    public Optional<Paciente> buscarPaciente(String id) {
        return pacientes.findById(id).map(PacienteJpa::toDomain);
    }

    @Override
    public void salvarPaciente(Paciente paciente) {
        pacientes.save(PacienteJpa.fromDomain(paciente));
    }

    @Override
    @Transactional
    public void removerPaciente(String id) {
        // DELETE em massa via JPQL (doses antes dos ciclos): evita o UPDATE SET
        // cicloId=NULL do orphanRemoval, que falhava na coluna NOT NULL e impedia
        // a exclusão de pets com carteira de vacinação.
        ciclosVacinais.deletarDosesPorPaciente(id);
        ciclosVacinais.deletarCiclosPorPaciente(id);
        if (pacientes.existsById(id)) {
            pacientes.deleteById(id);
        }
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
