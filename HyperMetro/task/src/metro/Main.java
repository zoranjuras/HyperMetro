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

//        String filePath = "HyperMetro/task/src/metro/london.json";
        String filePath = args[0];

        Map<String, DoublyLinkedList<Station>> mapOfLines = getMapOfLines(filePath);

        MinBinaryHeap<RouteNode> heap = new MinBinaryHeap<>(Comparator.comparingInt(RouteNode::distance));

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

            case ("/fastest-route") -> {
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

                RouteResult result = findFastestRoute(start, target, mapOfLines);

                if (result.path().isEmpty()) {
                    return true;
                }

                printFastestRoute(result);

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

    private static RouteResult findFastestRoute(StationRef start, StationRef target, Map<String, DoublyLinkedList<Station>> mapOfLines) {

        Map<StationRef, Integer> distances = new HashMap<>();
        Map<StationRef, StationRef> parent = new HashMap<>();
        Set<StationRef> visited = new HashSet<>();

        MinBinaryHeap<RouteNode> heap = new MinBinaryHeap<>(Comparator.comparingInt(RouteNode::distance));

        distances.put(start, 0);
        heap.add(new RouteNode(start, 0));
        StationRef current;

        while (!heap.isEmpty()) {

            RouteNode currentNode = heap.poll();
            current = currentNode.station();

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            if (current.equals(target)) {
                break;
            }

            for (WeightedNeighbour neighbour : getWeightedNeighbours(current, mapOfLines)) {
                StationRef next = neighbour.station();
                int newDistance = distances.get(current) + neighbour.time();

                if (newDistance < distances.getOrDefault(next, Integer.MAX_VALUE)) {
                    distances.put(next, newDistance);
                    parent.put(next, current);
                    heap.add(new RouteNode(next, newDistance));
                }
            }

        }

        if (!distances.containsKey(target)) {
            return new RouteResult(List.of(), 0);
        }

        List<StationRef> path = new ArrayList<>();
        current = target;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        return new RouteResult(
                path.reversed(),
                distances.get(target)
        );

    }

    private static Station findStation(StationRef ref, Map<String, DoublyLinkedList<Station>> mapOfLines) {

        String lineName = ref.line();
        String stationName = ref.station();

        DoublyLinkedList<Station> stations = mapOfLines.get(lineName);

        return stations.find(s -> Objects.equals(stationName, s.getName()));

    }

    private static List<WeightedNeighbour> getWeightedNeighbours(
            StationRef current,
            Map<String, DoublyLinkedList<Station>> mapOfLines
    ) {
        List<WeightedNeighbour> weightedNeighbours = new ArrayList<>();
        String line = current.line();
        Station station = findStation(current, mapOfLines);

        if (station == null) {
            return weightedNeighbours;
        }

        // Transfer between lines has fixed cost
        for (Station.Connection transfer : station.getTransfers()) {
            weightedNeighbours.add(
                    new WeightedNeighbour(
                            new StationRef(transfer.getLine(), transfer.getStation()),
                            5
                    )
            );
        }

        // current -> prev uses prev station's "time" (prev -> current segment)
        for (String prevName : station.getPrev()) {
            Station prevStation = findStation(new StationRef(line, prevName), mapOfLines);
            if (prevStation != null) {
                weightedNeighbours.add(
                        new WeightedNeighbour(
                                new StationRef(line, prevName),
                                prevStation.getTime()
                        )
                );
            }
        }

        // current -> next uses current station's "time" (current -> next segment)
        for (String nextName : station.getNext()) {
            weightedNeighbours.add(
                    new WeightedNeighbour(
                            new StationRef(line, nextName),
                            station.getTime()
                    )
            );
        }

        return weightedNeighbours;
    }

    private static List<StationRef> getNeighbours(StationRef current, Map<String, DoublyLinkedList<Station>> mapOfLines) {
        List<StationRef> neighbours = new ArrayList<>();

        Station station = findStation(current, mapOfLines);

        if (station == null) {
            return neighbours;
        }

        for (Station.Connection transfer : station.getTransfers()) {
            neighbours.add(new StationRef(transfer.getLine(), transfer.getStation()));

        }

        for (String prevName : station.getPrev()) {
            neighbours.add(new StationRef(current.line(), prevName));
        }

        for (String nextName : station.getNext()) {
            neighbours.add(new StationRef(current.line(), nextName));
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

    private static List<StationRef> findRoute(
            StationRef start,
            StationRef target,
            Map<String, DoublyLinkedList<Station>> mapOfLines
    ) {
        Queue<StationRef> queue = new LinkedList<>();
        Set<StationRef> visited = new HashSet<>();
        Map<StationRef, StationRef> parent = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            StationRef current = queue.poll();

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

        if (!visited.contains(target)) {
            return List.of();
        }

        List<StationRef> path = new ArrayList<>();
        StationRef current = target;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        return path.reversed();
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
        Type rawType = new TypeToken<Map<String, List<Station>>>() {}.getType();
        Map<String, List<Station>> raw = gson.fromJson(json, rawType);

        Map<String, DoublyLinkedList<Station>> mapOfLines = new HashMap<>();

        for (Map.Entry<String, List<Station>> entry : raw.entrySet()) {
            DoublyLinkedList<Station> stations = new DoublyLinkedList<>();
            for (Station s : entry.getValue()) {
                stations.addLast(s);
            }
            mapOfLines.put(entry.getKey(), stations);
        }

        return mapOfLines;

    }

    private static void printFastestRoute(RouteResult result) {

        List<StationRef> path = result.path();

        for (int i = 0; i < path.size() - 1; i++) {
            StationRef current = path.get(i);
            StationRef next = path.get(i + 1);
            System.out.println(current.station());
            if (!Objects.equals(current.line(), next.line())) {
                System.out.println("Transition to line " + next.line());
            }
        }
        System.out.println(path.getLast().station());
        System.out.println("Total: " + result.totalTime() + " minutes in the way");
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

            for (Station.Connection transfer : station.getTransfers()) {
                System.out.print(" - " + transfer.getStation() + " (" + transfer.getLine() + " line)");
            }

            System.out.println();
        }

        System.out.println("depot");
    }
}
