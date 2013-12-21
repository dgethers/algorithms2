import java.awt.Color;

/**
 * User: outzider
 * Date: 11/25/13
 * Time: 10:47 AM
 */
public class SeamCarver {

    private static final int MAX_GRADIENT_ENERGY = 195075;
    private Picture picture;
    private final double[][] energyMatrix;
    private int[][] colorArray;

    public SeamCarver(Picture picture) {
        this.picture = picture;

        energyMatrix = new double[picture.height()][picture.width()];

        //build energy matrix
        for (int row = 0; row < picture.height(); row++) {
            for (int column = 0; column < picture.width(); column++) {
                double energy = energy(column, row);
                energyMatrix[row][column] = energy;
            }
        }

        //build image array
        colorArray = new int[picture.height()][picture.width()];
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++) {
                colorArray[i][j] = picture.get(j, i).getRGB();
            }
        }
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
        if ((x < 0 || y < 0) || (x > picture.width() - 1 || y > picture.height() - 1)) {
            throw new IllegalArgumentException("Input outside expected parameters");
        }

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

        return (redX * redX) + (greenX * greenX) + (blueX * blueX);
    }

    private int computeYDualGradientEnergy(int x, int y) {
        int redY = picture.get(x, y + 1).getRed() - picture.get(x, y - 1).getRed();
        int greenY = picture.get(x, y + 1).getGreen() - picture.get(x, y - 1).getGreen();
        int blueY = picture.get(x, y + 1).getBlue() - picture.get(x, y - 1).getBlue();

        return (redY * redY) + (greenY * greenY) + (blueY * blueY);
    }

    // sequence of indices for horizontal seam in current picture
    public int[] findHorizontalSeam() {

        PixelSP pixelSP = new PixelSP(picture, energyMatrix);
        return pixelSP.findHorizontalShortestPath();
    }



    // sequence of indices for vertical seam in current picture
    public int[] findVerticalSeam() {

        PixelSP pixelSP = new PixelSP(picture, energyMatrix);
        return pixelSP.findVerticalShortestPath();

    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] values) {
        if (values.length < picture.width() || values.length > picture.width()) {
            throw new IllegalArgumentException("Length of input array is less/greater than picture size");
        }

        colorArray = MatrixUtils.transposeMatrix(colorArray);
        removeVerticalSeam(values);
        colorArray = MatrixUtils.transposeMatrix(colorArray);
        convertFromArrayToPicture();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] values) {
        if (values.length < colorArray.length || values.length > colorArray.length) {
            throw new IllegalArgumentException("Length of input array is less/greater than picture size");
        }

        int index = 0;

        for (int i = 0; i < picture.height(); i++) {
            int[] row = colorArray[i];
            int[] newRow = new int[row.length - 1];
            int position = index++;
            System.arraycopy(row, 0, newRow, 0, values[position]);
            System.arraycopy(row, values[position] + 1, newRow, values[position], newRow.length - values[position]);
            colorArray[i] = newRow;
        }

        convertFromArrayToPicture();

    }

    private void convertFromArrayToPicture() {
        int height = colorArray.length;
        int width = colorArray[0].length;
        Picture newImage = new Picture(width, height);
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                newImage.set(column, row, new Color(colorArray[row][column]));
            }
        }

        picture = newImage;
    }

//    public static void main(String[] args) {
        /*
        pic is 6x5, (i, j) = (-1, 4)
             - IndexOutOfBoundsException NOT thrown for energy()
            pic is 6x5, (i, j) = (6, 4)
             - IndexOutOfBoundsException NOT thrown for energy()
            pic is 6x5, (i, j) = (5, 5)
             - IndexOutOfBoundsException NOT thrown for energy()
            pic is 6x5, (i, j) = (4, -1)
             - IndexOutOfBoundsException NOT thrown for energy()
            pic is 6x5, (i, j) = (4, 5)
             - IndexOutOfBoundsException NOT thrown for energy()
         */
//        SeamCarver carver = new SeamCarver(new Picture("in/seamcarving/6x5.png"));
//        carver.energy(-1, 4);
//        carver.energy(6, 4);
//        carver.energy(5, 5);
//        carver.energy(4, -1);
//        carver.energy(4, 5);
//        carver.removeHorizontalSeam(new int[1]);
//        carver.removeHorizontalSeam(new int[5]);
//        carver.removeVerticalSeam(new int[1]);
//        carver.removeVerticalSeam(new int[6]);
//        int[] horizontalSeam = carver.findHorizontalSeam();
//        System.out.println("---Before---");
//        printPictureEnergy(carver.picture);
//        carver.removeHorizontalSeam(horizontalSeam);
//        System.out.println("---After---");
//        printPictureEnergy(carver.picture);
//    }
    
    /*public static void printPictureEnergy(Picture picture) {
        System.out.printf("image is %d pixels wide by %d pixels high.\n", picture.width(), picture.height());

        SeamCarver sc = new SeamCarver(picture);

        System.out.printf("Printing energy calculated for each pixel.\n");

        for (int j = 0; j < sc.height(); j++)
        {
            for (int i = 0; i < sc.width(); i++)
                System.out.printf("%9.0f ", sc.energy(i, j));

            System.out.println();
        }
        
    }*/
}
