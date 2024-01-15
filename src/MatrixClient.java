import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class MatrixClient {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 12345;


    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Random random = new Random();
            int N = random.nextInt(1000) + 1000; //генерація випадкових чисел
            int M = random.nextInt(1000) + 1000;
            int L = random.nextInt(1000) + 1000;

            System.out.println("N = " + N);
            System.out.println("M = " + M);
            System.out.println("L = " + L);
            System.out.println("Generating matrix's...");
            int[][] matrixA = generateRandomMatrix(N, M); //генерація матриць (заповнення їх випадковими числами)
            int[][] matrixB = generateRandomMatrix(M, L);
            System.out.println("Matrix's generated.");

            //надсилання матриць на сервер
            System.out.println("Sending matrix's...");
            out.writeObject(matrixA);
            System.out.println("Matrix A sent.");
            out.writeObject(matrixB);
            System.out.println("Matrix B sent.");
            out.flush();

            System.out.println("Waiting for result...");

            //прийом результату від серверу
            int[][] resultMatrix = (int[][]) in.readObject();
            System.out.print("Result accepted. Show result matrix? y/n:");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if(answer.equals("y")){
                displayMatrix(resultMatrix);
            }
            else System.out.println("Goodbye!");
            out.close(); //закриваємо всі потоки вводу/виводу для економії ресурсів
            in.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int[][] generateRandomMatrix(int rows, int cols) { //метод який заповнює матрицб випаковими елекментами
        int[][] matrix = new int[rows][cols];
        Random random = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(100);
            }
        }

        return matrix;
    }

    private static void displayMatrix(int[][] matrix) { //метод для друкування матариці в консоль
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
