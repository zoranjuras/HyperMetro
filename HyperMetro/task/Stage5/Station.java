package metro;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Station {
    private String name;
    @SerializedName("transfer")
    private List<Transfer> transfers = new ArrayList<>();
    private int time;

    public Station(String name) {
        this.name = name;
    }

    public Station() {
    }

    static class Transfer {
        String line;
        String station;

        Transfer(String line, String station) {
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
        transfers.add(new Transfer(line, station));
    }

    public String getName() {
        return name;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public int getTime() {
        return time;
    }
}
