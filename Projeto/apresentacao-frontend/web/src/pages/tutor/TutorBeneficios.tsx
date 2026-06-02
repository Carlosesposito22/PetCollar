import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../auth/AuthContext";

type StatusBeneficio = "DISPONIVEL" | "EM_CARENCIA" | "ESGOTADO";
type PeriodoRenovacao = "MENSAL" | "TRIMESTRAL" | "ANUAL";

type BeneficioItem = {
  id: string;
  nome: string;
  status: StatusBeneficio;
  usosRestantes: number;
  limiteUsosPorPeriodo: number;
  periodoRenovacao: PeriodoRenovacao;
  dataReferencia: string | null; // data de liberação (EM_CARENCIA) ou renovação (ESGOTADO)
  carenciaDias: number;
};

type TicketGerado = {
  codigoGUID: string;
  expiraEm: string; // ISO datetime
  nomeBeneficio: string;
};

// Dados simulados enquanto o endpoint /api/tutor/beneficios não está disponível
const MOCK: BeneficioItem[] = [
  {
    id: "1",
    nome: "Consulta Veterinária",
    status: "DISPONIVEL",
    usosRestantes: 2,
    limiteUsosPorPeriodo: 4,
    periodoRenovacao: "TRIMESTRAL",
    dataReferencia: null,
    carenciaDias: 30,
  },
  {
    id: "2",
    nome: "Vacinação Anual",
    status: "EM_CARENCIA",
    usosRestantes: 1,
    limiteUsosPorPeriodo: 1,
    periodoRenovacao: "ANUAL",
    dataReferencia: "2026-05-15",
    carenciaDias: 90,
  },
  {
    id: "3",
    nome: "Exame de Sangue",
    status: "ESGOTADO",
    usosRestantes: 0,
    limiteUsosPorPeriodo: 2,
    periodoRenovacao: "ANUAL",
    dataReferencia: "2027-05-01",
    carenciaDias: 60,
  },
  {
    id: "4",
    nome: "Banho e Tosa",
    status: "DISPONIVEL",
    usosRestantes: 4,
    limiteUsosPorPeriodo: 6,
    periodoRenovacao: "MENSAL",
    dataReferencia: null,
    carenciaDias: 0,
  },
];

const STATUS_CONFIG: Record<StatusBeneficio, { label: string; badge: string; dot: string }> = {
  DISPONIVEL: {
    label: "Disponível",
    badge: "text-emerald-700 bg-emerald-50 ring-1 ring-emerald-200",
    dot: "bg-emerald-500",
  },
  EM_CARENCIA: {
    label: "Em Carência",
    badge: "text-amber-700 bg-amber-50 ring-1 ring-amber-200",
    dot: "bg-amber-400",
  },
  ESGOTADO: {
    label: "Esgotado",
    badge: "text-paw-700 bg-paw-50 ring-1 ring-paw-200",
    dot: "bg-paw-500",
  },
};

const PERIODO_LABEL: Record<PeriodoRenovacao, string> = {
  MENSAL: "Mensal",
  TRIMESTRAL: "Trimestral",
  ANUAL: "Anual",
};

function gerarGuid() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    return (c === "x" ? r : (r & 0x3) | 0x8).toString(16);
  });
}

function formatarData(iso: string) {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
}

function formatarDataHora(iso: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(iso));
}

export function TutorBeneficios() {
  const { apiFetch } = useAuth();
  const [beneficios, setBeneficios] = useState<BeneficioItem[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [ticket, setTicket] = useState<TicketGerado | null>(null);
  const [gerando, setGerando] = useState<string | null>(null);
  const [copiado, setCopiado] = useState(false);

  const carregar = useCallback(async () => {
    try {
      const res = await apiFetch("/api/tutor/beneficios");
      if (res.ok) {
        setBeneficios(await res.json());
        return;
      }
    } catch { /* API ainda não disponível */ }
    setBeneficios(MOCK);
  }, [apiFetch]);

  useEffect(() => {
    void carregar().finally(() => setCarregando(false));
  }, [carregar]);

  async function usar(b: BeneficioItem) {
    setGerando(b.id);
    try {
      let sucesso = false;
      try {
        const res = await apiFetch(`/api/tutor/beneficios/${b.id}/usar`, { method: "POST" });
        if (res.ok) {
          const t = (await res.json()) as TicketGerado;
          setTicket({ ...t, nomeBeneficio: b.nome });
          await carregar();
          sucesso = true;
        }
      } catch { /* API ainda não disponível */ }

      if (!sucesso) {
        const expira = new Date(Date.now() + 48 * 60 * 60 * 1000);
        setTicket({
          codigoGUID: gerarGuid(),
          expiraEm: expira.toISOString(),
          nomeBeneficio: b.nome,
        });
        setBeneficios(prev =>
          prev.map(item =>
            item.id === b.id
              ? {
                  ...item,
                  usosRestantes: Math.max(0, item.usosRestantes - 1),
                  status: item.usosRestantes - 1 <= 0 ? "ESGOTADO" : "DISPONIVEL",
                }
              : item
          )
        );
      }
    } finally {
      setGerando(null);
    }
  }

  async function copiarGuid(guid: string) {
    await navigator.clipboard.writeText(guid);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2000);
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-ink-900">Painel de Benefícios</h1>
        <p className="mt-1 text-sm text-ink-500">
          Acompanhe e utilize os benefícios inclusos no seu plano de saúde veterinário.
        </p>
      </div>

      {carregando ? (
        <div className="card h-48 animate-pulse bg-white/60" />
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-ink-100">
                  <th className="px-5 py-3.5 text-left font-semibold text-ink-700">Benefício</th>
                  <th className="px-5 py-3.5 text-left font-semibold text-ink-700">Status</th>
                  <th className="px-5 py-3.5 text-left font-semibold text-ink-700">Usos Restantes</th>
                  <th className="px-5 py-3.5 text-left font-semibold text-ink-700">
                    Data de Liberação/Renovação
                  </th>
                  <th className="px-5 py-3.5 text-left font-semibold text-ink-700">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {beneficios.map(b => {
                  const s = STATUS_CONFIG[b.status];
                  return (
                    <tr key={b.id} className="transition hover:bg-ink-100/40">
                      <td className="px-5 py-4 font-medium text-ink-800">{b.nome}</td>
                      <td className="px-5 py-4">
                        <span
                          className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${s.badge}`}
                        >
                          <span className={`h-1.5 w-1.5 rounded-full ${s.dot}`} />
                          {s.label}
                        </span>
                      </td>
                      <td className="px-5 py-4">
                        <span
                          className={
                            b.usosRestantes === 0
                              ? "font-semibold text-paw-600"
                              : "font-semibold text-ink-800"
                          }
                        >
                          {b.usosRestantes}
                        </span>
                        <span className="text-ink-400"> / {b.limiteUsosPorPeriodo}</span>
                      </td>
                      <td className="px-5 py-4 text-ink-600">
                        {b.dataReferencia ? (
                          formatarData(b.dataReferencia)
                        ) : (
                          <span className="text-ink-400">—</span>
                        )}
                      </td>
                      <td className="px-5 py-4">
                        {b.status === "DISPONIVEL" && (
                          <button
                            onClick={() => usar(b)}
                            disabled={gerando === b.id}
                            className="rounded-lg border border-ink-300 bg-white px-3 py-1.5 text-xs font-medium text-ink-700 shadow-sm transition hover:bg-ink-100 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            {gerando === b.id ? "Gerando…" : "Usar"}
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <section className="space-y-5">
        <h2 className="text-lg font-bold text-ink-900">Como funcionam os benefícios</h2>

        <div className="grid gap-4 sm:grid-cols-3">
          <StatusExplicacao
            status="DISPONIVEL"
            descricao="Pronto para uso. Clique em 'Usar' para gerar um ticket com código GUID único, válido por 48 horas. Apresente o código ao atendente no momento do serviço."
          />
          <StatusExplicacao
            status="EM_CARENCIA"
            descricao="Aguardando fim do período de carência. Esse tempo de espera começa na data de contratação do plano e existe para equilibrar o uso do sistema de saúde veterinário."
          />
          <StatusExplicacao
            status="ESGOTADO"
            descricao="Todos os usos do período foram consumidos. O contador é reposto automaticamente na data de renovação conforme o ciclo do benefício."
          />
        </div>

        <div className="card p-5 space-y-4">
          <h3 className="font-semibold text-ink-800">Entendendo a carência</h3>
          <p className="text-sm text-ink-600 leading-relaxed">
            A <strong>carência</strong> é um período de espera obrigatório contado a partir da data de
            contratação do plano. Ela evita que procedimentos de alto custo sejam utilizados
            imediatamente após a adesão, garantindo sustentabilidade ao sistema de saúde veterinária.
          </p>
          <div className="grid gap-3 sm:grid-cols-2">
            <CarenciaItem
              titulo="Consultas veterinárias"
              dias={30}
              descricao="Carência curta para cobrir atendimentos de rotina e check-ups."
            />
            <CarenciaItem
              titulo="Vacinação"
              dias={90}
              descricao="Carência moderada para imunizações do ciclo anual do pet."
            />
            <CarenciaItem
              titulo="Exames laboratoriais"
              dias={60}
              descricao="Inclui hemograma, urinálise, bioquímica e exames de imagem."
            />
            <CarenciaItem
              titulo="Procedimentos cirúrgicos"
              dias={180}
              descricao="Carência longa para cirurgias eletivas e de emergência cobertas pelo plano."
            />
          </div>
          <p className="text-xs text-ink-400 pt-1">
            Os prazos de carência são definidos no contrato do plano e podem variar conforme a
            categoria do benefício.
          </p>
        </div>

        <div className="card p-5">
          <h3 className="font-semibold text-ink-800 mb-4">Ciclos de renovação</h3>
          <div className="grid gap-3 sm:grid-cols-3">
            <RenovacaoItem
              periodo="MENSAL"
              descricao="Usos repõem a cada mês. Ideal para serviços de higiene e rotina como banho, tosa e escovação."
            />
            <RenovacaoItem
              periodo="TRIMESTRAL"
              descricao="Usos repõem a cada 3 meses. Comum em consultas de acompanhamento e controle de parasitas."
            />
            <RenovacaoItem
              periodo="ANUAL"
              descricao="Usos repõem uma vez por ano. Aplicado em vacinações e exames de check-up anuais."
            />
          </div>
        </div>
      </section>

      {ticket && (
        <TicketModal
          ticket={ticket}
          copiado={copiado}
          onCopiar={() => copiarGuid(ticket.codigoGUID)}
          onFechar={() => { setTicket(null); setCopiado(false); }}
        />
      )}
    </div>
  );
}

function StatusExplicacao({
  status,
  descricao,
}: {
  status: StatusBeneficio;
  descricao: string;
}) {
  const s = STATUS_CONFIG[status];
  return (
    <div className="card p-4 space-y-2">
      <span
        className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${s.badge}`}
      >
        <span className={`h-1.5 w-1.5 rounded-full ${s.dot}`} />
        {s.label}
      </span>
      <p className="text-sm text-ink-600 leading-relaxed">{descricao}</p>
    </div>
  );
}

function CarenciaItem({
  titulo,
  dias,
  descricao,
}: {
  titulo: string;
  dias: number;
  descricao: string;
}) {
  return (
    <div className="flex gap-3">
      <div className="mt-0.5 flex-shrink-0">
        <span className="inline-flex h-6 w-12 items-center justify-center rounded-full bg-brand-50 text-xs font-bold text-brand-700 ring-1 ring-brand-100">
          {dias}d
        </span>
      </div>
      <div>
        <p className="text-sm font-medium text-ink-700">{titulo}</p>
        <p className="text-xs text-ink-500">{descricao}</p>
      </div>
    </div>
  );
}

function RenovacaoItem({
  periodo,
  descricao,
}: {
  periodo: PeriodoRenovacao;
  descricao: string;
}) {
  return (
    <div className="rounded-xl bg-ink-100/60 p-3 space-y-1">
      <p className="font-semibold text-ink-800">{PERIODO_LABEL[periodo]}</p>
      <p className="text-xs text-ink-500 leading-relaxed">{descricao}</p>
    </div>
  );
}

function TicketModal({
  ticket,
  copiado,
  onCopiar,
  onFechar,
}: {
  ticket: TicketGerado;
  copiado: boolean;
  onCopiar: () => void;
  onFechar: () => void;
}) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={onFechar}
    >
      <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
        <div className="mb-4 flex items-center gap-2">
          <span className="text-2xl" aria-hidden>🎟️</span>
          <h3 className="text-lg font-bold text-ink-900">Ticket Gerado</h3>
        </div>

        <p className="text-sm text-ink-600">
          Benefício: <strong className="text-ink-800">{ticket.nomeBeneficio}</strong>
        </p>
        <p className="mt-0.5 text-xs text-ink-400 mb-4">
          Válido até: {formatarDataHora(ticket.expiraEm)}
        </p>

        <div className="rounded-xl bg-ink-100 px-4 py-5 text-center">
          <p className="break-all font-mono text-sm font-bold tracking-wide text-ink-900">
            {ticket.codigoGUID}
          </p>
        </div>

        <button
          onClick={onCopiar}
          className="mt-3 w-full rounded-xl border border-ink-300 bg-white py-2.5 text-sm font-medium text-ink-700 shadow-sm transition hover:bg-ink-100"
        >
          {copiado ? "Copiado!" : "Copiar código"}
        </button>

        <p className="mt-4 text-center text-xs leading-relaxed text-ink-400">
          Apresente este código ao atendente no momento do serviço.
          <br />O ticket expira automaticamente após 48 horas.
        </p>

        <button onClick={onFechar} className="btn-primary mt-4">
          Fechar
        </button>
      </div>
    </div>
  );
}
