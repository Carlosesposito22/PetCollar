# language: pt
Funcionalidade: Emissão de Relatório Clínico Evolutivo e Sumário de Saúde

  Cenário: Gerar relatório com sinais vitais do atendimento
    Dado existe um atendimento em curso para o paciente
    E os sinais vitais foram aferidos com peso 5.2 kg e temperatura 38.5 graus
    Quando o servico consolidar os sinais vitais do atendimento
    Então os sinais vitais devem ser registrados no relatorio
    E o repositorio deve ter salvo o relatorio

  Cenário: Evolução comparativa com atendimento anterior
    Dado existe um relatorio com sinais vitais registrados de peso 5.2 kg
    E existe um historico de atendimento anterior com peso 4.8 kg
    Quando o servico gerar a evolucao comparativa
    Então a variacao de peso deve ser 0.4 kg
    E o resumo textual deve conter informacao de ganho de peso

  Cenário: Primeiro atendimento do paciente gera resumo especial
    Dado existe um relatorio sem historico anterior com peso 3.5 kg
    Quando o servico gerar a evolucao comparativa sem historico
    Então o resumo textual deve indicar primeiro atendimento registrado

  Cenário: Assinar relatório rotineiro completo o torna imutável
    Dado existe um relatorio rotineiro com diagnostico e resumo para o tutor preenchidos
    Quando o servico assinar digitalmente o relatorio
    Então o relatorio deve ter a flag imutavel verdadeira
    E o campo assinadoEm deve ser preenchido
    E o repositorio deve ter salvo o relatorio

  Cenário: Modificar relatório assinado lança exceção
    Dado existe um relatorio ja assinado digitalmente
    Quando o servico tentar modificar o diagnostico do relatorio
    Então deve ser lancada uma excecao de estado invalido

  Cenário: Assinar relatório rotineiro sem resumo para o tutor lança exceção
    Dado existe um relatorio rotineiro sem resumo para o tutor
    Quando o servico tentar assinar o relatorio incompleto
    Então deve ser lancada uma excecao de estado invalido

  Cenário: Relatório cirúrgico exige cuidados pós-operatórios antes da assinatura
    Dado existe um relatorio cirurgico com diagnostico e resumo mas sem cuidados pos-operatorios
    Quando o servico tentar assinar o relatorio incompleto
    Então deve ser lancada uma excecao de estado invalido

  Cenário: Relatório cirúrgico completo pode ser assinado
    Dado existe um relatorio cirurgico totalmente preenchido
    Quando o servico assinar digitalmente o relatorio
    Então o relatorio deve ter a flag imutavel verdadeira
    E o campo assinadoEm deve ser preenchido

  Cenário: Relatório preventivo exige apenas resumo para o tutor
    Dado existe um relatorio preventivo com apenas resumo para o tutor
    Quando o servico assinar digitalmente o relatorio
    Então o relatorio deve ter a flag imutavel verdadeira

  Cenário: Adicionar múltiplos anexos ao relatório
    Dado existe um relatorio rotineiro com diagnostico e resumo para o tutor preenchidos
    Quando o servico adicionar um anexo do tipo "FOTO_LESAO" com nome "lesao_pata.jpg"
    E o servico adicionar um anexo do tipo "LAUDO_PDF" com nome "laudo_hemograma.pdf"
    Então o relatorio deve conter 2 anexos

  Cenário: Limite de 4 anexos é respeitado
    Dado existe um relatorio rotineiro com diagnostico e resumo para o tutor preenchidos
    Quando o servico adicionar 4 anexos ao relatorio
    E o servico tentar adicionar um quinto anexo
    Então deve ser lancada uma excecao de estado invalido
