import java.io.*;
import java.util.*;

public class DataBaseReader {
    private String fileName;
    private BufferedReader reader;
    private BufferedWriter writer;

    public DataBaseReader(String fileName) {
        this.fileName = fileName;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileName)));
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(fileName)));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, List<String>> readAllFile() throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        while (reader.ready()) {
            List<String> value = Arrays.asList(reader.readLine().toLowerCase().split(","));
            String key = value.get(0);
            value.remove(0);
            Collections.sort(value);
            result.put(key, value);
        }
        return result;
    }
}
