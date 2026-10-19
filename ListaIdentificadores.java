public class ListaIdentificadores {

    private static class No {
        int valor;
        No prox;

        No(int valor, No prox) {
            this.valor = valor;
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

        public int proximo() {
            if (atual == fim) {
                throw new IllegalStateException("Fim da lista");
            }
            int valor = atual.valor;
            atual = atual.prox;
            return valor;
        }
    }

    private final No cabeca;
    private final No fim;
    private No tail;
    private int tamanho;

    public ListaIdentificadores() {
        fim = new No(0, null);
        cabeca = new No(0, fim);
        tail = cabeca;
        tamanho = 0;
    }

    public void insere(int id) {
        No novo = new No(id, fim);
        tail.prox = novo;
        tail = novo;
        tamanho++;
    }

    public boolean insereSemRepetir(int id) {
        if (contem(id)) {
            return false;
        }
        insere(id);
        return true;
    }

    public boolean remove(int id) {
        No anterior = buscaAnterior(id);
        if (anterior == null) {
            return false;
        }
        No alvo = anterior.prox;
        anterior.prox = alvo.prox;
        if (alvo == tail) {
            tail = anterior;
        }
        tamanho--;
        return true;
    }

    public boolean contem(int id) {
        fim.valor = id;
        No p = cabeca.prox;
        while (p.valor != id) {
            p = p.prox;
        }
        return p != fim;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return tamanho == 0;
    }

    public void esvazia() {
        cabeca.prox = fim;
        tail = cabeca;
        tamanho = 0;
    }

    public ListaIdentificadores copia() {
        ListaIdentificadores nova = new ListaIdentificadores();
        for (No p = cabeca.prox; p != fim; p = p.prox) {
            nova.insere(p.valor);
        }
        return nova;
    }

    public Iterador iterador() {
        return new Iterador();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (No p = cabeca.prox; p != fim; p = p.prox) {
            sb.append(p.valor);
            if (p.prox != fim) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    private No buscaAnterior(int id) {
        No anterior = cabeca;
        while (anterior.prox != fim && anterior.prox.valor != id) {
            anterior = anterior.prox;
        }
        return anterior.prox == fim ? null : anterior;
    }
}
