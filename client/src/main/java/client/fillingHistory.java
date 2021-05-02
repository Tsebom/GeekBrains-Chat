package client;

import java.io.FileWriter;
import java.io.IOException;

public class fillingHistory {
    public static void main(String[] args) throws IOException {
        FileWriter writer = new FileWriter("historyUser/history_asd.txt");
        for (int i = 1; i <= 110; i++) {
            writer.write("" + i + ". bla bla bla\n");
        }
        writer.close();
    }
}
