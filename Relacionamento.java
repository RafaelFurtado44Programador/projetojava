import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Relacionamento {

    private static ListaCandidatos candidatos;
    private static Scanner teclado;

    public static void main(String[] args) {
        String arquivo = args.length >= 1 ? args[0] : "candidatos.txt";

        try {
            candidatos = carregaCandidatos(arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao ler " + arquivo + ": " + e.getMessage());
            System.out.println("Gere o arquivo com: java GeradorCandidatos");
            return;
        } catch (IllegalArgumentException e) {
            System.out.println("Arquivo " + arquivo + " com formato invalido: " + e.getMessage());
            return;
        }

        System.out.println(candidatos.tamanho() + " candidatos carregados de " + arquivo + ".");
        calculaPares();

        teclado = new Scanner(System.in);
        int opcao;
        do {
            imprimeMenu();
            opcao = leInteiro("Opcao: ");
            System.out.println();
            switch (opcao) {
                case 1 -> tarefaAtualizaGrau();
                case 2 -> tarefaCompanheirosDeCandidato();
                case 3 -> tarefaKMaisCompanheiros();
                case 4 -> tarefaSemCompanheiros();
                case 5 -> tarefaPorArea();
                case 6 -> tarefaPorEstado();
                case 7 -> tarefaListaTodos();
                case 0 -> System.out.println("Saindo do sistema.");
                default -> System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }


    private static class LeitorLinhas {
        private final BufferedReader in;
        private int numeroLinha = 0;

        LeitorLinhas(BufferedReader in) {
            this.in = in;
        }


        private String le() throws IOException {
            String linha = in.readLine();
            if (linha == null) {
                return null;
            }
            numeroLinha++;
            if (numeroLinha == 1 && linha.startsWith("﻿")) {
                linha = linha.substring(1);
            }
            return linha.trim();
        }

        /** Próxima linha não vazia. */
        String proxima(String campo) throws IOException {
            String linha;
            while ((linha = le()) != null) {
                if (!linha.isEmpty()) {
                    return linha;
                }
            }
            throw erro("fim do arquivo ao ler " + campo);
        }

        String proximoNome() throws IOException {
            String linha = proxima("nome");
            if (!linha.startsWith("****")) {
                return linha; // arquivo sem separador antes deste bloco
            }
            linha = le();
            if (linha == null) {
                throw erro("fim do arquivo ao ler nome");
            }
            if (!Candidato.nomeValido(linha) || linha.startsWith("****")) {
                throw erro("nome vazio depois do separador ****");
            }
            return linha;
        }

        int proximoInteiro(String campo) throws IOException {
            String linha = proxima(campo);
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                throw erro(campo + " deveria ser um numero, mas e \"" + linha + "\"");
            }
        }

        char proximoChar(String campo) throws IOException {
            String linha = proxima(campo);
            if (linha.length() != 1) {
                throw erro(campo + " deveria ter uma letra, mas e \"" + linha + "\"");
            }
            return linha.charAt(0);
        }

        int linhaAtual() {
            return numeroLinha;
        }

        IllegalArgumentException erro(String mensagem) {
            return new IllegalArgumentException("linha " + numeroLinha + ": " + mensagem);
        }
    }

    private static ListaCandidatos carregaCandidatos(String arquivo) throws IOException {
        ListaCandidatos lista = new ListaCandidatos();
        try (BufferedReader in = new BufferedReader(new FileReader(arquivo, StandardCharsets.UTF_8))) {
            LeitorLinhas leitor = new LeitorLinhas(in);
            int n = leitor.proximoInteiro("numero de candidatos");
            if (n < 0) {
                throw leitor.erro("numero de candidatos negativo");
            }
            for (int i = 0; i < n; i++) {
                lista.insere(leCandidato(leitor));
            }
        }
        return lista;
    }

    private static Candidato leCandidato(LeitorLinhas leitor) throws IOException {
        String nome = leitor.proximoNome();

        String estado = leitor.proxima("sigla do estado");
        if (!Candidato.estadoValido(estado)) {
            throw leitor.erro("sigla do estado invalida (use 2 letras): \"" + estado + "\"");
        }

        char sexo = leitor.proximoChar("sexo");
        if (!Candidato.sexoValido(sexo)) {
            throw leitor.erro("sexo invalido (use F/M): \"" + sexo + "\"");
        }

        char sexoInteresse = leitor.proximoChar("sexo de interesse");
        if (!Candidato.sexoInteresseValido(sexoInteresse)) {
            throw leitor.erro("sexo de interesse invalido (use F/M/I): \"" + sexoInteresse + "\"");
        }

        Candidato c = Candidato.criaCandidato(nome, estado, sexo, sexoInteresse);

        int numAreas = leitor.proximoInteiro("numero de areas");
        if (numAreas < 0 || numAreas > Candidato.NUM_AREAS) {
            throw leitor.erro("numero de areas deve estar entre 0 e " + Candidato.NUM_AREAS
                    + ", mas e " + numAreas);
        }
        for (int i = 0; i < numAreas; i++) {
            int area = leitor.proximoInteiro("area de interesse");
            if (!Candidato.adicionaArea(c, area)) {
                System.out.println("Aviso (linha " + leitor.linhaAtual() + "): area " + area
                        + " de " + nome + " e invalida ou repetida e foi ignorada.");
            }
        }
        return c;
    }


    private static void calculaPares() {
        ListaCandidatos.Iterador itU = candidatos.iterador();
        while (itU.temProximo()) {
            Candidato u = itU.proximo();
            Candidato.limpaCompanheiros(u);
            ListaCandidatos.Iterador itV = candidatos.iterador();
            while (itV.temProximo()) {
                Candidato v = itV.proximo();
                if (Candidato.verificaPotencialCompanheiro(u, v)) {
                    Candidato.inserePotencialCompanheiro(u, v);
                }
            }
        }
        System.out.println("Pares calculados para o grau de interesse " + Candidato.obtemGrauDesejado() + ".");
    }


    private static void imprimeMenu() {
        System.out.println();
        System.out.println("===== AGENCIA DE RELACIONAMENTO (grau atual: " + Candidato.obtemGrauDesejado() + ") =====");
        System.out.println("1 - Inserir/atualizar o grau de interesse desejado");
        System.out.println("2 - Potenciais companheiros de um candidato");
        System.out.println("3 - K candidatos com mais potenciais companheiros");
        System.out.println("4 - Candidatos sem nenhum companheiro");
        System.out.println("5 - Candidatos com interesse em uma area");
        System.out.println("6 - Candidatos que residem em um estado");
        System.out.println("7 - Listar todos os candidatos");
        System.out.println("0 - Sair");
    }

    /** Tarefa 1 */
    private static void tarefaAtualizaGrau() {
        int grau = leInteiro("Novo grau de interesse (1 a " + Candidato.GRAU_MAXIMO + "): ");
        try {
            Candidato.defineGrauDesejado(grau);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + ". Grau mantido em " + Candidato.obtemGrauDesejado() + ".");
            return;
        }
        calculaPares();
    }

    private static void tarefaCompanheirosDeCandidato() {
        int id = leInteiro("Id do candidato (1 a " + candidatos.tamanho() + "): ");
        Candidato u = candidatos.buscaPorId(id);
        if (u == null) {
            System.out.println("Candidato " + id + " nao encontrado.");
            return;
        }
        System.out.println("Candidato: " + u);
        System.out.println("Numero de potenciais companheiros: " + Candidato.numeroCompanheiros(u));

        ListaIdentificadores.Iterador it = Candidato.listaCompanheiros(u).iterador();
        while (it.temProximo()) {
            Candidato v = candidatos.buscaPorId(it.proximo());
            System.out.printf("%4d  %-30s grau em comum: %d%n", Candidato.obtemIdentificador(v),
                    Candidato.obtemNome(v), Candidato.grauInteresseComum(u, v));
        }
    }

    private static void tarefaKMaisCompanheiros() {
        int k = leInteiro("Valor de K: ");
        if (k < 1) {
            System.out.println("K deve ser pelo menos 1.");
            return;
        }
        if (k > candidatos.tamanho()) {
            k = candidatos.tamanho();
        }

        ListaIdentificadores escolhidos = new ListaIdentificadores();
        System.out.println("Os " + k + " candidatos com mais potenciais companheiros:");
        for (int i = 0; i < k; i++) {
            Candidato melhor = null;
            ListaCandidatos.Iterador it = candidatos.iterador();
            while (it.temProximo()) {
                Candidato c = it.proximo();
                if (escolhidos.contem(Candidato.obtemIdentificador(c))) {
                    continue;
                }
                if (melhor == null || Candidato.numeroCompanheiros(c) > Candidato.numeroCompanheiros(melhor)) {
                    melhor = c;
                }
            }
            escolhidos.insere(Candidato.obtemIdentificador(melhor));
            System.out.printf("%2d. %4d  %-30s %d companheiro(s)%n", i + 1, Candidato.obtemIdentificador(melhor),
                    Candidato.obtemNome(melhor), Candidato.numeroCompanheiros(melhor));
        }
    }

    private static void tarefaSemCompanheiros() {
        System.out.println("Candidatos sem nenhum potencial companheiro:");
        int total = 0;
        ListaCandidatos.Iterador it = candidatos.iterador();
        while (it.temProximo()) {
            Candidato c = it.proximo();
            if (Candidato.numeroCompanheiros(c) == 0) {
                imprimeIdNome(c);
                total++;
            }
        }
        System.out.println("Total: " + total);
    }

    private static void tarefaPorArea() {
        for (int a = 1; a <= Candidato.NUM_AREAS; a++) {
            System.out.println(a + ": " + Candidato.nomeArea(a));
        }
        int area = leInteiro("Codigo da area: ");
        if (area < 1 || area > Candidato.NUM_AREAS) {
            System.out.println("Area invalida.");
            return;
        }
        System.out.println("Candidatos com interesse em " + Candidato.nomeArea(area) + ":");
        int total = 0;
        ListaCandidatos.Iterador it = candidatos.iterador();
        while (it.temProximo()) {
            Candidato c = it.proximo();
            if (Candidato.temInteresse(c, area)) {
                imprimeIdNome(c);
                total++;
            }
        }
        System.out.println("Total: " + total);
    }

    private static void tarefaPorEstado() {
        String sigla = leLinha("Sigla do estado: ").toUpperCase();
        System.out.println("Candidatos que residem em " + sigla + ":");
        int total = 0;
        ListaCandidatos.Iterador it = candidatos.iterador();
        while (it.temProximo()) {
            Candidato c = it.proximo();
            if (Candidato.reside(c, sigla)) {
                imprimeIdNome(c);
                total++;
            }
        }
        System.out.println("Total: " + total);
    }

    private static void tarefaListaTodos() {
        System.out.println("  id  nome                           UF  sexo->interesse");
        ListaCandidatos.Iterador it = candidatos.iterador();
        while (it.temProximo()) {
            Candidato c = it.proximo();
            System.out.println(c + "  [" + Candidato.numeroCompanheiros(c) + " companheiro(s)]");
        }
    }

    private static void imprimeIdNome(Candidato c) {
        System.out.printf("%4d  %s%n", Candidato.obtemIdentificador(c), Candidato.obtemNome(c));
    }


    private static String leLinha(String mensagem) {
        System.out.print(mensagem);
        if (!teclado.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return teclado.nextLine().trim();
    }

    private static int leInteiro(String mensagem) {
        while (true) {
            String linha = leLinha(mensagem);
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro.");
            }
        }
    }
}
