package SingleNucleotide.CNN;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.*;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/5/8.
 */
public class TopologyReader {
    private LinkedList<NetworkTopology> topologyList;

    public TopologyReader(LinkedList<NetworkTopology> topologyList) {
        this.topologyList = topologyList;
    }

    private void JSONParser(String jsonStr) {
        topologyList = new LinkedList<NetworkTopology>();
        JSONTokener jsonTokener = new JSONTokener(jsonStr);
        JSONObject jsonObjLayers = new JSONObject(jsonTokener);

        int channel = jsonObjLayers.getInt("Channels");
        int height = jsonObjLayers.getInt("Height");
        int width = jsonObjLayers.getInt("Width");

        JSONArray jsonArrLayers = jsonObjLayers.getJSONArray("Layers");
        int numLayers = jsonArrLayers.length();
        for (int i = 0; i < numLayers; i++) {
            NetworkTopology networkTopology = new NetworkTopology();

            JSONObject jsonObjLayerParam = jsonArrLayers.getJSONObject(i);
            int layerType = jsonObjLayerParam.getInt("LayerType");
            networkTopology.setLayerType(layerType);
            if ((layerType == NetworkTopology.Convolution) || (layerType == NetworkTopology.MaxPooling)) {
                networkTopology.setKernelSize(jsonObjLayerParam.getInt("KernelHeight"), jsonObjLayerParam.getInt("KernelWidth"));
                networkTopology.setStride(jsonObjLayerParam.getInt("StrideHeight"), jsonObjLayerParam.getInt("StrideWidth"));
                networkTopology.setPadding(jsonObjLayerParam.getInt("PaddingHeight"), jsonObjLayerParam.getInt("PaddingWidth"));
                if (layerType == NetworkTopology.Convolution) {
                    networkTopology.setNumFilters(jsonObjLayerParam.getInt("NumOfFilters"));
                }
            }
            if ( (layerType == NetworkTopology.DenseLayer) || (layerType == NetworkTopology.OutputLayer) ) {
                networkTopology.setInputSize(jsonObjLayerParam.getInt("InputSize"));
                networkTopology.setOutputSize(jsonObjLayerParam.getInt("OutputSize"));
                if( layerType == NetworkTopology.DenseLayer ) {
                    if(!jsonObjLayerParam.isNull("Dropout"))
                        networkTopology.setDropout(jsonObjLayerParam.getDouble("Dropout"));
                }
            }

            topologyList.add(networkTopology);
        }

        topologyList.getFirst().setChannels(channel);
        topologyList.getFirst().setHeight(height);
        topologyList.getFirst().setWidth(width);
    }

    public TopologyReader(File topologyFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(topologyFile));
            String jsonStr = "", strLine;
            while (br.ready()) {
                strLine = br.readLine();
                jsonStr = jsonStr + strLine;
            }
            br.close();
            //Parse json string
            JSONParser(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TopologyReader(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(TopologyReader.class.getResourceAsStream(filePath)));
            String jsonStr = "", strLine;
            while (br.ready()) {
                strLine = br.readLine();
                jsonStr = jsonStr + strLine;
            }
            br.close();
            //Parse json string
            JSONParser(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<NetworkTopology> getTopologyList() {
        return topologyList;
    }

    public void WriteTopologyParameters(File topologyFile) {
        try {
            FileWriter fw = new FileWriter(topologyFile);
            JSONStringer jsonStringer = new JSONStringer();

            jsonStringer.object();
            jsonStringer.key("Layers");
            JSONArray jsonArrLayers = new JSONArray();
            for (NetworkTopology networkTopology : topologyList) {
                JSONObject jsonObjLayerParm = new JSONObject();
                jsonObjLayerParm.put("LayerType", networkTopology.getLayerType());
                if ((networkTopology.getLayerType() == NetworkTopology.Convolution) || (networkTopology.getLayerType() == NetworkTopology.MaxPooling)) {
                    jsonObjLayerParm.put("KernelHeight", networkTopology.getKernelSize()[0]);
                    jsonObjLayerParm.put("KernelWidth", networkTopology.getKernelSize()[1]);
                    jsonObjLayerParm.put("StrideHeight", networkTopology.getStride()[0]);
                    jsonObjLayerParm.put("StrideWidth", networkTopology.getStride()[1]);
                    jsonObjLayerParm.put("PaddingHeight", networkTopology.getPadding()[0]);
                    jsonObjLayerParm.put("PaddingWidth", networkTopology.getPadding()[1]);
                    if (networkTopology.getLayerType() == NetworkTopology.Convolution) {
                        jsonObjLayerParm.put("NumOfFilters", networkTopology.getNumFilters());
                    }
                }
                if ( (networkTopology.getLayerType() == NetworkTopology.DenseLayer) || (networkTopology.getLayerType() == NetworkTopology.OutputLayer) ) {
                    jsonObjLayerParm.put("InputSize", networkTopology.getInputSize());
                    jsonObjLayerParm.put("OutputSize", networkTopology.getOutputSize());
                    if( networkTopology.getLayerType() == NetworkTopology.DenseLayer ) {
                        if( networkTopology.getDropout() > 0 )
                            jsonObjLayerParm.put("Dropout", networkTopology.getDropout());
                    }
                }
                jsonArrLayers.put(jsonObjLayerParm);
            }
            jsonStringer.value(jsonArrLayers);

            jsonStringer.key("Channels");
            jsonStringer.value(topologyList.getFirst().getChannels());
            jsonStringer.key("Height");
            jsonStringer.value(topologyList.getFirst().getHeight());
            jsonStringer.key("Width");
            jsonStringer.value(topologyList.getFirst().getWidth());
            jsonStringer.endObject();

            fw.write(jsonStringer.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void JSONWriteTest(File saveFile) {
        LinkedList<NetworkTopology> topologyList = new LinkedList<NetworkTopology>();

        NetworkTopology convolution1 = new NetworkTopology();
        convolution1.setChannels(1);
        convolution1.setHeight(4);
        convolution1.setWidth(65);
        convolution1.setKernelSize(4, 5);
        convolution1.setNumFilters(20);
        convolution1.setStride(1, 5);
        convolution1.setPadding(0, 0);
        convolution1.setLayerType(NetworkTopology.Convolution);
        topologyList.add(convolution1);

        NetworkTopology maxPooling1 = new NetworkTopology();
        maxPooling1.setKernelSize(1, 3);
        maxPooling1.setStride(1, 3);
        maxPooling1.setPadding(0, 1);
        maxPooling1.setLayerType(NetworkTopology.MaxPooling);
        topologyList.add(maxPooling1);

        NetworkTopology dense1 = new NetworkTopology();
        dense1.setInputSize(100);
        dense1.setOutputSize(80);
        dense1.setLayerType(NetworkTopology.DenseLayer);
        topologyList.add(dense1);

        NetworkTopology dense2 = new NetworkTopology();
        dense2.setInputSize(80);
        dense2.setOutputSize(60);
        dense2.setLayerType(NetworkTopology.DenseLayer);
        topologyList.add(dense2);

        NetworkTopology dense3 = new NetworkTopology();
        dense3.setInputSize(60);
        dense3.setOutputSize(40);
        dense3.setLayerType(NetworkTopology.DenseLayer);
        topologyList.add(dense3);

        NetworkTopology dense4 = new NetworkTopology();
        dense4.setInputSize(40);
        dense4.setOutputSize(2);
        dense4.setLayerType(NetworkTopology.OutputLayer);
        topologyList.add(dense4);

        TopologyReader topologyReader = new TopologyReader(topologyList);
        topologyReader.WriteTopologyParameters(saveFile);
    }

    public static void main(String[] args) {
        TopologyReader topologyReader = new TopologyReader(new File("E:\\DeepRIPTest\\CNN\\NetworkTopology.json"));
        topologyReader.getTopologyList();
    }
}
