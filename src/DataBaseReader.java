import java.io.*;
import java.util.*;

public class DataBaseReader {
    private String fileName;
    private BufferedReader reader;

    public DataBaseReader(String fileName) {
        this.fileName = fileName;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileName)));
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

    public Map<String, List<String>> readAllFile(String splitSign) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        while (reader.ready()) {
            List<String> value = new LinkedList<>(Arrays.asList(reader.readLine().toLowerCase().split(splitSign)));
            String key = value.get(0);
            value.remove(0);
            Collections.sort(value);
            result.put(key, value);
        }
        return result;
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
