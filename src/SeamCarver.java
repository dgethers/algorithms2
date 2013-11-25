/**
 * User: outzider
 * Date: 11/25/13
 * Time: 10:47 AM
 */
public class SeamCarver {

    private static final int MAX_GRADIENT_ENERGY = 195075;
    private final Picture picture;

    public SeamCarver(Picture picture) {
        this.picture = picture;
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

    // sequence of indices for vertical seam in current picture
    public int[] findVerticalSeam() {

        return new int[]{}; //TODO: Implement this
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
