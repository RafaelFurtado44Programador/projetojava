public class Candidato {

    public static final int ESPORTES = 1;
    public static final int ARTES = 2;
    public static final int MUSICA = 3;
    public static final int CINEMA = 4;
    public static final int TECNOLOGIA = 5;
    public static final int ANIMAIS = 6;
    public static final int GASTRONOMIA = 7;
    public static final int CIENCIAS = 8;

    public static final int NUM_AREAS = 8;
    public static final int GRAU_MAXIMO = 15;

    private static final String[] NOMES_AREAS = {
        "", "ESPORTES", "ARTES", "MUSICA", "CINEMA",
        "TECNOLOGIA", "ANIMAIS", "GASTRONOMIA", "CIENCIAS"
    };

    // Definição dos pesos das áreas de interesse.
    private static final int[] PESOS = {0, 1, 2, 1, 1, 3, 2, 2, 3};

    private static int grauDesejado = 6;
    private static int proximoId = 1;

    private final int id;
    private String nome;
    private String estado;
    private char sexo;
    private char sexoInteresse;
    private final ListaIdentificadores areas;
    private final ListaIdentificadores companheiros;

    private Candidato(int id) {
        this.id = id;
        this.areas = new ListaIdentificadores();
        this.companheiros = new ListaIdentificadores();
    }

    /** Cria um candidato automaticamente. */
    public static Candidato criaCandidato(String nome, String estado, char sexo, char sexoInteresse) {
        Candidato u = new Candidato(proximoId++);
        atualizaNome(u, nome);
        atualizaEstado(u, estado);
        atualizaSexo(u, sexo);
        atualizaSexoInteresse(u, sexoInteresse);
        return u;
    }

    public static void atualizaNome(Candidato u, String nome) {
        if (!nomeValido(nome)) {
            throw new IllegalArgumentException("Nome vazio");
        }
        u.nome = nome.trim();
    }

    public static void atualizaEstado(Candidato u, String sigla) {
        if (!estadoValido(sigla)) {
            throw new IllegalArgumentException("Sigla de estado invalida (use 2 letras): " + sigla);
        }
        u.estado = sigla.trim().toUpperCase();
    }

    public static void atualizaSexo(Candidato u, char sexo) {
        if (!sexoValido(sexo)) {
            throw new IllegalArgumentException("Sexo invalido (use F/M): " + sexo);
        }
        u.sexo = Character.toUpperCase(sexo);
    }

    public static void atualizaSexoInteresse(Candidato u, char sexoInteresse) {
        if (!sexoInteresseValido(sexoInteresse)) {
            throw new IllegalArgumentException("Sexo de interesse invalido (use F/M/I): " + sexoInteresse);
        }
        u.sexoInteresse = Character.toUpperCase(sexoInteresse);
    }


    public static boolean nomeValido(String nome) {
        return nome != null && !nome.trim().isEmpty();
    }

    public static boolean estadoValido(String sigla) {
        if (sigla == null || sigla.trim().length() != 2) {
            return false;
        }
        String s = sigla.trim();
        return Character.isLetter(s.charAt(0)) && Character.isLetter(s.charAt(1));
    }

    public static boolean sexoValido(char sexo) {
        sexo = Character.toUpperCase(sexo);
        return sexo == 'F' || sexo == 'M';
    }

    public static boolean sexoInteresseValido(char sexoInteresse) {
        sexoInteresse = Character.toUpperCase(sexoInteresse);
        return sexoInteresse == 'F' || sexoInteresse == 'M' || sexoInteresse == 'I';
    }

    /** Adiciona uma área de interesse e Não deixa repetir */
    public static boolean adicionaArea(Candidato u, int area) {
        if (!areaValida(area)) {
            return false;
        }
        return u.areas.insereSemRepetir(area);
    }

    public static boolean removeArea(Candidato u, int area) {
        return u.areas.remove(area);
    }

    /**Confere compatibilidade */
    public static boolean verificaPotencialCompanheiro(Candidato u, Candidato v) {
        if (obtemIdentificador(u) == obtemIdentificador(v)) {
            return false;
        }
        if (!reside(v, u.estado)) {
            return false;
        }
        if (!atendeSexo(u.sexoInteresse, v.sexo) || !atendeSexo(v.sexoInteresse, u.sexo)) {
            return false;
        }
        return grauInteresseComum(u, v) >= grauDesejado;
    }

    /** Insere o identificador de v na lista de potenciais companheiros de u. */
    public static void inserePotencialCompanheiro(Candidato u, Candidato v) {
        u.companheiros.insereSemRepetir(obtemIdentificador(v));
    }

    public static int numeroCompanheiros(Candidato u) {
        return u.companheiros.tamanho();
    }

    /** Retorna uma cópia da lista de ids. */
    public static ListaIdentificadores listaCompanheiros(Candidato u) {
        return u.companheiros.copia();
    }

    /** Verdadeiro se u tem interesse na área informada . */
    public static boolean temInteresse(Candidato u, int area) {
        return u.areas.contem(area);
    }

    public static boolean reside(Candidato u, String sigla) {
        return sigla != null && u.estado.equalsIgnoreCase(sigla.trim());
    }

    /** Verifica a lista de U */
    public static boolean temCompanheiro(Candidato u, int id) {
        return u.companheiros.contem(id);
    }

    public static void limpaCompanheiros(Candidato u) {
        u.companheiros.esvazia();
    }

    /** Calculo do interesse */
    public static int grauInteresseComum(Candidato u, Candidato v) {
        int grau = 0;
        ListaIdentificadores.Iterador it = u.areas.iterador();
        while (it.temProximo()) {
            int area = it.proximo();
            if (temInteresse(v, area)) {
                grau += pesoArea(area);
            }
        }
        return grau;
    }

    public static int obtemIdentificador(Candidato u) {
        return u.id;
    }

    public static String obtemNome(Candidato u) {
        return u.nome;
    }

    public static String obtemEstado(Candidato u) {
        return u.estado;
    }

    public static char obtemSexo(Candidato u) {
        return u.sexo;
    }

    public static char obtemSexoInteresse(Candidato u) {
        return u.sexoInteresse;
    }

    public static ListaIdentificadores obtemAreas(Candidato u) {
        return u.areas.copia();
    }

    public static void defineGrauDesejado(int grau) {
        if (grau < 1 || grau > GRAU_MAXIMO) {
            throw new IllegalArgumentException("Grau deve estar entre 1 e " + GRAU_MAXIMO);
        }
        grauDesejado = grau;
    }

    public static int obtemGrauDesejado() {
        return grauDesejado;
    }

    public static String nomeArea(int area) {
        return areaValida(area) ? NOMES_AREAS[area] : "?";
    }

    public static int pesoArea(int area) {
        return areaValida(area) ? PESOS[area] : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%4d  %-30s %s  %c -> %c  areas: ", id, nome, estado, sexo, sexoInteresse));
        ListaIdentificadores.Iterador it = areas.iterador();
        while (it.temProximo()) {
            sb.append(nomeArea(it.proximo()));
            if (it.temProximo()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /** confere se o sexo corresponde */
    private static boolean atendeSexo(char sexoInteresse, char sexo) {
        return sexoInteresse == 'I' || sexoInteresse == sexo;
    }

    private static boolean areaValida(int area) {
        return area >= 1 && area <= NUM_AREAS;
    }
}
