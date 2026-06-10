import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useRecepcao, type TutorDTO, type PacienteDTO, type SintomaDTO, type DadosPaciente } from "../hooks/useRecepcao";

/** Idade legível a partir da data de nascimento (anos, ou meses se < 1 ano). */
function calcularIdade(nascimento: string | null): string {
  if (!nascimento) return "—";
  const nasc = new Date(nascimento);
  if (Number.isNaN(nasc.getTime())) return "—";
  const hoje = new Date();
  let meses = (hoje.getFullYear() - nasc.getFullYear()) * 12 + (hoje.getMonth() - nasc.getMonth());
  if (hoje.getDate() < nasc.getDate()) meses--;
  if (meses < 0) return "—";
  if (meses < 1) return "< 1 mês";
  if (meses < 12) return `${meses} ${meses === 1 ? "mês" : "meses"}`;
  const anos = Math.floor(meses / 12);
  return `${anos} ${anos === 1 ? "ano" : "anos"}`;
}

function labelSexo(sexo: string | null): string {
  if (sexo === "MACHO") return "Macho";
  if (sexo === "FEMEA") return "Fêmea";
  return "";
}

function formatCpf(v: string) {
  const d = v.replace(/\D/g, "").slice(0, 11);
  return d
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

function formatTel(v: string) {
  const d = v.replace(/\D/g, "").slice(0, 11);
  if (d.length <= 10) return d.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
  return d.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
}

function CorBadge({ cor }: { cor: "VERMELHO" | "AMARELO" | "VERDE" }) {
  const map = {
    VERMELHO: { cls: "bg-red-100 text-red-700 border-red-300", dot: "bg-red-500", label: "Urgente" },
    AMARELO:  { cls: "bg-yellow-100 text-yellow-700 border-yellow-300", dot: "bg-yellow-500", label: "Moderado" },
    VERDE:    { cls: "bg-green-100 text-green-700 border-green-300", dot: "bg-green-500", label: "Leve" },
  } as const;
  const m = map[cor];
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-0.5 text-xs font-semibold ${m.cls}`}>
      <span className={`h-2 w-2 rounded-full ${m.dot}`} />
      {m.label}
    </span>
  );
}

function Modal({ titulo, onClose, children }: {
  titulo: string; onClose: () => void; children: React.ReactNode;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-3xl bg-white p-8 shadow-xl">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-xl font-bold text-ink-900">{titulo}</h2>
          <button onClick={onClose} className="text-xl leading-none text-ink-400 hover:text-ink-700">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

function ModalTutor({ tutor, cpfInicial, onSalvar, onClose }: {
  tutor?: TutorDTO; cpfInicial?: string;
  onSalvar: (d: { cpf: string; nome: string; telefone: string; email: string }) => Promise<void>;
  onClose: () => void;
}) {
  const [cpf, setCpf]     = useState(formatCpf(tutor?.cpf || cpfInicial || ""));
  const [nome, setNome]   = useState(tutor?.nome || "");
  const [tel, setTel]     = useState(tutor?.telefone || "");
  const [email, setEmail] = useState(tutor?.email || "");
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if (!nome.trim()) return;
    setLoading(true);
    await onSalvar({ cpf: cpf.replace(/\D/g, ""), nome, telefone: tel, email });
    setLoading(false);
  }

  return (
    <Modal titulo={tutor ? "Editar Tutor" : "Cadastrar Novo Tutor"} onClose={onClose}>
      <div className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">CPF</label>
          <input value={cpf} onChange={e => setCpf(formatCpf(e.target.value))}
            disabled={!!tutor}
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm disabled:bg-ink-100"
            placeholder="000.000.000-00" />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">Nome completo *</label>
          <input value={nome} onChange={e => setNome(e.target.value)}
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm" />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">Telefone</label>
          <input value={tel} onChange={e => setTel(formatTel(e.target.value))}
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm"
            placeholder="(81) 99999-9999" />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">E-mail</label>
          <input value={email} onChange={e => setEmail(e.target.value)} type="email"
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm" />
        </div>
        <div className="flex gap-3 pt-2">
          <button onClick={onClose} className="btn-ghost flex-1">Cancelar</button>
          <button onClick={handleSubmit} disabled={loading || !nome.trim()}
            className="flex-1 rounded-xl bg-ink-900 px-6 py-2.5 text-sm font-semibold text-white hover:bg-ink-700 disabled:opacity-50">
            {loading ? "Salvando..." : "Salvar"}
          </button>
        </div>
      </div>
    </Modal>
  );
}

function ModalPaciente({ paciente, onSalvar, onClose }: {
  paciente?: PacienteDTO;
  onSalvar: (d: DadosPaciente) => Promise<void>;
  onClose: () => void;
}) {
  const [nome, setNome]   = useState(paciente?.nome || "");
  const [esp, setEsp]     = useState(paciente?.especie || "");
  const [raca, setRaca]   = useState(paciente?.raca || "");
  const [nasc, setNasc]   = useState(paciente?.nascimento?.slice(0, 10) || "");
  const [peso, setPeso]   = useState(paciente?.pesoKg != null ? String(paciente.pesoKg) : "");
  const [sexo, setSexo]   = useState(paciente?.sexo || "");
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if (!nome.trim()) return;
    setLoading(true);
    const pesoNum = peso.trim() === "" ? null : Number(peso.replace(",", "."));
    await onSalvar({
      nome, especie: esp, raca, nascimento: nasc,
      pesoKg: pesoNum != null && !Number.isNaN(pesoNum) ? pesoNum : null,
      sexo,
    });
    setLoading(false);
  }

  const idadePreview = calcularIdade(nasc || null);

  return (
    <Modal titulo={paciente ? "Editar Paciente" : "Cadastrar Paciente"} onClose={onClose}>
      <div className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">Nome *</label>
          <input value={nome} onChange={e => setNome(e.target.value)}
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="mb-1 block text-sm font-medium text-ink-700">Espécie</label>
            <select value={esp} onChange={e => setEsp(e.target.value)}
              className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm">
              <option value="">—</option>
              <option>Cão</option><option>Gato</option><option>Ave</option>
              <option>Réptil</option><option>Roedor</option><option>Outro</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-ink-700">Raça</label>
            <input value={raca} onChange={e => setRaca(e.target.value)}
              className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm" />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="mb-1 block text-sm font-medium text-ink-700">Sexo</label>
            <select value={sexo} onChange={e => setSexo(e.target.value)}
              className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm">
              <option value="">—</option>
              <option value="MACHO">Macho</option>
              <option value="FEMEA">Fêmea</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-ink-700">Peso (kg)</label>
            <input value={peso} onChange={e => setPeso(e.target.value)}
              type="number" step="0.1" min="0" inputMode="decimal"
              className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm"
              placeholder="0.0" />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-ink-700">Nascimento</label>
          <input type="date" value={nasc} onChange={e => setNasc(e.target.value)}
            className="w-full rounded-xl border border-ink-300 px-4 py-2.5 text-sm" />
          {nasc && (
            <p className="mt-1 text-xs text-ink-500">Idade calculada: <strong>{idadePreview}</strong></p>
          )}
        </div>
        <div className="flex gap-3 pt-2">
          <button onClick={onClose} className="btn-ghost flex-1">Cancelar</button>
          <button onClick={handleSubmit} disabled={loading || !nome.trim()}
            className="flex-1 rounded-xl bg-ink-900 px-6 py-2.5 text-sm font-semibold text-white hover:bg-ink-700 disabled:opacity-50">
            {loading ? "Salvando..." : "Salvar"}
          </button>
        </div>
      </div>
    </Modal>
  );
}

function ModalTriagem({ paciente, sintomas, onSalvar, onClose }: {
  paciente: PacienteDTO; sintomas: SintomaDTO[];
  onSalvar: (codigos: string[]) => Promise<void>;
  onClose: () => void;
}) {
  const [selecionados, setSel] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  const score = sintomas
    .filter(s => selecionados.includes(s.codigo))
    .reduce((a, s) => a + s.peso, 0);
  const cor: "VERMELHO" | "AMARELO" | "VERDE" =
    score >= 10 ? "VERMELHO" : score >= 5 ? "AMARELO" : "VERDE";

  function toggle(codigo: string) {
    setSel(prev => prev.includes(codigo) ? prev.filter(c => c !== codigo) : [...prev, codigo]);
  }

  async function handleSubmit() {
    setLoading(true);
    await onSalvar(selecionados);
    setLoading(false);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="flex w-full max-w-2xl flex-col gap-6 rounded-3xl bg-white p-8 shadow-xl overflow-y-auto max-h-[90vh]">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-ink-900">Triagem Clínica</h2>
            <p className="text-sm text-ink-500">Paciente: <strong>{paciente.nome}</strong></p>
          </div>
          <button onClick={onClose} className="text-xl text-ink-400 hover:text-ink-700">✕</button>
        </div>

        <div className="flex items-center gap-4 rounded-2xl border bg-ink-50 p-4">
          <div className="flex-1">
            <p className="text-xs font-semibold uppercase tracking-widest text-ink-500">Score total</p>
            <p className="text-3xl font-bold text-ink-900">{score}</p>
          </div>
          <div className="text-right">
            <p className="mb-1 text-xs font-semibold uppercase tracking-widest text-ink-500">Classificação</p>
            <CorBadge cor={cor} />
          </div>
        </div>

        <div>
          <p className="mb-3 text-sm font-semibold text-ink-700">Selecione os sintomas:</p>
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
            {sintomas.map(s => (
              <button key={s.codigo} onClick={() => toggle(s.codigo)}
                className={`flex items-center justify-between rounded-xl border px-4 py-3 text-left text-sm transition ${
                  selecionados.includes(s.codigo)
                    ? "border-brand-500 bg-brand-50 text-brand-900"
                    : "border-ink-200 hover:border-ink-400"
                }`}>
                <span>{s.descricao}</span>
                <span className={`ml-2 rounded-full px-2 py-0.5 text-xs font-bold ${
                  selecionados.includes(s.codigo)
                    ? "bg-brand-200 text-brand-800"
                    : "bg-ink-100 text-ink-600"
                }`}>+{s.peso}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="flex gap-3">
          <button onClick={onClose} className="btn-ghost flex-1">Cancelar</button>
          <button onClick={handleSubmit} disabled={loading || selecionados.length === 0}
            className="flex-1 rounded-xl bg-ink-900 px-6 py-2.5 text-sm font-semibold text-white hover:bg-ink-700 disabled:opacity-50">
            {loading ? "Finalizando..." : "Finalizar Triagem"}
          </button>
        </div>
      </div>
    </div>
  );
}

export function BuscaTutorPage() {
  const navigate = useNavigate();
  const recepcao = useRecepcao();

  const [cpf, setCpf]                       = useState("");
  const [buscando, setBuscando]             = useState(false);
  const [tutor, setTutor]                   = useState<TutorDTO | null>(null);
  const [naoEncontrado, setNaoEncontrado]   = useState(false);
  const [erroBusca, setErroBusca]           = useState<string | null>(null);

  const [modalTutor, setModalTutor]               = useState<"cadastrar" | "editar" | null>(null);
  const [modalPaciente, setModalPaciente]         = useState<{ modo: "cadastrar" | "editar"; paciente?: PacienteDTO } | null>(null);
  const [modalTriagem, setModalTriagem]           = useState<PacienteDTO | null>(null);
  const [sintomas, setSintomas]                   = useState<SintomaDTO[]>([]);
  const [confirmExcluirTutor, setConfirmExcluirTutor] = useState(false);
  const [confirmExcluirPac, setConfirmExcluirPac]     = useState<PacienteDTO | null>(null);
  const [toastMsg, setToastMsg]                   = useState<string | null>(null);

  function toast(msg: string) {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(null), 3000);
  }

  async function buscar() {
    const cpfLimpo = cpf.replace(/\D/g, "");
    if (cpfLimpo.length !== 11) { setErroBusca("CPF deve ter 11 dígitos."); return; }
    setBuscando(true);
    setTutor(null);
    setNaoEncontrado(false);
    setErroBusca(null);
    const resultado = await recepcao.buscarTutorPorCpf(cpfLimpo);
    setBuscando(false);
    if (resultado) {
      setTutor(resultado);
    } else if (recepcao.erro) {
      setErroBusca(recepcao.erro);
    } else {
      setNaoEncontrado(true);
    }
  }

  async function abrirTriagem(paciente: PacienteDTO) {
    if (sintomas.length === 0) {
      const s = await recepcao.listarSintomas();
      setSintomas(s);
    }
    setModalTriagem(paciente);
  }

  return (
    <main className="mx-auto max-w-4xl px-6 py-10">
      {toastMsg && (
        <div className="fixed bottom-6 right-6 z-50 rounded-2xl bg-ink-900 px-6 py-3 text-sm text-white shadow-lg">
          {toastMsg}
        </div>
      )}

      {modalTutor === "cadastrar" && (
        <ModalTutor cpfInicial={cpf} onClose={() => setModalTutor(null)}
          onSalvar={async (dados) => {
            const novo = await recepcao.cadastrarTutor(dados);
            if (novo) { setTutor(novo); setNaoEncontrado(false); setModalTutor(null); toast("Tutor cadastrado!"); }
          }} />
      )}
      {modalTutor === "editar" && tutor && (
        <ModalTutor tutor={tutor} onClose={() => setModalTutor(null)}
          onSalvar={async (dados) => {
            const at = await recepcao.editarTutor(tutor.id, dados);
            if (at) { setTutor(at); setModalTutor(null); toast("Tutor atualizado!"); }
          }} />
      )}
      {modalPaciente && tutor && (
        <ModalPaciente paciente={modalPaciente.paciente} onClose={() => setModalPaciente(null)}
          onSalvar={async (dados) => {
            if (modalPaciente.modo === "cadastrar") {
              const novo = await recepcao.cadastrarPaciente(tutor.id, dados);
              if (novo) {
                setTutor(t => t ? { ...t, pacientes: [...t.pacientes, novo] } : t);
                setModalPaciente(null); toast("Paciente cadastrado!");
              }
            } else if (modalPaciente.paciente) {
              const at = await recepcao.editarPaciente(tutor.id, modalPaciente.paciente.id, dados);
              if (at) {
                setTutor(t => t ? { ...t, pacientes: t.pacientes.map(p => p.id === at.id ? at : p) } : t);
                setModalPaciente(null); toast("Paciente atualizado!");
              }
            }
          }} />
      )}
      {confirmExcluirTutor && tutor && (
        <Modal titulo="Excluir Tutor" onClose={() => setConfirmExcluirTutor(false)}>
          <p className="mb-6 text-sm text-ink-600">Tem certeza? Todos os pacientes vinculados também serão excluídos.</p>
          <div className="flex gap-3">
            <button onClick={() => setConfirmExcluirTutor(false)} className="btn-ghost flex-1">Cancelar</button>
            <button onClick={async () => {
              const ok = await recepcao.excluirTutor(tutor.id);
              if (ok) { setTutor(null); setNaoEncontrado(false); setCpf(""); setConfirmExcluirTutor(false); toast("Tutor excluído."); }
            }} className="flex-1 rounded-xl bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700">
              Excluir
            </button>
          </div>
        </Modal>
      )}
      {confirmExcluirPac && tutor && (
        <Modal titulo="Excluir Paciente" onClose={() => setConfirmExcluirPac(null)}>
          <p className="mb-6 text-sm text-ink-600">Excluir <strong>{confirmExcluirPac.nome}</strong>?</p>
          <div className="flex gap-3">
            <button onClick={() => setConfirmExcluirPac(null)} className="btn-ghost flex-1">Cancelar</button>
            <button onClick={async () => {
              const ok = await recepcao.excluirPaciente(tutor.id, confirmExcluirPac.id);
              if (ok) {
                setTutor(t => t ? { ...t, pacientes: t.pacientes.filter(p => p.id !== confirmExcluirPac.id) } : t);
                setConfirmExcluirPac(null); toast("Paciente excluído.");
              }
            }} className="flex-1 rounded-xl bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700">
              Excluir
            </button>
          </div>
        </Modal>
      )}
      {modalTriagem && tutor && (
        <ModalTriagem paciente={modalTriagem} sintomas={sintomas}
          onClose={() => setModalTriagem(null)}
          onSalvar={async (codigos) => {
            const ok = await recepcao.criarTriagem(tutor.id, modalTriagem.id, codigos);
            if (ok) { setModalTriagem(null); toast("Triagem finalizada! Paciente adicionado à fila."); }
          }} />
      )}

      <button onClick={() => navigate("/recepcao")}
        className="mb-8 flex items-center gap-2 text-sm text-ink-500 hover:text-ink-900 transition">
        ← Voltar ao Painel
      </button>
      <h1 className="mb-8 text-3xl font-bold text-ink-900">Busca e Prontuário do Tutor</h1>

      {/* Barra de busca */}
      <div className="mb-6 rounded-3xl border border-ink-200 bg-white p-6 shadow-card">
        <div className="flex gap-3">
          <input value={cpf} onChange={e => setCpf(formatCpf(e.target.value))}
            onKeyDown={e => e.key === "Enter" && buscar()}
            placeholder="000.000.000-00"
            className="flex-1 rounded-xl border border-ink-300 px-5 py-3 text-base outline-none focus:border-ink-900 focus:ring-2 focus:ring-ink-100" />
          <button onClick={buscar} disabled={buscando}
            className="rounded-xl bg-ink-900 px-8 py-3 font-semibold text-white hover:bg-ink-700 disabled:opacity-50 transition">
            {buscando ? "Buscando..." : "Localizar Tutor"}
          </button>
        </div>
        {erroBusca && <p className="mt-3 text-sm text-red-600">{erroBusca}</p>}
      </div>

      {/* Não encontrado */}
      {naoEncontrado && (
        <div className="rounded-3xl border-2 border-red-300 bg-red-50 p-8 text-center">
          <p className="mb-4 font-semibold text-red-700">Tutor não encontrado no sistema</p>
          <button onClick={() => setModalTutor("cadastrar")}
            className="rounded-xl bg-ink-900 px-8 py-3 text-sm font-semibold text-white hover:bg-ink-700">
            Cadastrar novo Tutor
          </button>
        </div>
      )}

      {/* Resultado */}
      {tutor && (
        <div className="flex flex-col gap-6">
          {tutor.alertaEpidemiologico && (
            <div className="flex items-start gap-4 rounded-3xl border-2 border-orange-300 bg-orange-50 p-6">
              <span className="text-2xl">⚠️</span>
              <div>
                <p className="font-bold text-orange-800">Alerta Epidemiológico</p>
                <p className="mt-1 text-sm text-orange-700">
                  Um ou mais animais deste tutor tiveram diagnóstico infectocontagioso nos últimos 40 dias.
                  Este tutor tem prioridade na fila de atendimento.
                </p>
              </div>
            </div>
          )}

          <div className="rounded-3xl border border-ink-200 bg-white p-8 shadow-card">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-2xl font-bold text-ink-900">{tutor.nome}</h2>
                <p className="mt-1 text-sm text-ink-500">CPF: {formatCpf(tutor.cpf)}</p>
                {tutor.telefone && <p className="text-sm text-ink-500">Tel: {tutor.telefone}</p>}
                {tutor.email && <p className="text-sm text-ink-500">E-mail: {tutor.email}</p>}
              </div>
              <div className="flex gap-2">
                <button onClick={() => setModalTutor("editar")}
                  className="rounded-xl border border-ink-300 px-4 py-2 text-sm font-medium text-ink-700 hover:bg-ink-50 transition">
                  ✏️ Editar
                </button>
                <button onClick={() => setConfirmExcluirTutor(true)}
                  className="rounded-xl border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 transition">
                  🗑 Excluir
                </button>
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-ink-200 bg-white p-8 shadow-card">
            <div className="mb-5 flex items-center justify-between">
              <h3 className="text-lg font-bold text-ink-900">
                Animais vinculados ({tutor.pacientes.length})
              </h3>
              <button onClick={() => setModalPaciente({ modo: "cadastrar" })}
                className="rounded-xl bg-ink-900 px-4 py-2 text-sm font-semibold text-white hover:bg-ink-700 transition">
                + Adicionar paciente
              </button>
            </div>

            {tutor.pacientes.length === 0 ? (
              <p className="text-sm text-ink-500">Nenhum animal cadastrado ainda.</p>
            ) : (
              <div className="flex flex-col gap-3">
                {tutor.pacientes.map(p => (
                  <div key={p.id}
                    className="flex items-center gap-4 rounded-2xl border border-ink-200 bg-ink-50/50 p-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-ink-900">{p.nome}</span>
                        {p.infectocontagiosoRecente && (
                          <span className="rounded-full border border-orange-300 bg-orange-100 px-2 py-0.5 text-xs font-semibold text-orange-700">
                            ⚠ Infectocontagioso
                          </span>
                        )}
                      </div>
                      <p className="mt-0.5 text-xs text-ink-500">
                        {[
                          [p.especie, p.raca].filter(Boolean).join(" "),
                          labelSexo(p.sexo),
                          p.pesoKg != null ? `${p.pesoKg} kg` : "",
                          p.nascimento ? `${calcularIdade(p.nascimento)}` : "",
                        ].filter(Boolean).join(" · ")}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => abrirTriagem(p)}
                        className="rounded-lg border border-brand-200 bg-brand-50 px-3 py-1.5 text-xs font-semibold text-brand-700 hover:bg-brand-100 transition">
                        🩺 Triagem
                      </button>
                      <button onClick={() => setModalPaciente({ modo: "editar", paciente: p })}
                        className="rounded-lg border border-ink-200 px-3 py-1.5 text-xs font-medium text-ink-600 hover:bg-ink-100 transition">
                        ✏️
                      </button>
                      <button onClick={() => setConfirmExcluirPac(p)}
                        className="rounded-lg border border-red-100 px-3 py-1.5 text-xs font-medium text-red-500 hover:bg-red-50 transition">
                        🗑
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      <div className="mt-8 rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-6 text-sm text-ink-500">
        ANOTAÇÃO: Alerta Epidemiológico aparece automaticamente se Paciente teve diagnóstico
        infectocontagioso nos últimos 40 dias. Força Paciente ao topo da Fila de Espera Dinâmica
      </div>
    </main>
  );
}