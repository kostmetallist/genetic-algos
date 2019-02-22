import java.util.List;
import java.util.ArrayList;
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

    private static List<Town> townsList;

    public Voyage(byte[] neighbourList) {
        super(neighbourList);
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
        byte[] decoded = v1.getTownsTraversal();

        for (int i = 0; i < 9; i++) {
            System.out.print(String.format("%d ", decoded[i]));
        }
        System.out.println();
    }
}