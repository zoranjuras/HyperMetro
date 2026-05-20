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

        Map<String, DoublyLinkedList<Station>> mapOfLines = getMapOfLines(filePath);

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

    private static boolean executeCommand(List<String> tokens, Map<String, DoublyLinkedList<Station>> mapOfLines) {

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

        DoublyLinkedList<Station> stations = mapOfLines.get(line);

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

            case ("/append") -> stations.addLast(new Station(station));

            case ("/add-head") -> stations.addFirst(new Station(station));

            case ("/remove") -> {
                Station stationToRemove = stations.find(s -> Objects.equals(s.getName(), station));
                if (stationToRemove != null) {
                    stations.remove(stationToRemove);
                }
            }

            case ("/route") -> {
                if (tokens.size() != 5) {
                    System.out.println("Invalid command");
                    return true;
                }

                String line1 = tokens.get(1);
                String line2 = tokens.get(3);
                String stationName1 = tokens.get(2);
                String stationName2 = tokens.get(4);

                StationRef start = new StationRef(line1, stationName1);
                StationRef target = new StationRef(line2, stationName2);

                List<StationRef> path = findRoute(start, target, mapOfLines);

                if (path.isEmpty()) {
                    return true;
                }

                printRoute(path);

            }

            case ("/connect") -> {
                if (tokens.size() != 5) {
                    System.out.println("Invalid command");
                    return true;
                }

                String line1 = tokens.get(1);
                String line2 = tokens.get(3);
                String stationName1 = tokens.get(2);
                String stationName2 = tokens.get(4);

                DoublyLinkedList<Station> stations1 = mapOfLines.get(line1);
                DoublyLinkedList<Station> stations2 = mapOfLines.get(line2);

                Station connection1 = stations1.find(s -> Objects.equals(s.getName(), stationName1));
                Station connection2 = stations2.find(s -> Objects.equals(s.getName(), stationName2));

                if (connection1 != null && connection2 != null) {
                    connection1.addTransfer(line2, stationName2);
                    connection2.addTransfer(line1, stationName1);
                }
            }

            case ("/output") -> printStations(line, mapOfLines);

            default -> System.out.println("Invalid command");

        }

        return true;

    }

    private static List<StationRef> getNeighbours(StationRef current, Map<String, DoublyLinkedList<Station>> mapOfLines) {
        DoublyLinkedList<Station> stations = mapOfLines.get(current.line());
        List<StationRef> neighbours = new ArrayList<>();

        int index = findStationPosition(current, mapOfLines);

        if (index >= 0) {
            Station station = stations.get(index);

            for (Station.Transfer transfer : station.getTransfers()) {
                neighbours.add(new StationRef(transfer.getLine(), transfer.getStation()));

            }

            if (index > 0) {
                Station prevStation = stations.get(index - 1);
                neighbours.add(new StationRef(current.line(), prevStation.getName()));

            }

            if (index < stations.size() - 1) {
                Station nextStation = stations.get(index + 1);
                neighbours.add(new StationRef(current.line(), nextStation.getName()));

            }
        }

        return neighbours;
    }

    private static int findStationPosition(StationRef ref, Map<String, DoublyLinkedList<Station>> mapOfLines) {
        String lineName = ref.line();
        String stationName = ref.station();
        int index = 0;

        DoublyLinkedList<Station> stations = mapOfLines.get(lineName);

        for (Station station : stations) {
            if (Objects.equals(stationName, station.getName())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static List<StationRef> findRoute(StationRef start, StationRef target, Map<String, DoublyLinkedList<Station>> mapOfLines) {
        Queue<StationRef> queue = new LinkedList<>();
        Set<StationRef> visited = new HashSet<>();
        Map<StationRef, StationRef> parent = new HashMap<>();

        queue.add(start);
        visited.add(start);
        StationRef current = start;

        while (!queue.isEmpty()) {
            current = queue.poll();

            if (current.equals(target)) {
                break;
            }

            for (StationRef neighbour : getNeighbours(current, mapOfLines)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    parent.put(neighbour, current);
                    queue.add(neighbour);
                }
            }
        }

        if (queue.isEmpty()) {
            return List.of();
        } else {
            List<StationRef> path = new ArrayList<>();
            current = target;
            while (current != null) {
                path.add(current);
                current = parent.get(current);
            }

            return (path.reversed());

        }

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

    public static Map<String, DoublyLinkedList<Station>> getMapOfLines(String filePath) throws IOException {
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
        Type rawType = new TypeToken<Map<String, Map<String, Station>>>() {}.getType();
        Map<String, Map<String, Station>> raw = gson.fromJson(json, rawType);

        Map<String, DoublyLinkedList<Station>> mapOfLines = new HashMap<>();

        for (Map.Entry<String, Map<String, Station>> entry : raw.entrySet()) {
            DoublyLinkedList<Station> stations = new DoublyLinkedList<>();
            // sort numeric keys so stations are added in correct order
            TreeMap<String, Station> sorted = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
            sorted.putAll(entry.getValue());
            sorted.forEach((k, v) -> stations.addLast(v));
            mapOfLines.put(entry.getKey(), stations);
        }

        return mapOfLines;

    }

    private static void printRoute(List<StationRef> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            StationRef current = path.get(i);
            StationRef next = path.get(i + 1);
            System.out.println(current.station());
            if (!Objects.equals(current.line(), next.line())) {
                System.out.println("Transition to line " + next.line());
            }
        }
        System.out.println(path.getLast().station());
    }

    private static void printStations(String lineName, Map<String, DoublyLinkedList<Station>> mapOfLines) {

        DoublyLinkedList<Station> stations = mapOfLines.get(lineName);

        if (stations == null) {
            System.out.println("No such line: " + lineName);
            return;
        }

        System.out.println("depot");

        for (Station station : stations) {
            System.out.print(station.getName());

            for (Station.Transfer transfer : station.getTransfers()) {
                System.out.print(" - " + transfer.getStation() + " (" + transfer.getLine() + " line)");
            }

            System.out.println();
        }

        System.out.println("depot");
    }
}
