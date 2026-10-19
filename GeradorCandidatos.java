import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class GeradorCandidatos {

    private static final String ARQUIVO_DICIONARIO = "nomes.txt";

    private static final String[] NOMES_FEMININOS = {"Ana", "Beatriz", "Camila", "Diana", "Fernanda", "Helena"};
    private static final String[] NOMES_MASCULINOS = {"Bruno", "Carlos", "Eduardo", "Gabriel", "Lucas", "Pedro"};
    private static final String[] SOBRENOMES = {
        "Silva", "Souza", "Santos", "Oliveira", "Costa", "Pereira", "Lima",
        "Almeida", "Ferreira", "Rodrigues", "Gomes", "Martins", "Rocha", "Ribeiro"
    };
    private static final String[] ESTADOS = {
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
        "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
        "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    };

    private static final int MAX_AREAS_POR_CANDIDATO = 6;

    private static Random random;
    private static String[] nomesFemininos = NOMES_FEMININOS;
    private static String[] nomesMasculinos = NOMES_MASCULINOS;

    public static void main(String[] args) {
        int quantidade = 1000;
        String arquivoSaida = "candidatos.txt";
        random = new Random();

        try {
            if (args.length >= 1) {
                quantidade = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                arquivoSaida = args[1];
            }
            if (args.length >= 3) {
                random = new Random(Long.parseLong(args[2]));
            }
        } catch (NumberFormatException e) {
            System.out.println("Uso: java GeradorCandidatos [quantidade] [arquivoSaida] [semente]");
            return;
        }
        if (quantidade < 1) {
            System.out.println("A quantidade deve ser pelo menos 1.");
            return;
        }

        carregaDicionario();

        try (PrintWriter out = new PrintWriter(new FileWriter(arquivoSaida, StandardCharsets.UTF_8))) {
            out.println(quantidade);
            for (int i = 0; i < quantidade; i++) {
                out.println("****");
                escreveCandidatoAleatorio(out);
            }
        } catch (IOException e) {
            System.out.println("Erro ao gravar " + arquivoSaida + ": " + e.getMessage());
            return;
        }

        System.out.println(quantidade + " candidatos gerados em " + arquivoSaida);
    }

    private static void escreveCandidatoAleatorio(PrintWriter out) {
        char sexo = random.nextBoolean() ? 'F' : 'M';
        String[] nomes = sexo == 'F' ? nomesFemininos : nomesMasculinos;
        String nome = sorteia(nomes) + " " + sorteia(SOBRENOMES);

        out.println(nome);
        out.println(sorteia(ESTADOS));
        out.println(sexo);
        out.println(sorteiaSexoInteresse(sexo));

        int numAreas = random.nextInt(MAX_AREAS_POR_CANDIDATO) + 1; // 1..6
        int[] areas = sorteiaAreasDistintas(numAreas);
        out.println(numAreas);
        for (int area : areas) {
            out.println(area);
        }
    }

    private static String sorteia(String[] opcoes) {
        return opcoes[random.nextInt(opcoes.length)];
    }

    private static char sorteiaSexoInteresse(char sexo) {
        int r = random.nextInt(100);
        if (r < 70) {
            return sexo == 'F' ? 'M' : 'F';
        }
        if (r < 85) {
            return sexo;
        }
        return 'I';
    }

    private static int[] sorteiaAreasDistintas(int n) {
        int[] codigos = new int[Candidato.NUM_AREAS];
        for (int i = 0; i < codigos.length; i++) {
            codigos[i] = i + 1;
        }
        for (int i = codigos.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = codigos[i];
            codigos[i] = codigos[j];
            codigos[j] = tmp;
        }
        int[] escolhidas = new int[n];
        for (int i = 0; i < n; i++) {
            escolhidas[i] = codigos[i];
        }
        return escolhidas;
    }

    private static void carregaDicionario() {
        File arquivo = new File(ARQUIVO_DICIONARIO);
        if (!arquivo.exists()) {
            System.out.println("Dicionario " + ARQUIVO_DICIONARIO + " nao encontrado; usando nomes internos.");
            return;
        }
        try {
            int[] contagem = new int[2]; // [0] = F, [1] = M
            percorreDicionario(arquivo, contagem, null, null);
            if (contagem[0] == 0 || contagem[1] == 0) {
                System.out.println("Dicionario sem nomes F e M; usando nomes internos.");
                return;
            }
            String[] femininos = new String[contagem[0]];
            String[] masculinos = new String[contagem[1]];
            percorreDicionario(arquivo, new int[2], femininos, masculinos);
            nomesFemininos = femininos;
            nomesMasculinos = masculinos;
        } catch (IOException e) {
            System.out.println("Erro ao ler " + ARQUIVO_DICIONARIO + "; usando nomes internos.");
        }
    }

    private static void percorreDicionario(File arquivo, int[] contagem,
                                           String[] femininos, String[] masculinos) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(arquivo, StandardCharsets.UTF_8))) {
            String linha;
            boolean primeiraLinha = true;
            while ((linha = in.readLine()) != null) {
                // Remove o BOM (U+FEFF) de arquivos salvos como "UTF-8 com BOM"
                if (primeiraLinha && linha.startsWith("﻿")) {
                    linha = linha.substring(1);
                }
                primeiraLinha = false;
                int sep = linha.indexOf(';');
                if (sep < 0) {
                    continue;
                }
                String sexo = linha.substring(0, sep).trim().toUpperCase();
                String nome = linha.substring(sep + 1).trim();
                if (nome.isEmpty()) {
                    continue;
                }
                if (sexo.equals("F")) {
                    if (femininos != null) {
                        femininos[contagem[0]] = nome;
                    }
                    contagem[0]++;
                } else if (sexo.equals("M")) {
                    if (masculinos != null) {
                        masculinos[contagem[1]] = nome;
                    }
                    contagem[1]++;
                }
            }
        }
    }
}
