package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Ativa o mapeamento JPA para o contexto IdentidadeAcesso. Os tipos de domínio
 * (Perfil, StatusConta, UsuarioAutenticavel, UsuarioRepositorio) vivem neste
 * módulo enquanto dominio-IdentidadeAcesso não é formalizado; a camada JPA
 * reside aqui por consequência.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.apresentacao.IdentidadeAcesso")
public class IdentidadeAcessoJpaConfig {
}
