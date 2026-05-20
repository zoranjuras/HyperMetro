package metro;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

//        String filePath = "HyperMetro/task/src/metro/metro_map.json";
        String filePath = args[0];

        Map<String, DoublyLinkedList<String>> mapOfLines = getMapOfLines(filePath);

        if (mapOfLines == null) {
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {

            String input = scanner.nextLine();

            List<String> tokens = parseCommand(input);

            if(!executeCommand(tokens, mapOfLines)) {
                break;
            }
        }
    }

    private static boolean executeCommand(List<String> tokens, Map<String, DoublyLinkedList<String>> mapOfLines) {

        if (tokens.isEmpty()) {
            System.out.println("Invalid command");
            return true;
        }

        String command = tokens.get(0);

        if (command.equals("/exit")) {
            return false;
        }

        String line = tokens.size() > 1 ? tokens.get(1) : null;

        if (line == null) {
            System.out.println("Invalid command");
            return true;
        }

        DoublyLinkedList<String> stations = mapOfLines.get(line);

        if (stations == null) {
            System.out.println("Invalid command");
            return true;
        }

        String station = tokens.size() > 2 ? tokens.get(2) : null;

        if (!command.equals("/output") && station == null) {
            System.out.println("Invalid command");
            return true;
        }

        switch (command) {

            case ("/append") -> stations.addLast(station);

            case ("/add-head") -> stations.addFirst(station);

            case ("/remove") -> stations.remove(station);

            case ("/output") -> printStations(line, mapOfLines);

            default -> System.out.println("Invalid command");

        }

        return true;

    }

    private static List<String> parseCommand(String input) {

        boolean insideQuotes = false;
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (char c : input.trim().toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ' ' && !insideQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }

        }

        if (insideQuotes) {
            return List.of();
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        
        return tokens;
    }

    public static Map<String, DoublyLinkedList<String>> getMapOfLines(String filePath) throws IOException {
//        File file = new File(args[0]);
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Error! Such a file doesn't exist!");
            return null;
        }

        if (file.length() == 0) {
            return null;
        }

        String json = new String(Files.readAllBytes(file.toPath()));

        Gson gson = new Gson();
        Type rawType = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        Map<String, Map<String, String>> raw = gson.fromJson(json, rawType);

        Map<String, DoublyLinkedList<String>> mapOfLines = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> entry : raw.entrySet()) {
            DoublyLinkedList<String> stations = new DoublyLinkedList<>();
            // sort numeric keys so stations are added in correct order
            TreeMap<String, String> sorted = new TreeMap<String, String>(Comparator.comparingInt(Integer::parseInt));
            sorted.putAll(entry.getValue());
            sorted.forEach((k, v) -> stations.addLast(v));
            mapOfLines.put(entry.getKey(), stations);
        }

        return mapOfLines;

    }

    public static void printStations(String lineName, Map<String, DoublyLinkedList<String>> mapOfLines) {

        DoublyLinkedList<String> stations = mapOfLines.get(lineName);

        if (stations == null) {
            System.out.println("No such line: " + lineName);
            return;
        }

        for (int i = 0; i < stations.size(); i++) {
            String previous, current, next;

            previous = (i == 0) ? "depot" : stations.get(i - 1);

            current = stations.get(i);

            next = (i == stations.size() - 1) ? "depot" : stations.get(i + 1);

            System.out.println(previous + " - " + current + " - " + next);
        }
    }
}
