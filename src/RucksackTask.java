import java.util.Random;
import java.util.List;
import java.util.ArrayList;


class Item {

    private double price;
    private double weight;


    public Item() {
        this.price = .0;
        this.weight = .0;
    }

    public Item(double price, double weight) {
        this.price = price;
        this.weight = weight;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}

class Loadout extends Organism {

    final private List<Item> itemPool;


    // attention: for simplicity, there is a shallow copying
    public Loadout(Number[] genome, List<Item> itemPool) {

        this.genome = genome;
        this.itemPool = itemPool;

        if (genome.length != itemPool.size()) {
            System.err.println("genome size and itemPool" +
                "size are different!");
        }
    }

    // deep copying
    public Loadout(Loadout another) {

        this.genome = another.genome.clone();
        this.itemPool = another.itemPool;
    }

    public double getItemsWeight() {

        double itemsWeight = .0;
        Number[] items = this.getGenome();

        for (int i = 0; i < items.length; i++) {
            itemsWeight += items[i].byteValue() * itemPool.get(i).getWeight();
        }

        return itemsWeight;
    }

    public double calculateFitnessValue() {

        double fitValue = .0;
        Number[] items = this.getGenome();

        for (int i = 0; i < items.length; i++) {
            fitValue += items[i].byteValue() * itemPool.get(i).getPrice();
        }

        return fitValue;
    }

    public double calculateFitnessValue(String penaltyType, 
        double rucksackCapacity) {

        double fitValue = .0;
        final Number[] items = this.getGenome();
        final double mult = 1.5;

        for (int i = 0; i < items.length; i++) {
            fitValue += items[i].byteValue() * itemPool.get(i).getPrice();
        }

        // System.out.println("FITNESS BEFORE: " + fitValue + "LOADOUT WEIGHT: " + 
        //     this.getItemsWeight());

        switch (penaltyType) {

            case "linear":
                fitValue -= getLinearPenalty(rucksackCapacity, mult);
                break;

            case "squared":
                fitValue -= getSquaredPenalty(rucksackCapacity, mult);
                break;

            case "log":
                fitValue -= getLogPenalty(rucksackCapacity, mult);
                break;
        }

        // System.out.println("FITNESS AFTER: " + fitValue);
        return fitValue;
    }

    private double getLinearPenalty(
        double rucksackCapacity,
        double multiplier) {

        return multiplier * (getItemsWeight() - rucksackCapacity);
    }

    private double getSquaredPenalty(
        double rucksackCapacity,
        double multiplier) {

        double linearPenalty = getLinearPenalty(rucksackCapacity, multiplier);
        return linearPenalty * linearPenalty;
    }

    private double getLogPenalty(
        double rucksackCapacity,
        double multiplier) {

        double logArgument = 
            1 + getLinearPenalty(rucksackCapacity, multiplier);
        // getting log2
        return Math.log(logArgument) / Math.log(2);
    }

    private List<Integer> getPresentItemsIndices() {

        List<Integer> indices = new ArrayList<>();
        Number[] genome = this.getGenome();

        for (int i = 0; i < genome.length; i++) {
            if (genome[i].byteValue() == 1) { indices.add(i); }
        }

        return indices;
    }

    public void restore(double rucksackCapacity) {

        while (this.getItemsWeight() > rucksackCapacity) {

            List<Integer> itemsIndices = this.getPresentItemsIndices();
            if (itemsIndices.isEmpty()) {
                System.out.println("W: nothing to drop from loadout " + 
                    this);
                break;
            }

            Integer minPriceIndex = itemsIndices.get(0);
            double  minPrice = 
                this.itemPool.get(itemsIndices.get(0)).getPrice();

            // getting index appropriate to min price item 
            for (Integer i : itemsIndices) {

                double itemPrice = 
                    this.itemPool.get(i).getPrice();
                if (itemPrice < minPrice) {

                    minPrice = itemPrice;
                    minPriceIndex = i;
                }
            }

            // removing that costless item to reduce weight
            this.getGenome()[minPriceIndex] = (byte) 0;
        }
    } 

    @Override
    public Organism[] crossWith(Organism another) {

        int genomeLength = this.genome.length;
        int crossingoverPoint = rand.nextInt(genomeLength-1) + 1;
        Byte[] newGenome1 = new Byte[genomeLength], 
            newGenome2 = new Byte[genomeLength];

        for (int j = 0; j < crossingoverPoint; j++) {
            newGenome1[j] = this.genome[j].byteValue();
            newGenome2[j] = another.genome[j].byteValue();
        }

        for (int j = crossingoverPoint; j < genomeLength; j++) {
            newGenome1[j] = another.genome[j].byteValue();
            newGenome2[j] = this.genome[j].byteValue();
        }

        Organism offspring1 = new Loadout(newGenome1, this.itemPool);
        Organism offspring2 = new Loadout(newGenome2, this.itemPool);
        Organism[] result = {offspring1, offspring2};

        return result;
    }

    @Override
    public Organism mutateGens(List<Integer> genIndices) {

        Organism mutant = new Loadout(this);
        Number[] mutantGenome = mutant.getGenome();

        for (int index : genIndices) {
            mutantGenome[index] = 
                (byte) ((mutantGenome[index].byteValue() == 1)? 0: 1);
        }

        return mutant;
    }
}


public class RucksackTask {

    public List<Item> generateItemPool(int itemNumber) {

        Random rnd = new Random();
        List<Item> itemPool = new ArrayList<>();

        for (int i = 0; i < itemNumber; i++) {
            itemPool.add(new Item(rnd.nextDouble(), rnd.nextDouble()*2));
        }

        return itemPool;
    }

    public Loadout generateRandomLoadout(List<Item> itemPool) {

        Random rnd = new Random();
        Byte[] binVector = new Byte[itemPool.size()];

        for (int i = 0; i < itemPool.size(); i++) {

            binVector[i] = (byte) (rnd.nextBoolean()? 1: 0);
        }

        return new Loadout(binVector, itemPool);
    }

    public void restoreLoadouts(List<Loadout> loadouts, 
        double rucksackCapacity) {

        for (Loadout loadout : loadouts) {
            loadout.restore(rucksackCapacity);
        }
    }

    public void fillLoadoutsFitnessValues(List<Loadout> loadouts, 
        double rucksackCapacity, 
        String penaltyType) {

        loadouts.forEach(loadout -> 
            loadout.setFitnessValue(
                loadout.calculateFitnessValue(penaltyType, rucksackCapacity)));
    }

    public void showMaxMin(List<Loadout> loadouts) {

        double max = loadouts.get(0).calculateFitnessValue();
        double min = loadouts.get(0).calculateFitnessValue();

        for (Loadout loadout : loadouts) {

            double fitValue = loadout.calculateFitnessValue();

            if (fitValue > max) {
                max = fitValue;
                // System.out.println("max is reassigned to " + max + 
                //     " with loadout probability " + loadout.get);
            }

            if (fitValue < min) {
                min = fitValue;
            }
        }

        System.out.println(min + "-" + max);
    }

    public static void main(String[] args) {

        // that also defines genome's size
        final int poolSize = 80;
        final int casesNumber = 30;
        final int iterationsNum = 20;
        final double rucksackCapacity = 35;

        if (args.length < 1) {

            System.err.println("Usage: <class name> <method>" + 
                        " [<options>]");
            System.err.println("Available methods: penalty, restoration");
            return;
        }

        boolean useRestoration = false;
        String method = args[0];
        String penaltyType = "none";

        switch (method) {

            case "penalty": 

                if (args.length < 2) {

                    System.err.println("For using method \'penalty\'" + 
                        " specify penalty type");
                    System.err.println("Usage: <class name> <method>" + 
                        " [<penalty type>]");
                    return;
                }

                penaltyType = args[1];
                break;

            case "restoration":
                useRestoration = true;
                break;

            default: 
                System.err.println("Unknown method: " + method);
                System.err.println("Available methods: penalty, restoration");
                return;
        }

        RucksackTask rt = new RucksackTask();
        List<Item> itemPool = rt.generateItemPool(poolSize);
        List<Loadout> loadoutList = new ArrayList<>();

        for (int i = 0; i < casesNumber; i++) {
            loadoutList.add(rt.generateRandomLoadout(itemPool));
        }

        // for (Loadout loadout : loadoutList) {
        //     System.out.println(loadout);
        // }

        Genetic geneticProcess = new Genetic(poolSize);
        Genetic.Parameters params = geneticProcess.new Parameters();
        params.setCrossingoverProb(0.7);
        params.setMutationProb(0.3);
        params.setDelta(0.05);
        params.setPercentage(0.75);
        geneticProcess.setParameters(params);

        if (useRestoration) { 
            rt.restoreLoadouts(loadoutList, rucksackCapacity); 
        }

        rt.fillLoadoutsFitnessValues(loadoutList, 
            rucksackCapacity, penaltyType);
        List<Organism> organisms = new ArrayList<>(loadoutList);
        geneticProcess.setCurOrganisms(organisms);
        rt.showMaxMin(loadoutList);

        for (int i = 0; i < iterationsNum; i++) {

            geneticProcess.doGenerationStep();
            List<Organism> newOrganisms = geneticProcess.getCurOrganisms();
            List<Loadout> newLoadouts = new ArrayList<>();

            for (Organism org : newOrganisms) {
                newLoadouts.add(new Loadout(org.getGenome(), itemPool));
            }

            if (useRestoration) { 
                rt.restoreLoadouts(newLoadouts, rucksackCapacity);
            }

            rt.fillLoadoutsFitnessValues(newLoadouts, 
                rucksackCapacity, penaltyType);

            for (int j = 0; j < newOrganisms.size(); j++) {
                newOrganisms.get(j).setFitnessValue(
                    newLoadouts.get(j).getFitnessValue());
            }

            System.out.println("--------------------------------------");
            rt.showMaxMin(newLoadouts);
        }
    }
}
