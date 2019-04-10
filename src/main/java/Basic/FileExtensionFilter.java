package Basic;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

/**
 * Created by Ben on 2018/5/11.
 */
public class FileExtensionFilter implements FileFilter {
    private HashSet<String> extensionSet;

    //file extension can be separated by comma
    public FileExtensionFilter(String extensions) {
        String[] extensionArr = extensions.split(",");
        extensionSet = new HashSet<>();
        for(int i=0; i<extensionArr.length; i++) {
            extensionArr[i] = extensionArr[i].trim();
            extensionSet.add(extensionArr[i].toLowerCase());
        }
    }

    @Override
    public boolean accept(File pathname) {
        String fileName = pathname.getName();
        int index = fileName.lastIndexOf(".");
        String extension = fileName.substring(index + 1, fileName.length());
        if(extensionSet.contains(extension.toLowerCase()))
            return true;
        else
            return false;
    }

    public static void main(String[] args) {
        FileExtensionFilter fileExtensionFilter = new FileExtensionFilter("json");
        fileExtensionFilter.accept(new File("E:\\DeepRIPTest\\CNN\\NetworkOptimization\\FilterNum\\Filter5.Json"));
    }
}
