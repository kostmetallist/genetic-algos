import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
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

        double distX = this.x - another.x;
        double distY = this.y - another.y;

        return Math.sqrt(distX*distX + distY*distY);
    }

    @Override
    public String toString() {
        return new String(this.id + ", " + this.x + ", " + this.y);
    }
}


class SearchableSet extends HashSet<Byte> {

    private static Random rand = new Random();


    public SearchableSet() {
        super();
    }

    public Byte searchFor(Byte value) {

        for (Byte each : this) {
            if (each.byteValue() == value.byteValue())
                return each;
        }

        return null;
    }

    public Byte getRandomElement() {

        byte randIndex = (byte) rand.nextInt(this.size());
        byte i = 0;
        Byte extracted = null;

        for (Byte each : this) {

            if (i == randIndex) {
                extracted = each;
                break;
            }   

            i++;
        }

        return extracted;
    }
}


// neighbour representation of towns in list
class Voyage extends Organism {

    private static Random rand = new Random();
    private static List<Town> townsList = null;


    // `neighbourIndices` is an array of different `townsList` indices.
    // It must contain all possible indices of `townsList` and thus
    // be the same length as that list.
    public Voyage(byte[] neighbourIndices) {

        // TODO validation

        super(neighbourIndices);
    }

    public Voyage(Voyage another) {
        super(another);
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

    // calculates summary of all distances between towns in voyage
    public double getTotalDistance() {

        byte[] traversal = this.getTownsTraversal();
        double total = .0;
        // this cycle won't go if traversal.length <= 1
        for (byte i = 1; i < traversal.length; i++) {
            total += townsList.get(i-1).distTo(townsList.get(i));
        }

        // Looping distance.
        // But in this case need to check for 1+ towns in voyage
        if (traversal.length > 1) {
            total += 
                townsList.get(traversal[traversal.length-1]).
                distTo(townsList.get(0));
        }

        return total;
    }

    // Travelling salesman problem solution is the shortest possible cycle.
    // Hense, fitness function should in inverse ratio to voyage distance.
    public double calculateFitnessValue(double numerator) {
        return numerator / this.getTotalDistance();
    }


    // gets byte[] representing towns traversal & encodes to neighbour indices
    public static byte[] encode(byte[] traversal) {

        // System.out.println("traversal:");
        // for (byte each : traversal) {
        //     System.out.print(each + " ");
        // }
        // System.out.println();

        byte[] indices = new byte[traversal.length];

        if (traversal.length == 0) {
            return indices;
        }

        for (byte i = 1; i < indices.length; i++) {
            indices[traversal[i-1]] = traversal[i];
        }

        indices[traversal[traversal.length-1]] = traversal[0];

        // System.out.println("encoded:");
        // for (byte each : indices) {
        //     System.out.print(each + " ");
        // }
        // System.out.println();
        return indices;
    }

    public void printoutTownsTraversal() {

        byte[] traversal = this.getTownsTraversal();
        for (byte i = 0; i < traversal.length; i++) {
            System.out.println(townsList.get(traversal[i]));
        }
    }

    @Override 
    public Voyage[] crossWith(Organism another) {

        final byte[] genome1 = this.getGenome();
        final byte[] genome2 = another.getGenome();

        byte rootTownNum = (byte) rand.nextInt(genome1.length);
        // taking in count `rootTownNum` as a first
        byte currentLength = 1;

        byte[] newTraversal = new byte[genome1.length];
        //Set<Byte> availableTownNums = new HashSet<>();
        SearchableSet availableTownNums = new SearchableSet();
        for (byte i = 0; i < genome1.length; i++) {
            availableTownNums.add(i);
        }

        newTraversal[0] = rootTownNum;
        if (!availableTownNums.remove(availableTownNums.
            searchFor(rootTownNum))) { 

            System.err.println("not removed " + rootTownNum); 
        }

        while (currentLength < genome1.length) {

            byte candidate1 = genome1[rootTownNum];
            byte candidate2 = genome2[rootTownNum];

            // both candidates are already in `newTraversal`, so 
            // to avoid replication we need to choose random next town
            // from the `availableTownNums` pool
            if (availableTownNums.searchFor(candidate1) == null &&
                availableTownNums.searchFor(candidate2) == null) {

                Byte townToRemove = availableTownNums.getRandomElement();
                newTraversal[currentLength] = townToRemove;
                availableTownNums.remove(townToRemove);
                rootTownNum = townToRemove;
            }

            else {

                if (availableTownNums.searchFor(candidate1) == null) {

                    newTraversal[currentLength] = candidate2;
                    availableTownNums.remove(availableTownNums.
                        searchFor(candidate2));
                    rootTownNum = candidate2;
                }

                else if (availableTownNums.searchFor(candidate2) == null) {

                    newTraversal[currentLength] = candidate1;
                    availableTownNums.remove(availableTownNums.
                        searchFor(candidate1));
                    rootTownNum = candidate1;
                }

                // if both candidates are available to append, genetic 
                // algorithm must choose the most strong alternative => 
                // in this case the nearest town (i.e. the least distance)
                else {

                    double dist1 = 
                        townsList.get(rootTownNum).
                        distTo(townsList.get(candidate1));
                    double dist2 = 
                        townsList.get(rootTownNum).
                        distTo(townsList.get(candidate2));

                    byte chosenOne = (dist1 > dist2)? candidate2: candidate1;
                    newTraversal[currentLength] = chosenOne;
                    availableTownNums.remove(availableTownNums.
                        searchFor(chosenOne));
                    rootTownNum = chosenOne;
                }
            }

            currentLength++;
        }

        Voyage offspring1 = new Voyage(encode(newTraversal));
        Voyage offspring2;

        // taking stronger parent as a second offspring
        if (this.getFitnessValue() > another.getFitnessValue()) {
            offspring2 = new Voyage(this);
        }

        // in this case can't use copy constructor, because conversion 
        // `Organism` -> `Voyage` is impossible
        else {

            byte[] parentGenome = new byte[genome2.length];
            for (byte i = 0; i < genome2.length; i++) {
                parentGenome[i] = genome2[i];
            }

            offspring2 = new Voyage(parentGenome);
        }

        Voyage[] offsprings = {offspring1, offspring2};
        return offsprings;
    }

    @Override
    public Voyage mutateGens(List<Integer> genIndices) {

        // System.out.println("before mutation:");
        // System.out.println(this);
        // encode(this.getTownsTraversal());


        // ignoring `genIndices` in this implementation
        Voyage mutant = new Voyage(this);
        byte[] mutantGenome = mutant.getGenome();

        byte first = (byte) rand.nextInt(mutantGenome.length);
        byte second = (byte) rand.nextInt(mutantGenome.length);
        byte temp = mutantGenome[first];

        mutantGenome[first] = mutantGenome[second];
        mutantGenome[second] = temp;

        if (mutant.isProper()) {
            return mutant;
        }

        byte[] traversal = mutant.getTownsTraversal();
        SearchableSet availableIndices = new SearchableSet();
        for (byte i = 0; i < traversal.length; i++) {
            availableIndices.add(i);
        }

        byte faultPosition = -1;
        for (byte i = 0; i < traversal.length; i++) {

            Byte searchResult = availableIndices.searchFor(traversal[i]);

            if (searchResult == null) {
                faultPosition = i;
                break;
            }

            else {
                availableIndices.remove(searchResult);
            }
        }

        while (!availableIndices.isEmpty()) {

            Byte randomTown = availableIndices.getRandomElement();
            traversal[faultPosition] = randomTown;
            availableIndices.remove(randomTown);
            faultPosition++;
        }

        byte[] fixedMutantGenome = encode(traversal);
        mutant.setGenome(fixedMutantGenome);
        return mutant;
    }
}


public class TspTask {

    // `path` specifies a town database file, which is in following format:
    // <integer: town_id> <float: town_x> <float: town_y>
    private static List<Town> readTownsFile(String path) {

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

    private static List<Voyage> generateRandomVoyages(byte townNum, 
        int outputSize) {

        List<Voyage> voyages = new LinkedList<>();
        for (int i = 0; i < outputSize; i++) {

            byte[] traversal = new byte[townNum];
            SearchableSet availableIndices = new SearchableSet();
            for (byte n = 0; n < townNum; n++) {
                availableIndices.add(n);
            }

            byte j = 0;
            while (!availableIndices.isEmpty()) {

                Byte extracted = availableIndices.getRandomElement();
                traversal[j++] = extracted;
                availableIndices.remove(extracted);
            }

            voyages.add(new Voyage(Voyage.encode(traversal)));
        }

        return voyages;
    }

    private static void fillVoyagesFitnessValues(List<Voyage> voyages) {

        voyages.forEach(voyage -> 
            voyage.setFitnessValue(voyage.calculateFitnessValue(100)));
    }

    private static void showMaxMin(List<Voyage> voyages) {

        if (voyages.isEmpty()) {
            return;
        }

        double max = voyages.get(0).getFitnessValue(), 
            min = max;

        for (Voyage each : voyages) {

            double currentValue = each.getFitnessValue();
            if (currentValue < min) {
                min = currentValue;
            }
            if (currentValue > max) {
                max = currentValue;
            }
        }

        System.out.println(min + " " + max);
    }

    public static void main(String[] args) {

        List<Town> townsList = readTownsFile("data/towns.txt");
        Voyage.setTownsList(townsList);
        // Voyage.showTowns();

        // byte[] arr1 = {1, 4, 3, 5, 2, 0};
        // Voyage v1 = new Voyage(arr1);
        // byte[] decoded1 = v1.getTownsTraversal();
        // byte[] arr2 = {4, 0, 1, 5, 3, 2};
        // Voyage v2 = new Voyage(arr2);
        // byte[] decoded2 = v2.getTownsTraversal();
        // Voyage[] children = v1.crossWith(v2);
        // System.out.println(children[0]);
        // System.out.println(v1.mutateGens(new ArrayList<>()));

        System.out.println("/// SOLUTION \\\\\\");
        byte[] solTraversal = {
            0 , 32, 62, 15, 2 , 43, 31, 8 , 38, 71, 57, 9 , 30, 54, 24, 
            49, 17, 23, 48, 22, 55, 40, 42, 41, 63, 21, 60, 20, 46, 35, 
            68, 70, 59, 69, 19, 36, 4,  14, 56, 12, 53, 18, 13, 58, 65, 
            64, 37, 10, 52, 6,  34, 7,  45, 33, 51, 26, 44, 28, 47, 29, 
            3,  74, 75, 66, 25, 11, 39, 16, 50, 5,  67, 1,  73, 27, 61, 
            72};
        Voyage solution = new Voyage(Voyage.encode(solTraversal));
        System.out.println(solution.getTotalDistance());
        System.out.println("/// SOLUTION \\\\\\");


        List<Voyage> voyages = 
            generateRandomVoyages((byte) townsList.size(), 20);

        //voyages.forEach(System.out::println);

        Genetic geneticProcess = new Genetic(townsList.size());
        Genetic.Parameters params = geneticProcess.new Parameters();
        params.setCrossingoverProb(0.7);
        params.setMutationProb(0.3);
        geneticProcess.setParameters(params);

        fillVoyagesFitnessValues(voyages);
        List<Organism> organisms = new ArrayList<>(voyages);
        geneticProcess.setCurOrganisms(organisms);
        //showMaxMin(voyages);

        final int iterationsNum = 30;
        for (int i = 0; i < iterationsNum; i++) {

            geneticProcess.doGenerationStep();
            List<Organism> newOrganisms = geneticProcess.getCurOrganisms();
            List<Voyage> newVoyages = new ArrayList<>();

            for (Organism org : newOrganisms) {
                newVoyages.add(new Voyage(org.getGenome()));
            }

            fillVoyagesFitnessValues(newVoyages);

            for (int j = 0; j < newOrganisms.size(); j++) {
                newOrganisms.get(j).setFitnessValue(
                    newVoyages.get(j).getFitnessValue());
            }

            System.out.println("--------------------------------------");
            showMaxMin(newVoyages);
            
            Voyage best = newVoyages.get(0);

            for (Voyage v : newVoyages) {
                if (v.getFitnessValue() > best.getFitnessValue()) {
                    best = v;
                }
            }

            System.out.println(best.getTotalDistance());
            if (i == iterationsNum - 1)
                best.printoutTownsTraversal();
        }
    }
}
