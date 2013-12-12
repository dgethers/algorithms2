import java.util.*;
import java.util.Stack;

/**
 * User: outzider
 * Date: 11/25/13
 * Time: 10:47 AM
 */
public class SeamCarver {

    private static final int MAX_GRADIENT_ENERGY = 195075;
    private final Picture picture;
    List<Pixel> path;

    public SeamCarver(Picture picture) {
        this.picture = picture;
        path = new ArrayList<Pixel>();
    }

    // current picture
    public Picture picture() {

        return picture;
    }

    // width  of current picture
    public int width() {

        return picture.width();
    }

    // height of current picture
    public int height() {

        return picture.height();
    }

    // energy of pixel at column x and row y in current picture
    public double energy(int x, int y) {
        int output = MAX_GRADIENT_ENERGY;

        if ((x > 0 && x < picture.width() - 1) && (y > 0 && y < picture.height() - 1)) {
            int deltaXSquared = computeXDualGradientEnergy(x, y);
            int deltaYSquared = computeYDualGradientEnergy(x, y);
            output = deltaXSquared + deltaYSquared;
        }

        return output;
    }

    private int computeXDualGradientEnergy(int x, int y) {
        int redX = picture.get(x + 1, y).getRed() - picture.get(x - 1, y).getRed();
        int greenX = picture.get(x + 1, y).getGreen() - picture.get(x - 1, y).getGreen();
        int blueX = picture.get(x + 1, y).getBlue() - picture.get(x - 1, y).getBlue();

        return (int) (Math.pow(redX, 2) + Math.pow(greenX, 2) + Math.pow(blueX, 2));
    }

    private int computeYDualGradientEnergy(int x, int y) {
        int redY = picture.get(x, y + 1).getRed() - picture.get(x, y - 1).getRed();
        int greenY = picture.get(x, y + 1).getGreen() - picture.get(x, y - 1).getGreen();
        int blueY = picture.get(x, y + 1).getBlue() - picture.get(x, y - 1).getBlue();

        return (int) (Math.pow(redY, 2) + Math.pow(greenY, 2) + Math.pow(blueY, 2));
    }

    // sequence of indices for horizontal seam in current picture
    public int[] findHorizontalSeam() {

        return new int[]{}; //TODO: Implement this
    }

    private class Pixel implements Comparable<Pixel> {
        private int row;
        private int column;
        private double energy;
        private List<Pixel> adj;
        private double distanceFromParent;

        private Pixel(int row, int column, double energy) {
            this.row = row;
            this.column = column;
            this.energy = energy;
        }

        @Override
        public int compareTo(Pixel pixel) {
            return Double.valueOf(energy).compareTo(pixel.energy);
        }

        @Override
        public String toString() {
            return String.format("(%d, %d) erg: %f adj->%s", row, column, energy, adj != null ? adj.toString() : "Nothing");
        }
    }

    private class PixelSP {
        Pixel[][] energyMatrix;
        double[] distTo;
        Pixel[] edgeTo;

        private PixelSP(Pixel[][] energyMatrix) {
            this.energyMatrix = energyMatrix;
            this.distTo = new double[picture.height()];
            this.edgeTo = new Pixel[picture.height()];

            for (int i = 0; i < distTo.length; i++) {
                distTo[i] = Double.POSITIVE_INFINITY;
            }
        }

        private void buildAdjList() {
            //set adj list and calculate distances from parent to each adj child
            for (int row = 0; row < picture.height(); row++) {
                for (int column = 0; column < picture.width(); column++) {
//                System.out.printf("row: %d column: %d \n", row, column);
                    Pixel pixel = energyMatrix[row][column];
                    List<Pixel> adjs = new ArrayList<Pixel>();

                    if (row < picture.height() - 1) {
                        Pixel originalPixel = energyMatrix[row + 1][column];
                        Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                        tmp.distanceFromParent = pixel.energy + tmp.energy;
                        adjs.add(tmp);
                    }

                    if (column < picture.width() - 1 && row < picture.height() - 1) {
                        Pixel originalPixel = energyMatrix[row + 1][column + 1];
                        Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                        tmp.distanceFromParent = pixel.energy + tmp.energy;
                        adjs.add(tmp);
                    }

                    if (column > 0 && row < picture.height() - 1) {
                        Pixel originalPixel = energyMatrix[row + 1][column - 1];
                        Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                        tmp.distanceFromParent = pixel.energy + tmp.energy;
                        adjs.add(tmp);
                    }

                    pixel.adj = adjs;

//                System.out.printf("(%d,%d) energy: %f - children %s", row, column, energyMatrix[row][column].energy, adjs.toString());
                }
            }

            /*for (int row = 0; row < picture.height(); row++) {
                for (int column = 0; column < picture.width(); column++) {
                    System.out.println(energyMatrix[row][column]);
                }
            }*/
        }

        private void findShortestPath() {

            java.util.Stack<Pixel> currentPath = new Stack<Pixel>(); //TODO: Replace with class data structure
            Pixel[] firstRowPixels = energyMatrix[picture.height() - 1];
            double rowSmallestPathTotal = Double.MAX_VALUE;
            for (Pixel rowPixel : firstRowPixels) {
                double pixelAdjShortestPath = Double.MAX_VALUE;
                for (Pixel adjPixel : rowPixel.adj) {
                    if ((rowPixel.energy + adjPixel.energy) < pixelAdjShortestPath) {
                        pixelAdjShortestPath = rowPixel.energy + adjPixel.energy;
                        System.out.printf("(%d,%d) - shortestPath = %f\n", rowPixel.row, rowPixel.column, pixelAdjShortestPath);
                    }
                }
                if (pixelAdjShortestPath < rowSmallestPathTotal) {
                    System.out.printf("(%d,%d) - short:%f < total:%f\n", rowPixel.row, rowPixel.column, pixelAdjShortestPath, rowSmallestPathTotal);
                    if (currentPath.size() > 0) {
                        System.out.println("stack has the current element at the top: " + currentPath.peek());
                        System.out.printf("popping pixel (%d, %d) off stack\n", rowPixel.row, rowPixel.column);
                        currentPath.pop();
                    }
                    System.out.printf("pushed pixel: (%d,%d) onto stack \n", rowPixel.row, rowPixel.column);
                    currentPath.push(rowPixel);
                    rowSmallestPathTotal = pixelAdjShortestPath;
                }
            }


            while (currentPath.size() > 0) {
                Pixel pixel = currentPath.pop();
                System.out.printf("(%d,%d) popped from path\n", pixel.row, pixel.column);
                List<Pixel> adj = pixel.adj;

                double pixelAdjShortestPath = Double.MAX_VALUE;
                for (Pixel adjPixel : adj) {
                    if ((pixel.energy + adjPixel.energy) < pixelAdjShortestPath) {
                        if (currentPath.size() > 0) {
                            currentPath.pop();
                        }
                        currentPath.push(energyMatrix[adjPixel.row][adjPixel.column]);
                        pixelAdjShortestPath = pixel.energy + adjPixel.energy;
                    }
                }
            }
        }
    }

    // sequence of indices for vertical seam in current picture
    public int[] findVerticalSeam() {
        Pixel[][] energyMatrix = new Pixel[picture.height()][picture.width()];

        //build energy matrix
        for (int row = 0; row < picture.height(); row++) {
            for (int column = 0; column < picture.width(); column++) {
                double energy = energy(column, row);
                Pixel pixel = new Pixel(row, column, energy);
                pixel.distanceFromParent = energy;
                energyMatrix[row][column] = pixel;
            }
        }

        PixelSP pixelSP = new PixelSP(energyMatrix);
        pixelSP.buildAdjList();
        pixelSP.findShortestPath();

        return new int[]{3, 2, 2, 2, 3}; //TODO: Implement this
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] a) {

        //TODO: Implement this
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] a) {

        //TODO: Implement this
    }
}
