import java.util.*;

/**
* User: outzider
* Date: 12/12/13
* Time: 3:57 PM
*/

public class PixelSP {

    private class Pixel {
        private int row;
        private int column;
        private double energy;
        private List<Pixel> adj;
        private double pathLength;
        private int parentXCoordinate;

        private Pixel(int row, int column, double energy) {
            this.row = row;
            this.column = column;
            this.energy = energy;

            adj = new ArrayList<Pixel>();
        }

        @Override
        public String toString() {
            return String.format("(%d, %d) energy: %f path-len:%f", row, column, energy, pathLength);
        }

    }

    private class PixelPathLengthComparator implements Comparator<Pixel> {
        @Override
        public int compare(Pixel pixel, Pixel pixel2) {
            return Double.valueOf(pixel.pathLength).compareTo(pixel2.pathLength);
        }
    }

    private Pixel[][] pixelArray;


    PixelSP(Picture picture, double[][] energyMatrix) {
        int height = picture.height();
        int width = picture.width();

        pixelArray = new Pixel[height][width];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                pixelArray[row][column] = new Pixel(row, column, energyMatrix[row][column]);
            }
        }
    }

    private void transposeMatrix() {
        int height = pixelArray.length;
        int width = pixelArray[0].length;

        Pixel[][] transpose = new Pixel[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel pixel = pixelArray[i][j];
                pixel.row = j;
                pixel.column = i;
                transpose[j][i] = pixel;
            }
        }

        pixelArray = transpose;
    }

    private void buildAdjList() {
        int height = pixelArray.length;
        int width = pixelArray[0].length;

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                initializePixelPathLength(row, column);

                Pixel pixel = pixelArray[row][column];
                List<Pixel> adjs = setPixelAdjacentPixels(height, width, row, column, pixel);
                setAdjacentPixelsParentXCoordinate(row, pixel, adjs);
            }
        }
    }

    private List<Pixel> setPixelAdjacentPixels(int matrixHeight, int matrixWidth, int row, int column, Pixel pixel) {
        List<Pixel> adjs = pixel.adj;

        //adjacent left
        if (column > 0 && row < matrixHeight - 1) {
            adjs.add(pixelArray[row + 1][column - 1]);
        }

        //adjacent
        if (row < matrixHeight - 1) {
            adjs.add(pixelArray[row + 1][column]);
        }

        //adjacent right
        if (column < matrixWidth - 1 && row < matrixHeight - 1) {
            adjs.add(pixelArray[row + 1][column + 1]);
        }
        return adjs;
    }

    private void setAdjacentPixelsParentXCoordinate(int row, Pixel parentPixel, List<Pixel> adjacentPixels) {
        if (row > 0) {
            for (Pixel adj : adjacentPixels) {
                adj.parentXCoordinate = parentPixel.column;
            }
        }
    }

    private void initializePixelPathLength(int row, int column) {
        if (row == 0)
            pixelArray[row][column].pathLength = 0.0;
        else
            pixelArray[row][column].pathLength = Double.MAX_VALUE;
    }

    public int[] findVerticalShortestPath() {
        int height = pixelArray.length;
        int width = pixelArray[0].length;

        buildAdjList();

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Pixel pixel = pixelArray[row][column];
                List<Pixel> adjPixels = pixel.adj;
                for (Pixel adjPixel : adjPixels) {
                    relaxPixel(pixel, adjPixel);
                }
            }
        }

        Stack<Pixel> stack = new Stack<Pixel>();
        MinPQ<Pixel> lastRow = new MinPQ<Pixel>(new PixelPathLengthComparator());

        for (int column = 0; column < width; column++) {
            lastRow.insert(pixelArray[height - 1][column]);
        }

        Pixel currentPixel = lastRow.delMin();
        stack.push(currentPixel);
        for (int row = height - 2; row >= 0; row--) {
            currentPixel = pixelArray[row][currentPixel.parentXCoordinate];
            stack.push(currentPixel);
        }

        int[] results = new int[stack.size()];
        int index=0;
        for (Pixel pixel : stack) {
            results[index++] = pixel.column;
        }

        return results;
    }

    private void relaxPixel(Pixel parentPixel, Pixel adjacentPixel) {
        if (adjacentPixel.pathLength > parentPixel.pathLength + parentPixel.energy) {
            adjacentPixel.pathLength = parentPixel.pathLength + parentPixel.energy;
            adjacentPixel.parentXCoordinate = parentPixel.column;
        }
    }

    public int[] findHorizontalShortestPath() {
        transposeMatrix();
        int[] results = findVerticalShortestPath();
        transposeMatrix();
        return results;
    }
}
