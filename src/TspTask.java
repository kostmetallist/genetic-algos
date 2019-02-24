import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


class Town {

    public double x;
    public double y;
    public int id;

    public Town(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distTo(Town another) {
        return Math.sqrt(this.x*another.x + this.y*another.y);
    }

    @Override
    public String toString() {
        return String.format("town#%d(%.2f, %.2f)", this.id, this.x, this.y);
    }
}

// neighbour representation of towns in list
class Voyage extends Organism {

    private static List<Town> townsList = null;


    // `neighbourIndices` is an array of different `townsList` indices.
    // It must contain all possible indices of `townsList` and thus
    // be the same length as that list.
    public Voyage(byte[] neighbourIndices) {

        // `indicesEntries` contains [0, n-1] where n is the `townsList` size
        // List<Byte> indicesEntries = new ArrayList<>();
        // for (byte i = 0; i < townsList.size(); i++) {
        //     indicesEntries.add(i);
        // }

        // for (byte i = 0; i < neighbourIndices.length; i++) {
        //     if (!indicesEntries.remove(neighbourIndices[i])) {
        //         System.err.println("alarm");
        //     }
        // }

        // TODO validation

        super(neighbourIndices);
    }

    public static void setTownsList(List<Town> tl) {
        townsList = tl;
    }

    public static void showTowns() {
        townsList.forEach(System.out::println);
    }

    // Returns decoded neighbourList (`genome`).
    // `traversal` represents towns' indices.
    public byte[] getTownsTraversal() {

        byte[] genome = this.getGenome();
        byte[] traversal = new byte[genome.length];
        // we assume any traversal is Hamilton Cycle, so 
        // first element may be any, if cyclic transform is performed
        traversal[0] = 0;
        byte index = 0;
        int i = 1;

        while (genome[index] != 0) {
            traversal[i] = genome[index];
            index = genome[index];
            i++;
        }

        return traversal;
    }

    // checks whether this instance represents hamilton cycle
    public boolean isProper() {

        byte[] traversal = this.getTownsTraversal();
        Map<Byte, Boolean> travExpectedContents = new HashMap<>();
        for (byte i = 0; i < traversal.length; i++) {
            travExpectedContents.put(i, true);
        }

        for (byte i = 0; i < traversal.length; i++) {

            if (travExpectedContents.get(traversal[i])) {
                travExpectedContents.put(traversal[i], false);
            }

            else {
                return false;
            }
        }

        return true;
    }
}

public class TspTask {

    // `path` specifies a town database file, which is in following format:
    // <integer: town_id> <float: town_x> <float: town_y>
    public static List<Town> readTownsFile(String path) {

        List<Town> townsList = new ArrayList<>();
        BufferedReader reader;
        try {

            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            int lineCounter = 1;
            while (line != null) {

                if (Pattern.matches("^\\d+\\s\\d+(\\.\\d+)?\\s\\d+(\\.\\d+)?$", 
                    line)) {

                    String[] splitParts = line.split("\\s+");
                    double townX = Double.parseDouble(splitParts[1]);
                    double townY = Double.parseDouble(splitParts[2]);
                    Town town = new Town(townX, townY);
                    town.id = Integer.parseInt(splitParts[0]);
                    townsList.add(town);
                }

                else {
                    System.err.println("W: line " + lineCounter + 
                        " is not matched a pattern");
                }

                line = reader.readLine();
                lineCounter++;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return townsList;
    }

    public static void main(String[] args) {

        List<Town> townsList = readTownsFile("data/towns.txt");
        Voyage.setTownsList(townsList);
        // Voyage.showTowns();
        byte[] arr1 = {1, 3, 7, 2, 8, 6, 0, 4, 5};
        Voyage v1 = new Voyage(arr1);
        //byte[] decoded = v1.getTownsTraversal();
        byte[] arr2 = {1, 3, 7, 0, 8, 2, 4, 6, 5};
        Voyage v2 = new Voyage(arr2);

        System.out.println(v1.isProper());
        System.out.println(v2.isProper());

        // for (int i = 0; i < 9; i++) {
        //     System.out.print(String.format("%d ", decoded[i]));
        // }
        System.out.println();
    }
}
