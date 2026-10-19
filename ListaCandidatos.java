public class ListaCandidatos {

    private static class No {
        Candidato candidato;
        No prox;

        No(Candidato candidato, No prox) {
            this.candidato = candidato;
            this.prox = prox;
        }
    }

    public class Iterador {
        private No atual;

        private Iterador() {
            atual = cabeca.prox;
        }

        public boolean temProximo() {
            return atual != fim;
        }

        public Candidato proximo() {
            if (atual == fim) {
                throw new IllegalStateException("Fim da lista");
            }
            Candidato c = atual.candidato;
            atual = atual.prox;
            return c;
        }
    }

    private final No cabeca;
    private final No fim;
    private No tail;
    private int tamanho;

    public ListaCandidatos() {
        fim = new No(null, null);
        cabeca = new No(null, fim);
        tail = cabeca;
        tamanho = 0;
    }

    public void insere(Candidato c) {
        No novo = new No(c, fim);
        tail.prox = novo;
        tail = novo;
        tamanho++;
    }

    public Candidato buscaPorId(int id) {
        for (No p = cabeca.prox; p != fim; p = p.prox) {
            if (Candidato.obtemIdentificador(p.candidato) == id) {
                return p.candidato;
            }
        }
        return null;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return tamanho == 0;
    }

    public Iterador iterador() {
        return new Iterador();
    }
}
