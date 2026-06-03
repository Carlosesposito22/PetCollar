/** Formatação padrão petCollar (pt-BR). */

export function formatarReal(valor: number | string | null | undefined): string {
  if (valor === null || valor === undefined || valor === "") return "—";
  const n = typeof valor === "string" ? Number(valor) : valor;
  if (Number.isNaN(n)) return "—";
  return n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function formatarData(iso: string | null | undefined): string {
  if (!iso) return "—";
  // ISO date "yyyy-MM-dd"
  const [a, m, d] = iso.split("-");
  if (!a || !m || !d) return iso;
  return `${d}/${m}/${a}`;
}

export function formatarCompetencia(yyyyMM: string | null | undefined): string {
  if (!yyyyMM) return "—";
  const [a, m] = yyyyMM.split("-");
  if (!a || !m) return yyyyMM;
  return `${m}/${a}`;
}
