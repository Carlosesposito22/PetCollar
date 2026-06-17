package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpa;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SeedInicial {

    private static final Logger log = LoggerFactory.getLogger(SeedInicial.class);

    private static final String SENHA_PADRAO = "petcollar123";
    private static final int MEDICOS_POR_ESPECIALIDADE = 3;

    private static final List<String[]> ESPECIALIDADES_PADRAO = List.of(
        new String[]{"Clínica Geral", "Consultas e diagnósticos gerais para cães e gatos"},
        new String[]{"Cardiologia",   "Diagnóstico e tratamento de doenças cardíacas"},
        new String[]{"Dermatologia",  "Alergias, dermatites e doenças de pele"},
        new String[]{"Ortopedia",     "Fraturas, displasias e doenças articulares"}
    );

    private static final java.util.Map<String, List<String>> NOMES_POR_ESPECIALIDADE =
        java.util.Map.of(
            "Clínica Geral", List.of(
                "Dr. Carlos Eduardo Lima",
                "Dra. Fernanda Souza Costa",
                "Dra. Ana Paula Mendes"),
            "Cardiologia", List.of(
                "Dr. Roberto Alves Pereira",
                "Dra. Juliana Castro Melo",
                "Dr. Marcelo Tavares Silva"),
            "Dermatologia", List.of(
                "Dra. Isabela Rodrigues Nunes",
                "Dr. André Luís Ferreira",
                "Dra. Camila Martins Lopes"),
            "Ortopedia", List.of(
                "Dr. Ricardo Gomes Santos",
                "Dra. Vanessa Oliveira Faria",
                "Dr. Bruno Costa Ribeiro")
        );

    @Bean
    public CommandLineRunner seedInicialRunner(EspecialidadeJpaRepository especialidades,
                                               UsuarioRepositorio usuarios,
                                               PasswordEncoder encoder) {
        return args -> {
            log.info("[SEED] Verificando dados iniciais (especialidades, médicos e recepcionista)…");
            criarEspecialidadesSeNecessario(especialidades);
            garantirMedicosPorEspecialidade(especialidades, usuarios, encoder);
            garantirRecepcionistaPadrao(usuarios, encoder);
            log.info("[SEED] Verificação concluída.");
        };
    }

    private void criarEspecialidadesSeNecessario(EspecialidadeJpaRepository especialidades) {
        if (!especialidades.findAll().isEmpty()) return;
        ESPECIALIDADES_PADRAO.forEach(e -> especialidades.save(
            EspecialidadeJpa.fromDomain(
                new Especialidade(EspecialidadeId.gerar(), e[0], e[1]), List.of())));
        log.info("[SEED] {} especialidades-padrão criadas.", ESPECIALIDADES_PADRAO.size());
    }

    private void garantirMedicosPorEspecialidade(EspecialidadeJpaRepository especialidades,
                                                 UsuarioRepositorio usuarios, PasswordEncoder encoder) {
        String senha = encoder.encode(SENHA_PADRAO);
        int criados = 0;

        for (EspecialidadeJpa esp : especialidades.findAll()) {
            Especialidade dominio = esp.toDomain();

            List<MedicoId> medicos = new ArrayList<>();
            for (MedicoId m : esp.medicos()) {
                if (usuarios.buscar(Perfil.MEDICO_VETERINARIO, m.getValor()).isPresent()) {
                    medicos.add(m);
                }
            }
            boolean mudou = medicos.size() != esp.medicos().size();

            List<String> nomesPool = NOMES_POR_ESPECIALIDADE.getOrDefault(
                    dominio.getNome(), List.of());
            while (medicos.size() < MEDICOS_POR_ESPECIALIDADE) {
                String matricula = usuarios.proximaMatricula(Perfil.MEDICO_VETERINARIO);
                int idx = medicos.size();
                String nome = (idx < nomesPool.size())
                        ? nomesPool.get(idx)
                        : "Dr(a). " + dominio.getNome() + " " + (idx + 1);
                usuarios.salvar(new UsuarioAutenticavel(
                    matricula, nome, Perfil.MEDICO_VETERINARIO, senha, StatusConta.ATIVA));
                medicos.add(MedicoId.de(matricula));
                criados++;
                mudou = true;
                log.info("[SEED] Médico '{}' [{}] — login: {} / {}",
                    nome, dominio.getNome(), matricula, SENHA_PADRAO);
            }

            if (mudou) {
                especialidades.save(EspecialidadeJpa.fromDomain(dominio, medicos));
            }
        }

        if (criados == 0) {
            log.info("[SEED] Médicos já estavam completos ({} por especialidade).", MEDICOS_POR_ESPECIALIDADE);
        } else {
            log.info("[SEED] {} médico(s) criados/vinculados.", criados);
        }
    }

    private void garantirRecepcionistaPadrao(UsuarioRepositorio usuarios, PasswordEncoder encoder) {
        if (!usuarios.listarPorPerfil(Perfil.RECEPCIONISTA).isEmpty()) return;
        String matricula = usuarios.proximaMatricula(Perfil.RECEPCIONISTA);
        String senha = encoder.encode(SENHA_PADRAO);
        usuarios.salvar(new UsuarioAutenticavel(
            matricula, "Recepcionista Padrão", Perfil.RECEPCIONISTA, senha, StatusConta.ATIVA));
        log.info("[SEED] Recepcionista criada — login: {} / {}", matricula, SENHA_PADRAO);
    }
}
