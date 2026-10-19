# Agência de Relacionamento — Especificação de Requisitos e Arquitetura

Trabalho 1 — Lab. AED I — Eng. da Computação — CEFET-MG Campus Timóteo
Autor: Rafael Furtado

---

## 1. Objetivo

Ferramenta que lê os cadastros dos candidatos de uma agência de relacionamento
(arquivo `candidatos.txt`) e identifica automaticamente os **potenciais pares**,
com base em estado de residência, opção sexual e grau de interesse em comum.

## 2. Especificação de Requisitos

### 2.1 Requisitos funcionais

| Código | Requisito |
|---|---|
| RF01 | Ler os candidatos do arquivo `candidatos.txt` no formato definido no enunciado (N, `****`, blocos de candidato). |
| RF02 | Atribuir a cada candidato um identificador interno único (1, 2, 3, ... na ordem do arquivo). |
| RF03 | **Tarefa 1** — Inserir/atualizar o grau de interesse desejado (valor inicial: 6). Ao mudar o grau, os pares são recalculados. |
| RF04 | **Tarefa 2** — Imprimir o número e a lista dos nomes dos potenciais companheiros de um candidato. |
| RF05 | **Tarefa 3** — Imprimir id/nome dos K candidatos com maior número de potenciais companheiros. |
| RF06 | **Tarefa 4** — Imprimir id/nome dos candidatos sem nenhum companheiro encontrado. |
| RF07 | **Tarefa 5** — Imprimir id/nome dos candidatos com interesse em uma determinada área. |
| RF08 | **Tarefa 6** — Imprimir id/nome dos candidatos que residem em um determinado estado. |
| RF09 | Menu textual interativo para escolher uma das 6 tarefas ou sair. |
| RF10 | Programa auxiliar que gera candidatos aleatórios e grava `candidatos.txt` (nomes lidos de um dicionário `nomes.txt`). |

### 2.2 Regras de negócio

**Potenciais companheiros** — `u` e `v` formam um potencial par se:

1. são candidatos diferentes;
2. residem no mesmo estado (sigla, sem diferenciar maiúsculas/minúsculas);
3. atendem à opção sexual um do outro: o sexo de `v` atende o sexo de interesse de `u`
   **e** o sexo de `u` atende o sexo de interesse de `v`. Sexo de interesse `I`
   (indiferente) aceita F e M. A verificação nos dois sentidos torna a relação simétrica;
4. o grau de interesse em comum é **maior ou igual** ao grau desejado definido pelo usuário.

**Grau de interesse em comum** — soma dos pesos das áreas que os dois têm em comum:

| Código | Área | Peso |
|---|---|---|
| 1 | ESPORTES | 1 |
| 2 | ARTES | 2 |
| 3 | MÚSICA | 1 |
| 4 | CINEMA | 1 |
| 5 | TECNOLOGIA | 3 |
| 6 | ANIMAIS | 2 |
| 7 | GASTRONOMIA | 2 |
| 8 | CIÊNCIAS | 3 |

O grau máximo possível é 15 (todas as áreas em comum). O grau desejado aceito vai de 1 a 15.

**Desempate na Tarefa 3** — candidatos com o mesmo número de companheiros aparecem em
ordem crescente de id.

### 2.3 Requisitos não funcionais / restrições

- Linguagem Java (sem bibliotecas externas).
- **Proibido** usar estruturas prontas do Java (`ArrayList`, `LinkedList`, `HashMap`,
  `Iterator` da biblioteca, etc.). Todas as estruturas são implementadas no projeto.
- As TADs usam **lista simplesmente encadeada com tail e sentinelas**.
- Os campos do candidato são privados; o acesso é feito pelas operações da TAD
  (ex.: `inserePotencialCompanheiro` obtém o id de `v` por `obtemIdentificador(v)`).
- Interface textual.

## 3. Arquitetura do Programa

### 3.1 Módulos (arquivos-fonte)

```
ListaIdentificadores.java   TAD lista de inteiros (ids de companheiros e códigos de áreas)
ListaCandidatos.java        lista de candidatos cadastrados (mesma técnica de lista)
Candidato.java              TAD Candidato (dados, regras de compatibilidade, pesos)
Relacionamento.java         programa principal: leitura do arquivo, cálculo dos pares, menu e tarefas
GeradorCandidatos.java      programa auxiliar: gera candidatos.txt aleatoriamente
nomes.txt                   dicionário de nomes usado pelo gerador
```

Dependências:

```
Relacionamento ──> ListaCandidatos ──> Candidato ──> ListaIdentificadores
GeradorCandidatos ──> Candidato (usa apenas a constante NUM_AREAS)
```

### 3.2 Estrutura das listas encadeadas

Todas as listas seguem o mesmo desenho:

```
 cabeca (sentinela)      tail                    fim (sentinela)
   [  ] ──> [ e1 ] ──> [ e2 ] ──> ... ──> [ en ] ──> [  ]
```

- **Sentinela cabeça**: nó sem dado antes do primeiro elemento. Inserção e remoção
  nunca precisam tratar "lista vazia" ou "primeiro elemento" como caso especial.
- **Sentinela fim**: nó sem dado depois do último elemento (substitui o `null`).
  Na busca de `ListaIdentificadores`, o valor procurado é colocado no sentinela fim,
  então o laço de busca tem uma única comparação por passo.
- **tail**: aponta para o último elemento real (ou para a cabeça, se vazia),
  tornando a inserção no fim O(1).
- **Iterador próprio** (`Iterador`, classe interna): permite percursos
  independentes e aninhados na mesma lista (necessário no cálculo dos pares,
  que compara todos com todos).

### 3.3 TAD ListaIdentificadores

| Operação | Descrição | Custo |
|---|---|---|
| `ListaIdentificadores()` | cria lista vazia | O(1) |
| `insere(int id)` | insere no fim | O(1) |
| `insereSemRepetir(int id)` | insere só se ainda não existir | O(n) |
| `remove(int id)` | remove a primeira ocorrência | O(n) |
| `contem(int id)` | busca com sentinela | O(n) |
| `tamanho()` / `vazia()` | tamanho / teste de vazia | O(1) |
| `esvazia()` | remove todos | O(1) |
| `copia()` | nova lista com os mesmos valores | O(n) |
| `iterador()` | percurso: `temProximo()` / `proximo()` | O(1) por passo |
| `toString()` | `[1, 4, 7]` | O(n) |

### 3.4 TAD Candidato

Dados (privados): `id`, `nome`, `estado`, `sexo` (F/M), `sexoInteresse` (F/M/I),
`areas` (ListaIdentificadores com códigos 1..8), `companheiros`
(ListaIdentificadores com ids, inicialmente vazia).
Dado da classe: `grauDesejado` (estático, padrão 6).

Operações exigidas pelo enunciado (estáticas, recebem os candidatos por parâmetro):

| Operação | Descrição |
|---|---|
| `verificaPotencialCompanheiro(u, v)` | aplica as regras da seção 2.2 |
| `inserePotencialCompanheiro(u, v)` | insere `obtemIdentificador(v)` na lista de `u` (sem repetir) |
| `numeroCompanheiros(u)` | quantidade de potenciais companheiros |
| `listaCompanheiros(u)` | **cópia** da lista de ids (protege o estado interno) |
| `temInteresse(u, area)` | `u` tem a área de interesse informada (usada na Tarefa 5) |
| `reside(u, sigla)` | `u` reside no estado informado |

Observação: o enunciado descreve `temInteresse(u, id)` de forma ambígua ("tem o id
(tem outro candidato) na sua lista de interesses"). Como a Tarefa 5 precisa saber
se o candidato tem interesse em uma área, `temInteresse` verifica a área, e a
verificação de outro candidato na lista de companheiros é feita por
`temCompanheiro(u, id)`.

Operações complementares:

| Operação | Descrição |
|---|---|
| `criaCandidato(nome, estado, sexo, sexoInteresse)` | cria candidato com id automático |
| `atualizaNome / atualizaEstado / atualizaSexo / atualizaSexoInteresse` | atualizam campos (com validação) |
| `adicionaArea(u, area)` / `removeArea(u, area)` | gerenciam as áreas de interesse |
| `temCompanheiro(u, id)` | `id` está na lista de companheiros de `u` |
| `limpaCompanheiros(u)` | esvazia a lista (antes de recalcular) |
| `grauInteresseComum(u, v)` | soma dos pesos das áreas em comum |
| `obtemIdentificador / obtemNome / obtemEstado / obtemSexo / obtemSexoInteresse` | consultas |
| `defineGrauDesejado(g)` / `obtemGrauDesejado()` | grau usado nas verificações |
| `nomeArea(codigo)` / `pesoArea(codigo)` | dados da tabela de áreas |
| `nomeValido / estadoValido / sexoValido / sexoInteresseValido` | validam um campo antes de criar o candidato (usadas na leitura do arquivo) |

Funções auxiliares privadas: `atendeSexo(interesse, sexo)`, `areaValida(codigo)`.

### 3.5 ListaCandidatos

Mesma estrutura de lista (cabeça, fim, tail), guardando referências a `Candidato`.
Operações: `insere`, `buscaPorId`, `tamanho`, `vazia`, `iterador`.

### 3.6 Programa principal (Relacionamento)

1. Lê `candidatos.txt` (ou o arquivo passado como argumento) → `ListaCandidatos`.
2. Calcula os pares para o grau padrão: para cada `u`, limpa sua lista e, para
   cada `v`, chama `verificaPotencialCompanheiro(u, v)`; se verdadeiro,
   `inserePotencialCompanheiro(u, v)`.

   Custo, com n = nº de candidatos, a = nº de áreas por candidato (≤ 8) e
   c_u = nº de companheiros de u:
   - `verificaPotencialCompanheiro` é chamada n² vezes; cada chamada calcula o
     grau em O(a²) (para cada área de u, uma busca na lista de áreas de v) → O(n² · a²);
   - `inserePotencialCompanheiro` usa `insereSemRepetir`, que percorre a lista de
     companheiros de u antes de inserir; para u isso soma 1 + 2 + ... + c_u = O(c_u²).
   - Total: O(n² · a² + Σ c_u²). Como a ≤ 8 é constante, o primeiro termo é O(n²);
     no pior caso (todos compatíveis entre si, c_u = n − 1) o segundo termo é O(n³).
     Na prática c_u é pequeno (com os dados do gerador, no máximo algumas dezenas),
     e o custo fica próximo de O(n²).
   - A verificação de repetição foi mantida de propósito: `inserePotencialCompanheiro`
     é uma operação pública do TAD e não deve permitir ids duplicados se for chamada
     fora do cálculo dos pares.
3. Laço do menu:

```
1 - Inserir/atualizar grau de interesse desejado   (recalcula os pares)
2 - Potenciais companheiros de um candidato
3 - K candidatos com mais potenciais companheiros
4 - Candidatos sem nenhum companheiro
5 - Candidatos com interesse em uma área
6 - Candidatos que residem em um estado
7 - Listar todos os candidatos (auxiliar)
0 - Sair
```

A Tarefa 3 faz K passagens pela lista; em cada uma escolhe o candidato ainda não
escolhido com maior número de companheiros (os escolhidos ficam numa
`ListaIdentificadores`). Custo O(K · n · K), sem precisar de vetor auxiliar.

### 3.7 Leitura do arquivo

Formato (o leitor ignora linhas em branco e linhas `****` antes de cada candidato):

```
N
****
nome
sigla_estado
sexo
sexo_interesse
numero_areas
area_1
...
****
...
```

Regras da leitura:

- Cada campo é validado logo depois de lido, e o erro indica a linha exata do
  campo inválido (número inválido, sigla que não tem 2 letras, sexo diferente de F/M,
  sexo de interesse diferente de F/M/I, número de áreas fora de 0..8, fim de
  arquivo antes do esperado). Erros interrompem a leitura.
- Depois de um separador `****`, a linha seguinte deve ser o nome; nome vazio é erro
  (sem isso, os campos seguintes seriam lidos deslocados).
- Linhas em branco entre os demais campos são ignoradas.
- Áreas fora de 1..8 ou repetidas são ignoradas com aviso.
- Arquivos salvos como "UTF-8 com BOM" (Bloco de Notas, PowerShell) são aceitos:
  o caractere BOM do início é removido. O mesmo vale para o `nomes.txt` do gerador.

### 3.8 Programa auxiliar (GeradorCandidatos)

Inspirado em um gerador de perfis aleatórios: vetores de nomes/sobrenomes/estados
e sorteio com `java.util.Random`.

- Nomes lidos de `nomes.txt` (linhas `F;Nome` ou `M;Nome`); se o arquivo não
  existir, usa uma lista interna.
- Nome completo = nome + sobrenome sorteados; o nome é coerente com o sexo sorteado.
- Estado sorteado entre as 27 siglas das unidades federativas do Brasil.
- Sexo de interesse: 70% oposto, 15% mesmo sexo, 15% indiferente (I).
- 1 a 6 áreas distintas (embaralhamento de Fisher-Yates sobre 1..8).
- Uso: `java GeradorCandidatos [quantidade] [arquivo] [semente]`.
- Os nomes do dicionário são lidos em duas passagens (contagem e preenchimento)
  para dimensionar os vetores sem usar estruturas prontas.

### 3.9 Decisões de interface

As mensagens impressas não têm acentos: em terminais do Windows que não usam
UTF-8 os caracteres acentuados apareciam corrompidos (ex.: `AG�NCIA`).

## 4. Testes realizados

| Teste | Resultado |
|---|---|
| Arquivo montado à mão com 6 candidatos e pares conhecidos (estado diferente, sexo incompatível, interesse `I`, área inválida 9, área repetida, linha em branco, letras minúsculas) | listas de companheiros iguais às calculadas à mão; avisos de área inválida/repetida exibidos |
| Mudança de grau 6 → 7 no mesmo arquivo | todos os pares desaparecem, como esperado (grau máximo entre eles era 6) |
| 500 candidatos aleatórios, graus 1 a 15 | relação simétrica (v na lista de u ⇔ u na lista de v), mesmo estado e grau ≥ desejado em todos os pares |
| Remoção do último elemento da lista seguida de inserção (atualização do tail) | lista correta (`[2, 4]`, depois `[5]`) |
| Arquivo truncado / arquivo inexistente / opção e grau inválidos / texto no lugar de número | mensagens de erro, sem travar o programa |
| K maior que o número de candidatos | K limitado ao total de candidatos |
