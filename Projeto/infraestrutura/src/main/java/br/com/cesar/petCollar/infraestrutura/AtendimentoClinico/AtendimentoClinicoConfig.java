package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico;

import petCollar.dominio.AtendimentoClinico.relatorio.AssinaturaDigitalService;
import petCollar.dominio.AtendimentoClinico.relatorio.GeracaoEvolucaoComparativaService;
import petCollar.dominio.AtendimentoClinico.relatorio.IRelatorioClinicoRepositorio;
import petCollar.dominio.AtendimentoClinico.relatorio.RelatorioClinicoService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AtendimentoClinicoConfig {

    @Bean
    public RelatorioClinicoService relatorioClinicoService(IRelatorioClinicoRepositorio repositorio) {
        return new RelatorioClinicoService(repositorio);
    }

    @Bean
    public AssinaturaDigitalService assinaturaDigitalService(IRelatorioClinicoRepositorio repositorio) {
        return new AssinaturaDigitalService(repositorio);
    }

    @Bean
    public GeracaoEvolucaoComparativaService geracaoEvolucaoComparativaService(
            IRelatorioClinicoRepositorio repositorio) {
        return new GeracaoEvolucaoComparativaService(repositorio);
    }
}
