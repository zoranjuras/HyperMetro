package metro;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Station {
    private String name;
    private int time;
    @SerializedName("transfer")
    private List<Connection> transfers = new ArrayList<>();

    @SerializedName("next")
    private List<String> next = new ArrayList<>();

    @SerializedName("prev")
    private List<String> prev = new ArrayList<>();

    public Station(String name) {
        this.name = name;
    }

    public Station() {
    }

    static class Connection {
        String line;
        String station;

        Connection(String line, String station) {
            this.line = line;
            this.station = station;
        }

        public String getLine() {
            return line;
        }

        public String getStation() {
            return station;
        }
    }

    public void addTransfer(String line, String station) {
        transfers.add(new Connection(line, station));
    }

    public String getName() {
        return name;
    }

    public List<Connection> getTransfers() {
        return transfers;
    }

    public int getTime() {
        return time;
    }

    public List<String> getNext() {
        return next;
    }

    public List<String> getPrev() {
        return prev;
    }
}
