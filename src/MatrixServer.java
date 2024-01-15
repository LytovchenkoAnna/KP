import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MatrixServer {
    private static final int PORT = 12345; //порт на якому буде працювати локальний сервер
    private static final int THREAD_POOL_SIZE = 5; //розмір тред пулу, тобто кількість робочих потоків в ньому

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { //створюється серверний сокет
            System.out.println("Server is running and waiting for connections...");

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); //створюється тред пул на 5 потоків. Цей тред пул займається обробкою клієнтів

            while (true) {
                Socket clientSocket = serverSocket.accept(); //встановлення з'єднання з клієнтом
                System.out.println("Client connected: " + clientSocket); //друк інформації

                executorService.submit(() -> handleClient(clientSocket)); //додаємо "задачу" у вигляді клієнта в тред пул
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) { //цей метод займається обробкою одного клієнта
        //кожен потік в тред пулі буде вионувати цей код
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream()); //ініціалізація засобів вводу/виводу даних для вза'ємодії з клієнтом
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {

            int port = clientSocket.getPort(); //тут просто отримуємо порт клієнта
            System.out.println("Waiting for matrix's from " + port);
            int[][] matrixA = (int[][]) in.readObject(); //читаємо першу матрицю від клієнта
            System.out.println("Matrix A accepted from " + port);
            int[][] matrixB = (int[][]) in.readObject(); //читаємо другу матрицю від клієнта
            System.out.println("Matrix B accepted from " + port);

            System.out.println("Calculating result for " + port);
            int[][] resultMatrix = multiplyMatrices(matrixA, matrixB); //викликаємо метод для обрахунку добутку двох матриць

            out.writeObject(resultMatrix); //передаємо результуючу матрицю клієнту
            out.flush();
            System.out.println("Result sent to " + port);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int[][] multiplyMatrices(int[][] matrixA, int[][] matrixB) { //метод для обрахунку добутку двох матриць
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] resultMatrix = new int[rowsA][colsB];

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); //знову створюємо тред пул, але цей тредпул вже буде займатись обчисленням матриці

        for (int i = 0; i < rowsA; i++) {
            final int rowIdx = i;
            threadPool.submit(() -> { // кожен потік в тредпулі займається знаходженням резуьтату в одному рядку результуючої матриці
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        resultMatrix[rowIdx][j] += matrixA[rowIdx][k] * matrixB[k][j];
                    }
                }
            });
        }

        threadPool.shutdown(); // тред пул потрібно зупинити, щоб звільнити ресурси
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return resultMatrix;
    }
}
