"""
remover_comentarios.py
Remove todos os comentários Java (// e /* */) dos arquivos .java do projeto,
preservando literais de string e text blocks (Java 13+).

Uso:
    python remover_comentarios.py           # dry-run: mostra o que mudaria
    python remover_comentarios.py --aplicar # aplica as alterações nos arquivos
"""

import sys
import os
from pathlib import Path

RAIZ_PROJETO = Path(__file__).parent / "Projeto"


def remover_comentarios_java(fonte: str) -> str:
    """
    Remove comentários // e /* */ de código Java via máquina de estados,
    sem alterar o conteúdo de strings nem text blocks.
    """
    resultado = []
    i = 0
    n = len(fonte)

    while i < n:
        c = fonte[i]

        # Text block Java 13+: """..."""
        if fonte[i:i+3] == '"""':
            j = i + 3
            # avança até fechar com """
            while j < n and fonte[j:j+3] != '"""':
                j += 1
            j += 3  # inclui o fechamento
            resultado.append(fonte[i:j])
            i = j

        # String literal "..."
        elif c == '"':
            j = i + 1
            while j < n:
                if fonte[j] == '\\':
                    j += 2
                    continue
                if fonte[j] == '"':
                    j += 1
                    break
                j += 1
            resultado.append(fonte[i:j])
            i = j

        # Char literal '.'
        elif c == "'":
            j = i + 1
            while j < n:
                if fonte[j] == '\\':
                    j += 2
                    continue
                if fonte[j] == "'":
                    j += 1
                    break
                j += 1
            resultado.append(fonte[i:j])
            i = j

        # Comentário de linha: //
        elif fonte[i:i+2] == '//':
            # pula tudo até o fim da linha (sem consumir o \n)
            j = i + 2
            while j < n and fonte[j] != '\n':
                j += 1
            i = j  # o \n será adicionado na próxima iteração normalmente

        # Comentário de bloco: /* ... */ ou /** ... */
        elif fonte[i:i+2] == '/*':
            j = i + 2
            while j < n:
                if fonte[j:j+2] == '*/':
                    j += 2
                    break
                j += 1
            i = j

        else:
            resultado.append(c)
            i += 1

    return ''.join(resultado)


def limpar_linhas_em_branco(fonte: str) -> str:
    """
    Após remover comentários:
    - remove espaço em branco à direita de cada linha
    - colapsa mais de uma linha em branco consecutiva para no máximo uma
    - garante que o arquivo termina com exatamente um \n
    """
    linhas = fonte.splitlines()
    resultado = []
    em_branco_consecutivas = 0

    for linha in linhas:
        linha_limpa = linha.rstrip()
        if linha_limpa == '':
            em_branco_consecutivas += 1
            if em_branco_consecutivas <= 1:
                resultado.append('')
        else:
            em_branco_consecutivas = 0
            resultado.append(linha_limpa)

    # remove linhas em branco no início do arquivo
    while resultado and resultado[0] == '':
        resultado.pop(0)

    return '\n'.join(resultado) + '\n'


def processar_arquivo(caminho: Path, aplicar: bool) -> tuple[int, int, bool]:
    """
    Processa um arquivo .java.
    Retorna (linhas_antes, linhas_depois, houve_alteracao).
    """
    texto_original = caminho.read_text(encoding='utf-8')
    sem_comentarios = remover_comentarios_java(texto_original)
    texto_final = limpar_linhas_em_branco(sem_comentarios)

    linhas_antes = texto_original.count('\n')
    linhas_depois = texto_final.count('\n')
    houve_alteracao = texto_original != texto_final

    if houve_alteracao and aplicar:
        caminho.write_text(texto_final, encoding='utf-8')

    return linhas_antes, linhas_depois, houve_alteracao


def main():
    aplicar = '--aplicar' in sys.argv

    if not RAIZ_PROJETO.exists():
        print(f"[ERRO] Diretório não encontrado: {RAIZ_PROJETO}")
        sys.exit(1)

    arquivos = sorted(RAIZ_PROJETO.rglob('*.java'))
    if not arquivos:
        print("Nenhum arquivo .java encontrado.")
        sys.exit(0)

    print(f"{'APLICANDO ALTERAÇÕES' if aplicar else 'DRY-RUN (use --aplicar para alterar os arquivos)'}")
    print(f"Raiz: {RAIZ_PROJETO}")
    print(f"Arquivos .java encontrados: {len(arquivos)}\n")

    total_alterados = 0
    total_linhas_removidas = 0

    for arquivo in arquivos:
        try:
            antes, depois, alterado = processar_arquivo(arquivo, aplicar)
            if alterado:
                total_alterados += 1
                removidas = antes - depois
                total_linhas_removidas += removidas
                caminho_relativo = arquivo.relative_to(RAIZ_PROJETO.parent)
                status = "✓" if aplicar else "~"
                print(f"  [{status}] {caminho_relativo}  ({removidas:+d} linhas)")
        except Exception as e:
            print(f"  [!] {arquivo.name}: {e}")

    print(f"\nResumo:")
    print(f"  Arquivos modificados : {total_alterados} / {len(arquivos)}")
    print(f"  Linhas removidas     : {total_linhas_removidas}")
    if not aplicar:
        print("\n  Execute com --aplicar para salvar as alterações.")


if __name__ == '__main__':
    main()
