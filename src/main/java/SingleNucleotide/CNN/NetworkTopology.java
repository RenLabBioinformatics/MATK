package SingleNucleotide.CNN;

/**
 * Created by Ben on 2018/5/8.
 * Specify the topological parameters for each layer
 */
public class NetworkTopology {
    private int[] kernelSize;
    private int[] stride;
    private int[] padding;
    private int channels;
    private int numFilters;//Number of kernels for the convolution layer
    private int inputSize;
    private int outputSize;
    private double dropout;
    private int LayerType;
    private int height;
    private int width;

    public static int Convolution = 1;
    public static int MaxPooling = 2;
    public static int DenseLayer = 3;
    public static int OutputLayer = 4;

    public NetworkTopology() {
        kernelSize = new int[2];
        stride = new int[2];
        padding = new int[2];
    }

    public void setKernelSize(int height, int width) {
        kernelSize[0] = height;
        kernelSize[1] = width;
    }

    public void setStride(int height, int width) {
        stride[0] = height;
        stride[1] = width;
    }

    public void setPadding(int height, int width) {
        padding[0] = height;
        padding[1] = width;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public void setNumFilters(int numFilters) {
        this.numFilters = numFilters;
    }

    public int[] getKernelSize() {
        return kernelSize;
    }

    public int[] getStride() {
        return stride;
    }

    public int[] getPadding() {
        return padding;
    }

    public int getChannels() {
        return channels;
    }

    public int getNumFilters() {
        return numFilters;
    }

    public int getInputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(int outputSize) {
        this.outputSize = outputSize;
    }

    public double getDropout() {
        return dropout;
    }

    public void setDropout(double dropout) {
        this.dropout = dropout;
    }

    public int getLayerType() {
        return LayerType;
    }

    public void setLayerType(int layerType) {
        LayerType = layerType;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
