package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class HistoryFile {
    private static File file;

    /**
     * Create new file "history_login.txt" if it is not exist
     * @param login - the login of the authorizing user
     * @throws IOException
     */
    public static void createHistoryFile(String login) throws IOException {
        file = new File("historyUser/history_" + login + ".txt");
        file.createNewFile();
    }

    /**
     * Reading the history and output one last hundred messages
     * @return - One last hundred messages
     * @throws IOException
     */
    public static String readHistory() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        List<String> list = Files.readAllLines(file.getAbsoluteFile().toPath());

        for (int i = list.size() - 100; i < list.size(); i++) {
            stringBuffer.append(list.get(i) + "\n");
        }

        return stringBuffer.toString();
    }

    /**
     * Recording messages to history file
     * @return - thread of write
     * @throws IOException
     */
    public static FileWriter writeHistory() throws IOException {
        FileWriter writer = new FileWriter(file.getCanonicalFile(), true);
        return writer;
    }
}
