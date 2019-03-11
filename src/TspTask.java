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


class SearchableSet extends HashSet<Number> {

    private static Random rand = new Random();


    public SearchableSet() {
        super();
    }

    public Number searchFor(Number value) {

        if (value == null) {
            return null;
        }

        for (Number each : this) {
            if (each.intValue() == value.intValue())
                return each;
        }

        return null;
    }

    public Number getRandomElement() {

        int randIndex = rand.nextInt(this.size());
        int i = 0;
        Number extracted = null;

        for (Number each : this) {

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
    public Voyage(Number[] neighbourIndices) {

        this.genome = neighbourIndices;
    }

    public Voyage(Voyage another) {
        this.genome = another.genome.clone();
    }

    public static void setTownsList(List<Town> tl) {
        townsList = tl;
    }

    public static void showTowns() {
        townsList.forEach(System.out::println);
    }

    // Returns decoded neighbourList (`genome`).
    // `traversal` represents towns' indices.
    public Number[] getTownsTraversal() {

        Number[] genome = this.getGenome();
        Number[] traversal = new Number[genome.length];
        // we assume any traversal is Hamilton Cycle, so 
        // first element may be any, if cyclic transform is performed
        traversal[0] = 0;
        int index = 0;
        int i = 1;

        while (genome[index].intValue() != 0) {
            traversal[i] = genome[index];
            index = genome[index].intValue();
            i++;
        }

        // System.out.println(this);
        // for (int j = 0; j < traversal.length; j++) {
        //     System.out.print(traversal[j] + " ");
        // }
        // System.out.println();

        return traversal;
    }

    // checks whether this instance represents hamilton cycle
    public boolean isProper() {

        Number[] traversal = this.getTownsTraversal();
        Map<Integer, Boolean> travExpectedContents = new HashMap<>();
        for (int i = 0; i < traversal.length; i++) {
            travExpectedContents.put(i, true);
        }

        for (int i = 0; i < traversal.length; i++) {

            try {

                if (travExpectedContents.get(traversal[i].intValue())) {
                    travExpectedContents.put(traversal[i].intValue(), false);
                }

                else {
                    //System.out.println("false");
                    return false;
                }
            }

            catch (NullPointerException e) {

                //e.printStackTrace();
                //System.out.println("false");
                return false;
            }
        }

        //System.out.println("true");
        return true;
    }

    // calculates summary of all distances between towns in voyage
    public double getTotalDistance() {

        Number[] traversal = this.getTownsTraversal();
        double total = .0;
        // this cycle won't go if traversal.length <= 1
        for (int i = 1; i < traversal.length; i++) {
            total += townsList.get(traversal[i-1].intValue()).
                distTo(townsList.get(traversal[i].intValue()));
        }

        // Looping distance.
        // But in this case need to check for 1+ towns in voyage
        if (traversal.length > 1) {
            total += 
                townsList.get(traversal[traversal.length-1].intValue()).
                distTo(townsList.get(0));
        }

        return total;
    }

    // Travelling salesman problem solution is the shortest possible cycle.
    // Hense, fitness function should in inverse ratio to voyage distance.
    public double calculateFitnessValue(double numerator) {
        return numerator / this.getTotalDistance();
    }


    // gets Number[] representing towns traversal & encodes to neighbour indices
    public static Number[] encode(Number[] traversal) {

        Number[] indices = new Number[traversal.length];

        if (traversal.length == 0) {
            return indices;
        }

        for (int i = 1; i < indices.length; i++) {
            indices[traversal[i-1].intValue()] = traversal[i];
        }

        indices[traversal[traversal.length-1].intValue()] = traversal[0];
        return indices;
    }

    public void printoutTownsTraversal() {

        Number[] traversal = this.getTownsTraversal();
        for (int i = 0; i < traversal.length; i++) {
            System.out.println(townsList.get(traversal[i].intValue()));
        }
    }

    @Override 
    public Voyage[] crossWith(Organism another) {

        final Number[] genome1 = this.genome;
        final Number[] genome2 = another.genome;

        int rootTownNum = rand.nextInt(genome1.length);
        // taking in count `rootTownNum` as a first
        int currentLength = 1;

        Number[] newTraversal = new Number[genome1.length];
        //Set<Byte> availableTownNums = new HashSet<>();
        SearchableSet availableTownNums = new SearchableSet();
        for (int i = 0; i < genome1.length; i++) {
            availableTownNums.add(i);
        }

        newTraversal[0] = rootTownNum;
        if (!availableTownNums.remove(availableTownNums.
            searchFor(rootTownNum))) { 

            System.err.println("not removed " + rootTownNum); 
        }

        while (currentLength < genome1.length) {

            Number candidate1 = genome1[rootTownNum];
            Number candidate2 = genome2[rootTownNum];

            // both candidates are already in `newTraversal`, so 
            // to avoid replication we need to choose random next town
            // from the `availableTownNums` pool
            if (availableTownNums.searchFor(candidate1) == null &&
                availableTownNums.searchFor(candidate2) == null) {

                Number townToRemove = availableTownNums.getRandomElement();
                newTraversal[currentLength] = townToRemove;
                availableTownNums.remove(townToRemove);
                rootTownNum = townToRemove.intValue();
            }

            else {

                if (availableTownNums.searchFor(candidate1) == null) {

                    newTraversal[currentLength] = candidate2;
                    availableTownNums.remove(availableTownNums.
                        searchFor(candidate2));
                    rootTownNum = candidate2.intValue();
                }

                else if (availableTownNums.searchFor(candidate2) == null) {

                    newTraversal[currentLength] = candidate1;
                    availableTownNums.remove(availableTownNums.
                        searchFor(candidate1));
                    rootTownNum = candidate1.intValue();
                }

                // if both candidates are available to append, genetic 
                // algorithm must choose the most strong alternative => 
                // in this case the nearest town (i.e. the least distance)
                else {

                    double dist1 = 
                        townsList.get(rootTownNum).
                        distTo(townsList.get(candidate1.intValue()));
                    double dist2 = 
                        townsList.get(rootTownNum).
                        distTo(townsList.get(candidate2.intValue()));

                    Number chosenOne = (dist1 > dist2)? candidate2: candidate1;
                    newTraversal[currentLength] = chosenOne;
                    availableTownNums.remove(availableTownNums.
                        searchFor(chosenOne));
                    rootTownNum = chosenOne.intValue();
                }
            }

            currentLength++;
        }

        Voyage offspring1 = new Voyage(encode(newTraversal));
        Voyage offspring2;

        offspring1.isProper();

        // taking stronger parent as a second offspring
        if (this.getFitnessValue() > another.getFitnessValue()) {
            offspring2 = new Voyage(this);
        }

        // in this case can't use copy constructor, because conversion 
        // `Organism` -> `Voyage` is impossible
        else {

            Number[] parentGenome = new Number[genome2.length];
            for (int i = 0; i < genome2.length; i++) {
                parentGenome[i] = genome2[i];
            }

            offspring2 = new Voyage(parentGenome);
        }

        offspring2.isProper();

        Voyage[] offsprings = {offspring1, offspring2};
        return offsprings;
    }

    @Override
    public Voyage mutateGens(List<Integer> genIndices) {

        // ignoring `genIndices` in this implementation
        Voyage mutant = new Voyage(this);
        Number[] mutantGenome = mutant.genome;

        //System.out.println("before:");
        //System.out.println(mutant);

        int first = rand.nextInt(mutantGenome.length);
        int second = rand.nextInt(mutantGenome.length);
        Number temp = mutantGenome[first];

        mutantGenome[first] = mutantGenome[second];
        mutantGenome[second] = temp;

        // for (int j = 0; j < traversal.length; j++) {
        //     System.out.print(traversal[j] + " ");
        // }
        // System.out.println();

        if (mutant.isProper()) {
            return mutant;
        }

        Number[] traversal = mutant.getTownsTraversal();
        SearchableSet availableIndices = new SearchableSet();
        for (int i = 0; i < traversal.length; i++) {
            availableIndices.add(i);
        }

        int faultPosition = -1;
        for (int i = 0; i < traversal.length; i++) {

            Number searchResult = availableIndices.searchFor(traversal[i]);

            if (searchResult == null) {
                faultPosition = i;
                break;
            }

            else {
                availableIndices.remove(searchResult);
            }
        }

        while (!availableIndices.isEmpty()) {

            Number randomTown = availableIndices.getRandomElement();
            traversal[faultPosition] = randomTown;
            availableIndices.remove(randomTown);
            faultPosition++;
        }

        Number[] fixedMutantGenome = encode(traversal);
        mutant.setGenome(fixedMutantGenome);
        //mutant.isProper();
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

    private static List<Voyage> generateRandomVoyages(int townNum, 
        int outputSize) {

        List<Voyage> voyages = new LinkedList<>();
        for (int i = 0; i < outputSize; i++) {

            Number[] traversal = new Number[townNum];
            SearchableSet availableIndices = new SearchableSet();
            for (int n = 0; n < townNum; n++) {
                availableIndices.add(n);
            }

            int j = 0;
            while (!availableIndices.isEmpty()) {

                Number extracted = availableIndices.getRandomElement();
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

        System.out.println("/// SOLUTION \\\\\\");
        Number[] solTraversal = {
            0 , 32, 62, 15, 2 , 43, 31, 8 , 38, 71, 57, 9 , 30, 54, 24, 
            49, 17, 23, 48, 22, 55, 40, 42, 41, 63, 21, 60, 20, 46, 35, 
            68, 70, 59, 69, 19, 36, 4,  14, 56, 12, 53, 18, 13, 58, 65, 
            64, 37, 10, 52, 6,  34, 7,  45, 33, 51, 26, 44, 28, 47, 29, 
            3,  74, 75, 66, 25, 11, 39, 16, 50, 5,  67, 1,  73, 27, 61, 
            72};
        Voyage solution = new Voyage(Voyage.encode(solTraversal));
        System.out.println(solution.getTotalDistance());
        solution.printoutTownsTraversal();
        //solution.isProper();
        System.out.println("/// SOLUTION \\\\\\");


        List<Voyage> voyages = 
            generateRandomVoyages(townsList.size(), 20);

        Genetic geneticProcess = new Genetic(townsList.size());
        Genetic.Parameters params = geneticProcess.new Parameters();
        params.setCrossingoverProb(0.7);
        params.setMutationProb(0.3);
        geneticProcess.setParameters(params);

        fillVoyagesFitnessValues(voyages);
        List<Organism> organisms = new ArrayList<>(voyages);
        geneticProcess.setCurOrganisms(organisms);

        final int iterationsNum = 100;
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
