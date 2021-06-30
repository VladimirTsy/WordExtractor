import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Write {

    public static void writeToFile(StringBuilder sb, String pathToWrite) throws IOException {
        FileWriter fw = new FileWriter(pathToWrite);
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bw.close();
        }
    }
}
