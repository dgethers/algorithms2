import java.util.*;

/**
* User: outzider
* Date: 12/12/13
* Time: 3:57 PM
*/

public class PixelSP {

    private final Pixel[][] pixelArray;
    private final Picture picture;

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
            adj = new ArrayList<Pixel>();
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

    PixelSP(Picture picture, double[][] energyMatrix) {
        this.picture = picture;

        pixelArray = new Pixel[picture.height()][picture.width()];
        for (int row = 0; row < picture.height(); row++) {
            for (int column = 0; column < picture.width(); column++) {
                pixelArray[row][column] = new Pixel(row, column, energyMatrix[row][column]);
            }
        }
    }

    public void buildAdjList() {
        //set adj list and calculate distances from parent to each adj child
        for (int row = picture.height() - 1; row > 0; row--) {
            for (int column = 0; column < picture.width(); column++) {
//                System.out.printf("row: %d column: %d \n", row, column);
                Pixel pixel = pixelArray[row][column];
                List<Pixel> adjs = pixel.adj;

                //adjacent left
                if (column > 0 && row > 0) {
                    Pixel originalPixel = pixelArray[row - 1][column - 1];
                    Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                    tmp.distanceFromParent = pixel.energy + tmp.energy;
                    adjs.add(tmp);
                }

                //adjacent
                if (row > 0) {
                    Pixel originalPixel = pixelArray[row - 1][column];
                    Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                    tmp.distanceFromParent = pixel.energy + tmp.energy;
                    adjs.add(tmp);
                }

                //adjacent right
                if (column < picture.width() - 1 && row > 0) {
                    Pixel originalPixel = pixelArray[row - 1][column + 1];
                    Pixel tmp = new Pixel(originalPixel.row, originalPixel.column, originalPixel.energy);
                    tmp.distanceFromParent = pixel.energy + tmp.energy;
                    adjs.add(tmp);
                }

//                System.out.printf("(%d,%d) energy: %f - adj %s", row, column, pixelArray[row][column].energy, adjs.toString());
            }
        }
    }

    public int[] findShortestPath() {
        int[] results = new int[picture.height()];
        int index = picture.height() - 1;

        java.util.Stack<Pixel> currentPath = new java.util.Stack<Pixel>(); //TODO: Replace with class data structure
        Pixel[] firstRowPixels = pixelArray[picture.height() - 1];
        double rowSmallestPathTotal = Double.MAX_VALUE;
        for (Pixel rowPixel : firstRowPixels) {
            double pixelAdjShortestPath = Double.MAX_VALUE;
            for (Pixel adjPixel : rowPixel.adj) {
                if ((rowPixel.energy + adjPixel.energy) < pixelAdjShortestPath) {
                    pixelAdjShortestPath = rowPixel.energy + adjPixel.energy;
//                        System.out.printf("(%d,%d) - shortestPath = %f\n", rowPixel.row, rowPixel.column, pixelAdjShortestPath);
                }
            }
            if (pixelAdjShortestPath < rowSmallestPathTotal) {
//                    System.out.printf("(%d,%d) - short:%f < total:%f\n", rowPixel.row, rowPixel.column, pixelAdjShortestPath, rowSmallestPathTotal);
                if (currentPath.size() > 0) {
//                        System.out.println("stack has the current element at the top: " + currentPath.peek());
//                        System.out.printf("popping pixel (%d, %d) off stack\n", rowPixel.row, rowPixel.column);
                    currentPath.pop();
                }
//                    System.out.printf("pushed pixel: (%d,%d) onto stack \n", rowPixel.row, rowPixel.column);
                currentPath.push(rowPixel);
                rowSmallestPathTotal = pixelAdjShortestPath;
            }
        }


        while (currentPath.size() > 0) {
            Pixel pixel = currentPath.pop();
            results[index--] = pixel.column;
            System.out.printf("(%d,%d) \n", pixel.row, pixel.column);
            List<Pixel> adj = pixel.adj;

            double pixelAdjShortestPath = Double.MAX_VALUE;
            for (Pixel adjPixel : adj) {
                if ((pixel.energy + adjPixel.energy) < pixelAdjShortestPath) {
                    if (currentPath.size() > 0) {
                        currentPath.pop();
                    }
                    currentPath.push(pixelArray[adjPixel.row][adjPixel.column]);
                    pixelAdjShortestPath = pixel.energy + adjPixel.energy;
                }
            }
        }

        return results;
    }
}
