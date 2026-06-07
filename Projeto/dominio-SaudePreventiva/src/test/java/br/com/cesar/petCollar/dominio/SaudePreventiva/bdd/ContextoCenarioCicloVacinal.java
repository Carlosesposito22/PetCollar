package br.com.cesar.petCollar.dominio.SaudePreventiva.bdd;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.ICicloVacinalRepositorio;

import org.mockito.Mockito;

import java.time.LocalDate;

/** Compartilha estado entre os steps de um mesmo cenário BDD. */
public class ContextoCenarioCicloVacinal {

    public final ICicloVacinalRepositorio repositorioMock = Mockito.mock(ICicloVacinalRepositorio.class);
    public final CicloVacinalService servico = new CicloVacinalService(repositorioMock);

    public CicloVacinal cicloAtual;
    public DoseVacinal doseAtual;
    public LocalDate dataSugerida;
    public Exception excecao;
}
