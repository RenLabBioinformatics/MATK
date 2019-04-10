import SingleNucleotide.CNN.NetworkTopology;
import SingleNucleotide.CNN.TopologyReader;

import java.io.File;
import java.util.LinkedList;

public class ConstructNetworkTopology {
    private LinkedList<NetworkTopology> topologyList;

    public ConstructNetworkTopology() {
        topologyList = new LinkedList<>();
        //CNN layer
        NetworkTopology cnnLayer = new NetworkTopology();
        cnnLayer.setLayerType(NetworkTopology.Convolution);
        cnnLayer.setWidth(65);
        cnnLayer.setHeight(4);
        cnnLayer.setChannels(2);
        cnnLayer.setKernelSize(4,5);
        cnnLayer.setStride(1,1);
        cnnLayer.setNumFilters(10);
        topologyList.add(cnnLayer);
        //Max pooling
        NetworkTopology maxPoolLyer = new NetworkTopology();
        maxPoolLyer.setLayerType(NetworkTopology.MaxPooling);
        maxPoolLyer.setPadding(0,1);
        maxPoolLyer.setKernelSize(1,3);
        maxPoolLyer.setStride(1,3);
        topologyList.add(maxPoolLyer);
        //Dense layer 1
        NetworkTopology denseLayer1 = new NetworkTopology();
        denseLayer1.setLayerType(NetworkTopology.DenseLayer);
        denseLayer1.setInputSize(210);
        denseLayer1.setOutputSize(180);
        topologyList.add(denseLayer1);
        //Dense layer 2
        NetworkTopology denseLayer2 = new NetworkTopology();
        denseLayer2.setLayerType(NetworkTopology.DenseLayer);
        denseLayer2.setInputSize(180);
        denseLayer2.setOutputSize(150);
        topologyList.add(denseLayer2);
        //Dense layer 3
        NetworkTopology denseLayer3 = new NetworkTopology();
        denseLayer3.setLayerType(NetworkTopology.DenseLayer);
        denseLayer3.setInputSize(150);
        denseLayer3.setOutputSize(120);
        topologyList.add(denseLayer3);
        //Dense layer 4
        NetworkTopology denseLayer4 = new NetworkTopology();
        denseLayer4.setLayerType(NetworkTopology.DenseLayer);
        denseLayer4.setInputSize(120);
        denseLayer4.setOutputSize(90);
        topologyList.add(denseLayer4);
        //Dense layer 5
        NetworkTopology denseLayer5 = new NetworkTopology();
        denseLayer5.setLayerType(NetworkTopology.DenseLayer);
        denseLayer5.setInputSize(90);
        denseLayer5.setOutputSize(60);
        topologyList.add(denseLayer5);
        //Output layer
        NetworkTopology outputLayer = new NetworkTopology();
        outputLayer.setLayerType(NetworkTopology.OutputLayer);
        outputLayer.setInputSize(60);
        outputLayer.setOutputSize(2);
        topologyList.add(outputLayer);
    }

    public void WriteTopologyFile(File saveFile) {
        TopologyReader topologyReader = new TopologyReader(topologyList);
        topologyReader.WriteTopologyParameters(saveFile);
    }

    public static void main(String[] args) {
        ConstructNetworkTopology constructNetworkTopology = new ConstructNetworkTopology();
        constructNetworkTopology.WriteTopologyFile(new File("E:\\工作文档\\MATK\\SingleNucleotide\\OneHot_PSSM_MeRIP_Model.json"));
    }
}
