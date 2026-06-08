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

/**
 * Seed operacional do petCollar, executado no startup (idempotente e auto-curável —
 * garante o estado-alvo, mesmo sobre um banco já populado). Assegura os dados mínimos
 * para a clínica funcionar: as especialidades padrão e {@value #MEDICOS_POR_ESPECIALIDADE}
 * médicos por especialidade (usuários MEDICO_VETERINARIO, login = matrícula). Roda
 * automaticamente ao subir o container Docker.
 *
 * <p>Observação: os <em>enums</em> do domínio (Perfil, StatusConta, StatusConsulta,
 * TipoConsulta, etc.) são constantes de código — não exigem povoamento no banco.
 */
@Configuration
public class SeedInicial {

    private static final Logger log = LoggerFactory.getLogger(SeedInicial.class);

    private static final String SENHA_PADRAO = "petcollar123";
    private static final int MEDICOS_POR_ESPECIALIDADE = 3;

    /** Especialidades padrão: {nome, descrição}. */
    private static final List<String[]> ESPECIALIDADES_PADRAO = List.of(
        new String[]{"Clínica Geral", "Consultas e diagnósticos gerais para cães e gatos"},
        new String[]{"Cardiologia",   "Diagnóstico e tratamento de doenças cardíacas"},
        new String[]{"Dermatologia",  "Alergias, dermatites e doenças de pele"},
        new String[]{"Ortopedia",     "Fraturas, displasias e doenças articulares"}
    );

    @Bean
    public CommandLineRunner seedInicialRunner(EspecialidadeJpaRepository especialidades,
                                               UsuarioRepositorio usuarios,
                                               PasswordEncoder encoder) {
        return args -> {
            log.info("[SEED] Verificando dados iniciais (especialidades e médicos)…");
            criarEspecialidadesSeNecessario(especialidades);
            garantirMedicosPorEspecialidade(especialidades, usuarios, encoder);
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

    /**
     * Garante {@value #MEDICOS_POR_ESPECIALIDADE} médicos válidos por especialidade.
     * Descarta vínculos para médicos que não existem mais como usuário e completa o
     * que faltar — funciona tanto num banco zerado quanto num já parcialmente populado.
     */
    private void garantirMedicosPorEspecialidade(EspecialidadeJpaRepository especialidades,
                                                 UsuarioRepositorio usuarios, PasswordEncoder encoder) {
        String senha = encoder.encode(SENHA_PADRAO);
        int criados = 0;

        for (EspecialidadeJpa esp : especialidades.findAll()) {
            Especialidade dominio = esp.toDomain();

            // Mantém apenas os médicos que ainda existem como usuário.
            List<MedicoId> medicos = new ArrayList<>();
            for (MedicoId m : esp.medicos()) {
                if (usuarios.buscar(Perfil.MEDICO_VETERINARIO, m.getValor()).isPresent()) {
                    medicos.add(m);
                }
            }
            boolean mudou = medicos.size() != esp.medicos().size();

            // Completa até o alvo, criando novos usuários-médico.
            while (medicos.size() < MEDICOS_POR_ESPECIALIDADE) {
                String matricula = usuarios.proximaMatricula(Perfil.MEDICO_VETERINARIO);
                String nome = "Dr(a). " + dominio.getNome() + " " + (medicos.size() + 1);
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
}
