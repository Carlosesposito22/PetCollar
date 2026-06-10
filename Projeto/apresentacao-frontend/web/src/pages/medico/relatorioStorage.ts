// Armazenamento local e geração de PDF dos relatórios clínicos finalizados (F-10).
//
// O backend mantém os relatórios em repositório em memória (stand-in provisório),
// então persistimos uma cópia no localStorage do navegador para que o histórico de
// triagem consiga reabrir/baixar o relatório assinado mesmo após navegação ou reload.

export type RelatorioSalvo = {
  id: string;
  pacienteId: string;
  triagemId: string;
  nomePet: string;
  nomeTutor: string;
  especie: string;
  raca: string;
  medicoNome: string;
  data: string;                 // dd/mm/aaaa
  tipoRelatorio: string;        // ROTINEIRO | CIRURGICO | PREVENTIVO
  tipoRotulo: string;
  peso: string;
  temperatura: string;
  frequenciaCardiaca: string;
  diagnostico: string;
  resumoTutor: string;
  orientacoes: string;
  cuidadosPosOp: string;
  tempoRecuperacao: string;
  medicamentos: string[];
  anexos: string[];
  assinaturaDataUrl: string;    // PNG base64 da assinatura desenhada
  assinadoEm: string;           // ISO
};

const PREFIXO = "petcollar.relatorios.";

export function salvarRelatorio(r: RelatorioSalvo): void {
  const lista = listarRelatorios(r.pacienteId);
  lista.unshift(r);
  localStorage.setItem(PREFIXO + r.pacienteId, JSON.stringify(lista));
}

export function listarRelatorios(pacienteId: string): RelatorioSalvo[] {
  try {
    const raw = localStorage.getItem(PREFIXO + pacienteId);
    return raw ? (JSON.parse(raw) as RelatorioSalvo[]) : [];
  } catch {
    return [];
  }
}

export function ultimoRelatorio(pacienteId: string): RelatorioSalvo | null {
  return listarRelatorios(pacienteId)[0] ?? null;
}

/** Relatório já emitido para uma triagem específica (1 relatório por atendimento). */
export function relatorioDaTriagem(pacienteId: string, triagemId: string): RelatorioSalvo | null {
  return listarRelatorios(pacienteId).find((r) => r.triagemId === triagemId) ?? null;
}

function esc(v: string): string {
  return (v || "").replace(/[&<>"]/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c] ?? c)
  );
}

function bloco(rotulo: string, valor: string): string {
  if (!valor || !valor.trim()) return "";
  return `
    <div class="bloco">
      <div class="rotulo">${esc(rotulo)}</div>
      <div class="valor">${esc(valor).replace(/\n/g, "<br>")}</div>
    </div>`;
}

/**
 * Gera o PDF do relatório abrindo uma janela de impressão estilizada.
 * O usuário escolhe "Salvar como PDF" no diálogo de impressão.
 */
export function gerarPdfRelatorio(r: RelatorioSalvo): void {
  const win = window.open("", "_blank", "width=840,height=1000");
  if (!win) {
    alert("Permita pop-ups para gerar o PDF do relatório.");
    return;
  }

  const vitais = [
    r.peso ? `Peso: <strong>${esc(r.peso)} kg</strong>` : "",
    r.temperatura ? `Temperatura: <strong>${esc(r.temperatura)} °C</strong>` : "",
    r.frequenciaCardiaca ? `Freq. cardíaca: <strong>${esc(r.frequenciaCardiaca)} bpm</strong>` : "",
  ].filter(Boolean).join(" &nbsp;•&nbsp; ");

  const medicamentos = r.medicamentos.length
    ? `<ul>${r.medicamentos.map((m) => `<li>${esc(m)}</li>`).join("")}</ul>`
    : "<p class='vazio'>Nenhum medicamento prescrito.</p>";

  const anexos = r.anexos.length
    ? `<ul>${r.anexos.map((a) => `<li>📎 ${esc(a)}</li>`).join("")}</ul>`
    : "";

  const cirurgico = r.tipoRelatorio === "CIRURGICO"
    ? bloco("Cuidados Pós-Operatórios", r.cuidadosPosOp) +
      bloco("Tempo de Recuperação Estimado", r.tempoRecuperacao)
    : "";

  const html = `<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<title>Relatório Clínico — ${esc(r.nomePet)}</title>
<style>
  * { box-sizing: border-box; }
  body { font-family: -apple-system, Segoe UI, Roboto, Arial, sans-serif; color: #1f2933; margin: 0; padding: 40px; }
  .topo { display: flex; align-items: center; justify-content: space-between; border-bottom: 3px solid #0d9488; padding-bottom: 16px; margin-bottom: 24px; }
  .logo { font-size: 22px; font-weight: 800; color: #0d9488; letter-spacing: -0.5px; }
  .logo span { color: #ef4444; }
  .doc-tipo { text-align: right; font-size: 12px; color: #6b7280; text-transform: uppercase; letter-spacing: 2px; }
  h1 { font-size: 20px; margin: 0 0 4px; }
  .sub { color: #6b7280; font-size: 13px; margin: 0 0 24px; }
  .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; background: #f0fdfa; border: 1px solid #99f6e4; border-radius: 12px; padding: 16px; margin-bottom: 20px; font-size: 13px; }
  .grid div span { color: #6b7280; }
  .vitais { background: #f9fafb; border-radius: 12px; padding: 12px 16px; font-size: 13px; margin-bottom: 20px; }
  .bloco { margin-bottom: 16px; }
  .rotulo { font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #6b7280; margin-bottom: 4px; }
  .valor { font-size: 14px; line-height: 1.5; }
  .destaque { background: #f0fdfa; border: 1px solid #99f6e4; border-radius: 10px; padding: 12px; }
  h2 { font-size: 14px; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; margin: 24px 0 12px; }
  ul { margin: 0; padding-left: 18px; font-size: 14px; line-height: 1.6; }
  .vazio { color: #9ca3af; font-size: 13px; }
  .assinatura { margin-top: 40px; border-top: 1px dashed #cbd5e1; padding-top: 16px; display: flex; justify-content: flex-end; }
  .assinatura .bloco-assi { text-align: center; }
  .assinatura img { max-height: 90px; display: block; margin: 0 auto; }
  .assinatura .linha { border-top: 1px solid #1f2933; width: 260px; margin-top: 4px; padding-top: 4px; font-size: 12px; color: #374151; }
  .rodape { margin-top: 32px; font-size: 11px; color: #9ca3af; text-align: center; }
  @media print { body { padding: 24px; } .noprint { display: none; } }
</style>
</head>
<body>
  <div class="topo">
    <div class="logo">PET C<span>●</span>LLAR</div>
    <div class="doc-tipo">Relatório Clínico Evolutivo<br>${esc(r.tipoRotulo)}</div>
  </div>

  <h1>${esc(r.nomePet)}</h1>
  <p class="sub">Documento assinado digitalmente • Emitido em ${esc(r.data)}</p>

  <div class="grid">
    <div><span>Tutor:</span> ${esc(r.nomeTutor)}</div>
    <div><span>Espécie / Raça:</span> ${esc(r.especie)} · ${esc(r.raca)}</div>
    <div><span>Médico Responsável:</span> ${esc(r.medicoNome)}</div>
    <div><span>Data do Atendimento:</span> ${esc(r.data)}</div>
  </div>

  ${vitais ? `<div class="vitais">${vitais}</div>` : ""}

  ${bloco("Diagnóstico Técnico", r.diagnostico)}
  ${r.resumoTutor ? `<div class="bloco"><div class="rotulo">Resumo para o Tutor</div><div class="valor destaque">${esc(r.resumoTutor).replace(/\n/g, "<br>")}</div></div>` : ""}
  ${bloco("Orientações de Manejo", r.orientacoes)}
  ${cirurgico}

  <h2>Medicamentos Prescritos</h2>
  ${medicamentos}

  ${anexos ? `<h2>Anexos Clínicos</h2>${anexos}` : ""}

  <div class="assinatura">
    <div class="bloco-assi">
      ${r.assinaturaDataUrl ? `<img src="${r.assinaturaDataUrl}" alt="assinatura">` : ""}
      <div class="linha">${esc(r.medicoNome)} — Médico(a) Veterinário(a)</div>
    </div>
  </div>

  <div class="rodape">petCollar • Documento gerado eletronicamente • RN-122</div>

  <script>
    window.onload = function () { setTimeout(function () { window.print(); }, 300); };
  </script>
</body>
</html>`;

  win.document.open();
  win.document.write(html);
  win.document.close();
}
