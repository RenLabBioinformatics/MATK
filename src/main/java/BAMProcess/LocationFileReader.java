package BAMProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Ben on 2018/4/23.
 */
public class LocationFileReader {
    private LinkedList<LocationRecord> locationList;

    public LocationFileReader(File locationFile) {
        try {
            locationList = new LinkedList<LocationRecord>();
            //Read data
            BufferedReader br = new BufferedReader(new FileReader(locationFile));
            String strLine;
            String[] strArr;
            while (br.ready()) {
                strLine = br.readLine();
                strArr = strLine.split("\t");
                LocationRecord locRec = new LocationRecord(strArr[0], Integer.parseInt(strArr[1]));
                locationList.add(locRec);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<LocationRecord> getLocationList() {
        return locationList;
    }
}
