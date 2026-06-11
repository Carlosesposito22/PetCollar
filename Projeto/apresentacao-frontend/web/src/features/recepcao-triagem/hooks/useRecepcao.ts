import { useState, useCallback } from "react";
import { useAuth } from "../../../auth/AuthContext";

export interface PacienteDTO {
  id: string;
  tutorId: string;
  nome: string;
  especie: string;
  raca: string;
  nascimento: string | null;
  pesoKg: number | null;
  sexo: string | null;
  infectocontagiosoRecente: boolean;
}

export interface DadosPaciente {
  nome: string;
  especie: string;
  raca: string;
  nascimento: string;
  pesoKg: number | null;
  sexo: string;
}

export interface TutorDTO {
  id: string;
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
  alertaEpidemiologico: boolean;
  pacientes: PacienteDTO[];
}

export interface SintomaDTO {
  codigo: string;
  descricao: string;
  peso: number;
}

export interface MedicoDTO {
  id: string;
  nome: string;
}

export interface FilaItemDTO {
  pacienteId: string;
  triagemId: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  finalizadaEm: string;
  nomePaciente: string;
  tutorId: string;
  medicoId: string | null;
  nomeMedico: string | null;
}

export function useRecepcao() {
  const { apiFetch } = useAuth();
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const buscarTutorPorCpf = useCallback(async (cpf: string): Promise<TutorDTO | null> => {
    setCarregando(true);
    setErro(null);
    try {
      const cpfLimpo = cpf.replace(/\D/g, "");
      const res = await apiFetch(`/api/recepcao/tutores?cpf=${cpfLimpo}`);
      if (res.status === 404) return null;
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao buscar tutor.");
      }
      return res.json();
    } catch (e: any) {
      setErro(e.message);
      return null;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const cadastrarTutor = useCallback(async (dados: {
    cpf: string; nome: string; telefone: string; email: string;
  }): Promise<TutorDTO | null> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch("/api/recepcao/tutores", {
        method: "POST",
        body: JSON.stringify(dados),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao cadastrar tutor.");
      }
      return res.json();
    } catch (e: any) {
      setErro(e.message);
      return null;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const editarTutor = useCallback(async (
    id: string,
    dados: { nome: string; telefone: string; email: string }
  ): Promise<TutorDTO | null> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/recepcao/tutores/${id}`, {
        method: "PUT",
        body: JSON.stringify(dados),
      });
      if (!res.ok) throw new Error("Erro ao editar tutor.");
      return res.json();
    } catch (e: any) {
      setErro(e.message);
      return null;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const excluirTutor = useCallback(async (id: string): Promise<boolean> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/recepcao/tutores/${id}`, { method: "DELETE" });
      return res.ok;
    } catch (e: any) {
      setErro(e.message);
      return false;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const cadastrarPaciente = useCallback(async (
    tutorId: string,
    dados: DadosPaciente
  ): Promise<PacienteDTO | null> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/recepcao/tutores/${tutorId}/pacientes`, {
        method: "POST",
        body: JSON.stringify(dados),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao cadastrar paciente.");
      }
      return res.json();
    } catch (e: any) {
      setErro(e.message);
      return null;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const editarPaciente = useCallback(async (
    tutorId: string,
    pacienteId: string,
    dados: DadosPaciente
  ): Promise<PacienteDTO | null> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(
        `/api/recepcao/tutores/${tutorId}/pacientes/${pacienteId}`,
        { method: "PUT", body: JSON.stringify(dados) }
      );
      if (!res.ok) throw new Error("Erro ao editar paciente.");
      return res.json();
    } catch (e: any) {
      setErro(e.message);
      return null;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const excluirPaciente = useCallback(async (
    tutorId: string,
    pacienteId: string
  ): Promise<boolean> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(
        `/api/recepcao/tutores/${tutorId}/pacientes/${pacienteId}`,
        { method: "DELETE" }
      );
      return res.ok;
    } catch (e: any) {
      setErro(e.message);
      return false;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const listarSintomas = useCallback(async (): Promise<SintomaDTO[]> => {
    try {
      const res = await apiFetch("/api/recepcao/sintomas");
      if (!res.ok) return [];
      return res.json();
    } catch {
      return [];
    }
  }, [apiFetch]);

  const criarTriagem = useCallback(async (
    tutorId: string,
    pacienteId: string,
    codigosSintomas: string[],
    aplicacaoVacina: boolean = false
  ): Promise<boolean> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(
        `/api/recepcao/tutores/${tutorId}/pacientes/${pacienteId}/triagens`,
        { method: "POST", body: JSON.stringify({ codigosSintomas, aplicacaoVacina }) }
      );
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao criar triagem.");
      }
      return true;
    } catch (e: any) {
      setErro(e.message);
      return false;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const contarVacinasPendentes = useCallback(async (pacienteId: string): Promise<number> => {
    try {
      const res = await apiFetch(`/api/recepcao/pacientes/${pacienteId}/vacinas-pendentes`);
      if (!res.ok) return 0;
      const arr = await res.json();
      return Array.isArray(arr) ? arr.length : 0;
    } catch {
      return 0;
    }
  }, [apiFetch]);

  const cadastrarVacina = useCallback(async (
    pacienteId: string,
    dados: { ciclo: string; totalDoses: number; data: string; tipoProtocolo: string; intervaloDias?: number }
  ): Promise<boolean> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/recepcao/pacientes/${pacienteId}/vacinas`, {
        method: "POST",
        body: JSON.stringify(dados),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao cadastrar vacina.");
      }
      return true;
    } catch (e: any) {
      setErro(e.message);
      return false;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  const listarFila = useCallback(async (): Promise<FilaItemDTO[]> => {
    try {
      const res = await apiFetch("/api/recepcao/fila");
      if (!res.ok) return [];
      return res.json();
    } catch {
      return [];
    }
  }, [apiFetch]);

  const removerDaFila = useCallback(async (triagemId: string): Promise<void> => {
    await apiFetch(`/api/recepcao/fila/${triagemId}`, { method: "DELETE" });
  }, [apiFetch]);

  const listarMedicos = useCallback(async (): Promise<MedicoDTO[]> => {
    try {
      const res = await apiFetch("/api/recepcao/medicos");
      if (!res.ok) return [];
      return res.json();
    } catch {
      return [];
    }
  }, [apiFetch]);

  const encaminharParaMedico = useCallback(async (
    triagemId: string,
    medicoId: string,
    nomeMedico: string
  ): Promise<boolean> => {
    setCarregando(true);
    setErro(null);
    try {
      const res = await apiFetch(`/api/recepcao/fila/${triagemId}/encaminhar`, {
        method: "POST",
        body: JSON.stringify({ medicoId, nomeMedico }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.mensagem || "Erro ao encaminhar.");
      }
      return true;
    } catch (e: any) {
      setErro(e.message);
      return false;
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  return {
    carregando, erro, setErro,
    buscarTutorPorCpf, cadastrarTutor, editarTutor, excluirTutor,
    cadastrarPaciente, editarPaciente, excluirPaciente,
    listarSintomas, criarTriagem,
    contarVacinasPendentes, cadastrarVacina,
    listarFila, removerDaFila,
    listarMedicos, encaminharParaMedico,
  };
}