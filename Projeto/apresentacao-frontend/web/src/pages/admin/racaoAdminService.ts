type ApiFetch = (input: string, init?: RequestInit) => Promise<Response>;

async function lancarSeErro(res: Response): Promise<Response> {
  if (res.ok) return res;
  const corpo = await res.json().catch(() => ({}) as { mensagem?: string });
  throw new Error(corpo?.mensagem ?? `Falha na requisição (HTTP ${res.status}).`);
}

async function json<T>(entrada: Response | Promise<Response>): Promise<T> {
  return (await lancarSeErro(await entrada)).json() as Promise<T>;
}

// ── Tipos ─────────────────────────────────────────────────────────────────

export type FaixaEtaria = "FILHOTE" | "ADULTO" | "SENIOR";
export type Porte = "PEQUENO" | "MEDIO" | "GRANDE";
export type Comorbidade = "NENHUMA" | "OBESIDADE" | "DIABETES" | "DOENCA_RENAL";

export type RacaoAdminDTO = {
  id: string;
  fabricante: string;
  linha: string;
  descricaoCurta: string;
  densidadeCaloricaKcalPorKg: number | string;
  faixasIndicadas: FaixaEtaria[];
  portesIndicados: Porte[];
  comorbidadesIndicadas: Comorbidade[];
  desativada: boolean;
};

export type RequisicaoRacaoDTO = {
  fabricante: string;
  linha: string;
  densidadeCaloricaKcalPorKg: number;
  faixasIndicadas: FaixaEtaria[];
  portesIndicados: Porte[];
  comorbidadesIndicadas: Comorbidade[];
};

export type DesativacaoResultadoDTO = {
  racao: RacaoAdminDTO;
  planosAfetados: number;
};

export type ImpactoDTO = { planosAfetados: number };

// ── Rótulos ───────────────────────────────────────────────────────────────

export const ROTULOS_FAIXA: Record<FaixaEtaria, string> = {
  FILHOTE: "Filhote", ADULTO: "Adulto", SENIOR: "Sênior",
};
export const ROTULOS_PORTE: Record<Porte, string> = {
  PEQUENO: "Pequeno (<10kg)", MEDIO: "Médio (10-25kg)", GRANDE: "Grande (>25kg)",
};
export const ROTULOS_COMORBIDADE: Record<Comorbidade, string> = {
  NENHUMA: "Nenhuma", OBESIDADE: "Obesidade",
  DIABETES: "Diabetes", DOENCA_RENAL: "Doença Renal",
};

// ── Service ───────────────────────────────────────────────────────────────

export function criarRacoesAdminService(apiFetch: ApiFetch) {
  const base = "/api/admin/nutricao/racoes";
  return {
    listar: (): Promise<RacaoAdminDTO[]> => json(apiFetch(base)),

    criar: (req: RequisicaoRacaoDTO): Promise<RacaoAdminDTO> =>
      json(apiFetch(base, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
      })),

    atualizar: (id: string, req: RequisicaoRacaoDTO): Promise<RacaoAdminDTO> =>
      json(apiFetch(`${base}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
      })),

    desativar: (id: string): Promise<DesativacaoResultadoDTO> =>
      json(apiFetch(`${base}/${id}`, { method: "DELETE" })),

    reativar: (id: string): Promise<RacaoAdminDTO> =>
      json(apiFetch(`${base}/${id}/reativar`, { method: "POST" })),

    impacto: (id: string): Promise<ImpactoDTO> =>
      json(apiFetch(`${base}/${id}/impacto`)),
  };
}
