/**
 * User: outzider
 * Date: 12/20/13
 * Time: 12:58 PM
 */
public class MatrixUtils {

    public static int[][] transposeMatrix(int[][] original) {
            int height = original.length;
            int width = original[0].length;

            int[][] transposed = new int[width][height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int rgb = original[i][j];
                    transposed[j][i] = rgb;
                }
            }

            return transposed;
    }

    public static void printMatrix(int[][] matrix) {
        int height = matrix.length;
        int width = matrix[0].length;

        System.out.println("------");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.printf("%4d", matrix[i][j]);
            }
            System.out.println();
        }

    }

    public static void main(String[] args) {
        int[] row1 = {1, 2, 3, 4, 5};
        int[] row2 = {1, 2, 3, 4, 5};
        int[] row3 = {1, 2, 3, 4, 5};
        int[][] test = {row1, row2, row3};
        printMatrix(test);
        test = transposeMatrix(test);
        printMatrix(test);
        test = transposeMatrix(test);
        printMatrix(test);
    }

}
