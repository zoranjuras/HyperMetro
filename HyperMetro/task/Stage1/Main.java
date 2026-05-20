package metro;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {

        File file = new File(args[0]);
//        File file = new File("HyperMetro/task/src/metro/file.txt");

        if (!file.exists()) {
            System.out.println("Error! Such a file doesn't exist!");
            return;
        }

        if (file.length() == 0) {
            return;
        }

        DoublyLinkedList<String> stations = new DoublyLinkedList<>();

        stations.addFirst("depot");

        for (String line : Files.readAllLines(file.toPath())) {
            stations.addLast(line.trim());
        }

        stations.addLast("depot");

        boolean first = true;

        for (int i = 0; i < stations.size() - 2; i++) {
            for (int j = i; j < (i + 3); j++) {
                if (!first) {
                    System.out.print(" - ");
                }
                System.out.print(stations.get(j));
                first = false;
            }
            System.out.println();
            first = true;
        }

    }
}
