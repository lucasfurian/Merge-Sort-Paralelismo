import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

class Dados {

    static int[] A = {1, 4, 7, 10};
    static int[] B = {2, 3, 6, 11, 12};

    static int[] C = new int[A.length + B.length];

    static int posicaoC = 0;
    static boolean vezA = true;

    public static void main(String[] args) {

        Thread threadA = new Thread(() -> {

            for (int i = 0; i < A.length; i++) {

                synchronized (C) {

                    while (!vezA) {
                        try {
                            C.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    C[posicaoC] = A[i];
                    posicaoC++;

                    vezA = false;

                    C.notifyAll();
                }
            }
        });

        Thread threadB = new Thread(() -> {

            for (int i = 0; i < B.length; i++) {

                synchronized (C) {

                    while (vezA && posicaoC < A.length * 2) {
                        try {
                            C.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    C[posicaoC] = B[i];
                    posicaoC++;

                    vezA = true;

                    C.notifyAll();
                }
            }
        });

        threadA.start();
        threadB.start();

        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
        }

        System.out.println(Arrays.toString(C));
    }
}

class MaiorVetorAproximado {
    public static void main(String[] args) {
        System.out.println("Estimando o maior tamanho possível de vetor em Java...");
        long inicio = System.currentTimeMillis();

        int tamanho = 1_000_000;
        int ultimoBemSucedido = 0;

        while (true) {
            try {
                byte[] vetor = new byte[tamanho];
                ultimoBemSucedido = tamanho;
                vetor = null;
                System.gc();

                if (tamanho > Integer.MAX_VALUE / 3 * 2) break;

                tamanho /= 2;
                tamanho *= 3;

                System.out.printf("Alocado com sucesso: %,d elementos%n", ultimoBemSucedido);
            } catch (OutOfMemoryError e) {
                System.out.printf("Falhou em %,d elementos%n", tamanho);
                break;
            }
        }

        long fim = System.currentTimeMillis();
        System.out.println("\nMaior vetor que coube (aproximadamente): "
                + String.format("%,d", ultimoBemSucedido));
        System.out.printf("Memória estimada: %.2f MB%n",
                ultimoBemSucedido * 1.0 / (1024 * 1024));
        System.out.printf("Tempo total: %.2f segundos%n", (fim - inicio) / 1000.0);
    }
}

class Ordenadora extends Thread {

    byte[] parte;
    int numero;
    boolean falhou = false;

    Ordenadora(byte[] parte, int numero) {
        this.parte = parte;
        this.numero = numero;
    }

    public void run() {
        try {
            System.out.println("Thread ordenadora " + numero + " iniciou com " + parte.length + " elementos");
            parte = MergeSort.mergeSort(parte);
            System.out.println("Thread ordenadora " + numero + " terminou");
        } catch (Throwable e) {
            falhou = true;
            System.out.println("Thread ordenadora " + numero + " falhou: " + e);
        }
    }
}

class Juntadora extends Thread {

    byte[] esquerda;
    byte[] direita;
    byte[] resultado;
    int rodada;
    int numero;
    boolean falhou = false;

    Juntadora(byte[] esquerda, byte[] direita, int rodada, int numero) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.rodada = rodada;
        this.numero = numero;
    }

    public void run() {
        try {
            System.out.println("Thread juntadora " + numero + " da rodada " + rodada + " iniciou");
            resultado = MergeSort.merge(esquerda, direita);
            System.out.println("Thread juntadora " + numero + " da rodada " + rodada + " terminou");
        } catch (Throwable e) {
            falhou = true;
            System.out.println("Thread juntadora " + numero + " da rodada " + rodada + " falhou: " + e);
        }
    }
}

public class MergeSort {

    static Scanner teclado = new Scanner(System.in);

    public static byte[] mergeSort(byte[] vetor) {

        if (vetor.length <= 1) {
            return vetor;
        }

        int meio = vetor.length / 2;
        byte[] esquerda = Arrays.copyOfRange(vetor, 0, meio);
        byte[] direita = Arrays.copyOfRange(vetor, meio, vetor.length);

        esquerda = mergeSort(esquerda);
        direita = mergeSort(direita);

        return merge(esquerda, direita);
    }

    public static byte[] merge(byte[] esquerda, byte[] direita) {

        byte[] resultado = new byte[esquerda.length + direita.length];

        int posicao_esquerda = 0;
        int posicao_direita = 0;
        int proxima_posicao = 0;

        while (posicao_esquerda < esquerda.length && posicao_direita < direita.length) {

            if (esquerda[posicao_esquerda] <= direita[posicao_direita]) {
                resultado[proxima_posicao] = esquerda[posicao_esquerda];
                posicao_esquerda++;
            } else {
                resultado[proxima_posicao] = direita[posicao_direita];
                posicao_direita++;
            }

            proxima_posicao++;
        }

        while (posicao_esquerda < esquerda.length) {
            resultado[proxima_posicao] = esquerda[posicao_esquerda];
            posicao_esquerda++;
            proxima_posicao++;
        }

        while (posicao_direita < direita.length) {
            resultado[proxima_posicao] = direita[posicao_direita];
            posicao_direita++;
            proxima_posicao++;
        }

        return resultado;
    }

    public static byte[] mergeSortParalelo(byte[] vetor) throws InterruptedException {

        int processadores = Runtime.getRuntime().availableProcessors();
        int quantidade = processadores - 1;

        if (quantidade < 1) {
            quantidade = 1;
        }
        if (quantidade > vetor.length) {
            quantidade = vetor.length;
        }

        System.out.println("Processadores: " + processadores + " | Threads ordenadoras: " + quantidade);

        Ordenadora[] ordenadoras = new Ordenadora[quantidade];
        int tamanho = vetor.length / quantidade;
        int resto = vetor.length % quantidade;
        int inicio = 0;

        for (int i = 0; i < quantidade; i++) {
            int fim = inicio + tamanho;
            if (i < resto) {
                fim++;
            }
            ordenadoras[i] = new Ordenadora(Arrays.copyOfRange(vetor, inicio, fim), i);
            ordenadoras[i].start();
            inicio = fim;
        }

        byte[][] partes = new byte[quantidade][];

        for (int i = 0; i < quantidade; i++) {
            ordenadoras[i].join();
            if (ordenadoras[i].falhou) {
                throw new RuntimeException("Uma thread ordenadora falhou");
            }
            partes[i] = ordenadoras[i].parte;
        }

        int rodada = 1;

        while (partes.length > 1) {

            int quantidadeJuntadoras = partes.length / 2;
            Juntadora[] juntadoras = new Juntadora[quantidadeJuntadoras];

            System.out.println("Rodada " + rodada + ": " + quantidadeJuntadoras + " juntadora(s)");

            for (int j = 0; j < quantidadeJuntadoras; j++) {
                juntadoras[j] = new Juntadora(partes[2 * j], partes[2 * j + 1], rodada, j);
                juntadoras[j].start();
            }

            byte[][] novas = new byte[(partes.length + 1) / 2][];

            for (int j = 0; j < quantidadeJuntadoras; j++) {
                juntadoras[j].join();
                if (juntadoras[j].falhou) {
                    throw new RuntimeException("Uma thread juntadora falhou");
                }
                novas[j] = juntadoras[j].resultado;
            }

            if (partes.length % 2 == 1) {
                novas[novas.length - 1] = partes[partes.length - 1];
            }

            partes = novas;
            rodada++;
        }

        return partes[0];
    }

    public static int lerInteiro(String texto, int minimo, int maximo) {
        while (true) {
            System.out.print(texto);
            try {
                int valor = Integer.parseInt(teclado.nextLine().trim());
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("Valor deve estar entre " + minimo + " e " + maximo);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro valido");
            }
        }
    }

    public static boolean estaOrdenado(byte[] vetor) {
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i - 1] > vetor[i]) {
                return false;
            }
        }
        return true;
    }

    public static void imprimir(byte[] vetor) {
        System.out.println("1 - Imprimir vetor inteiro");
        System.out.println("2 - Imprimir apenas uma parte");
        System.out.println("3 - Nao imprimir");
        int opcao = lerInteiro("Opcao: ", 1, 3);

        if (opcao == 1) {
            System.out.println(Arrays.toString(vetor));
        } else if (opcao == 2) {
            int de = lerInteiro("Da posicao (0 a " + (vetor.length - 1) + "): ", 0, vetor.length - 1);
            int ate = lerInteiro("Ate a posicao (" + de + " a " + (vetor.length - 1) + "): ", de, vetor.length - 1);
            System.out.println(Arrays.toString(Arrays.copyOfRange(vetor, de, ate + 1)));
        }
    }

    public static void ordenar() {

        long memoria = Runtime.getRuntime().maxMemory() / 8;
        int limite = (int) Math.min(Integer.MAX_VALUE - 8, memoria);

        int tamanho = lerInteiro("Quantos elementos tera o vetor (1 a " + limite + "): ", 1, limite);

        System.out.println("1 - Preencher a mao");
        System.out.println("2 - Preencher com numeros aleatorios");
        int modo = lerInteiro("Opcao: ", 1, 2);

        byte[] vetor;

        try {
            vetor = new byte[tamanho];
        } catch (OutOfMemoryError e) {
            System.out.println("Sem memoria para esse tamanho de vetor");
            return;
        }

        if (modo == 1) {
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = (byte) lerInteiro("vetor[" + i + "] (-128 a 127): ", -128, 127);
            }
        } else {
            Random random = new Random();
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = (byte) (random.nextInt(256) - 128);
            }
            System.out.println("Vetor gerado com " + tamanho + " elementos");
        }

        byte[] resultadoSequencial = null;
        byte[] resultadoParalelo = null;
        long tempoSequencial = 0;
        long tempoParalelo = 0;

        try {
            System.out.println("\nIniciando ordenacao sem paralelismo");
            long inicio = System.nanoTime();
            resultadoSequencial = mergeSort(vetor.clone());
            long fim = System.nanoTime();
            tempoSequencial = fim - inicio;
            System.out.println("Ordenacao sem paralelismo terminou");
        } catch (OutOfMemoryError e) {
            System.out.println("Sem memoria na ordenacao sem paralelismo");
        }

        try {
            System.out.println("\nIniciando ordenacao paralela");
            long inicio = System.nanoTime();
            resultadoParalelo = mergeSortParalelo(vetor.clone());
            long fim = System.nanoTime();
            tempoParalelo = fim - inicio;
            System.out.println("Ordenacao paralela terminou");
        } catch (InterruptedException e) {
            System.out.println("Ordenacao paralela interrompida");
        } catch (RuntimeException e) {
            System.out.println("Erro na ordenacao paralela: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            System.out.println("Sem memoria na ordenacao paralela");
        }

        System.out.println();
        if (resultadoSequencial != null) {
            System.out.printf("Tempo sem paralelismo: %.3f ms (ordenado: %b)%n",
                    tempoSequencial / 1_000_000.0, estaOrdenado(resultadoSequencial));
        }
        if (resultadoParalelo != null) {
            System.out.printf("Tempo paralelo: %.3f ms (ordenado: %b)%n",
                    tempoParalelo / 1_000_000.0, estaOrdenado(resultadoParalelo));
        }
        if (resultadoSequencial != null && resultadoParalelo != null) {
            System.out.println("Resultados iguais: " + Arrays.equals(resultadoSequencial, resultadoParalelo));
        }

        if (resultadoParalelo != null) {
            imprimir(resultadoParalelo);
        } else if (resultadoSequencial != null) {
            imprimir(resultadoSequencial);
        }
    }

    public static void main(String[] args) {

        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n1 - Ordenar vetor (sem paralelismo e paralelo)");
            System.out.println("2 - Estimar maior vetor");
            System.out.println("3 - Executar Dados (A e B)");
            System.out.println("0 - Sair");
            opcao = lerInteiro("Opcao: ", 0, 3);

            if (opcao == 1) {
                ordenar();
            } else if (opcao == 2) {
                MaiorVetorAproximado.main(null);
            } else if (opcao == 3) {
                Dados.main(null);
            }
        }

        System.out.println("Programa encerrado");
    }
}
