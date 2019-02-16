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

    List<Item> itemPool;


    // attention: for simplicity, there is a shallow copying
    public Loadout(byte[] genome, List<Item> itemPool) {

        super(genome);
        this.itemPool = itemPool;

        if (genome.length != itemPool.size()) {
            System.err.println("genome size and itemPool" +
                "size are different!");
        }
    }

    public double getItemsWeight() {

        double itemsWeight = .0;
        byte[] items = this.getGenome();

        for (int i = 0; i < items.length; i++) {
            itemsWeight += items[i] * itemPool.get(i).getWeight();
        }

        return itemsWeight;
    }

    public double getFitnessValue() {

        double fitValue = .0;
        byte[] items = this.getGenome();

        for (int i = 0; i < items.length; i++) {
            fitValue += items[i] * itemPool.get(i).getPrice();
        }

        return fitValue;
    }

    public double getFitnessValue(String penaltyType, 
        double rucksackCapacity) {

        double fitValue = .0;
        final byte[] items = this.getGenome();
        final double mult = 0.03;

        for (int i = 0; i < items.length; i++) {
            fitValue += items[i] * itemPool.get(i).getPrice();
        }

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

        System.out.println("FITNESS: " + fitValue);
        return fitValue;
    }

    public double getLinearPenalty(
        double rucksackCapacity,
        double multiplier) {

        return multiplier * (getItemsWeight() - rucksackCapacity);
    }

    public double getSquaredPenalty(
        double rucksackCapacity,
        double multiplier) {

        double linearPenalty = getLinearPenalty(rucksackCapacity, multiplier);
        return linearPenalty * linearPenalty;
    }

    public double getLogPenalty(
        double rucksackCapacity,
        double multiplier) {

        double logArgument = 
            1 + getLinearPenalty(rucksackCapacity, multiplier);
        System.out.println("la " + logArgument);
        // getting log2
        return Math.log(logArgument) / Math.log(2);
    }

    @Override
    public String toString() {

        String output = new String();
        byte[] items = this.getGenome();

        for (int i = 0; i < items.length; i++) {
            output += items[i];
        }

        return output;
    }
}


public class RucksackTask {

    public List<Item> generateItemPool(int itemNumber) {

        Random rnd = new Random();
        List<Item> itemPool = new ArrayList<>();

        for (int i = 0; i < itemNumber; i++) {
            itemPool.add(new Item(rnd.nextDouble(), rnd.nextDouble()));
        }

        return itemPool;
    }

    public Loadout generateRandomLoadout(List<Item> itemPool) {

        Random rnd = new Random();
        byte[] binVector = new byte[itemPool.size()];

        for (int i = 0; i < itemPool.size(); i++) {

            binVector[i] = (byte) (rnd.nextBoolean()? 1: 0);
        }

        return new Loadout(binVector, itemPool);
    }

    public void fillLoadoutsProbabilities(List<Loadout> loadouts, 
        double rucksackCapacity) {

        // in this method we assume all loadouts have non-negative fit values
        double sumValue = .0;

        for (Loadout loadout : loadouts) {
            sumValue += loadout.getFitnessValue("log", rucksackCapacity);
        }

        for (Loadout loadout : loadouts) {
            loadout.setReproductionProb(
                loadout.getFitnessValue("log", rucksackCapacity) / sumValue);
        }
    }

    public void showMaxMin(List<Loadout> loadouts) {

        double max = loadouts.get(0).getFitnessValue();
        double min = loadouts.get(0).getFitnessValue();

        for (Loadout loadout : loadouts) {

            double fitValue = loadout.getFitnessValue();

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
        final double rucksackCapacity = 35;

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
        params.setCrossingoverProb(0.8);
        params.setMutationProb(0.1);
        params.setDelta(0.05);
        params.setPercentage(0.75);
        geneticProcess.setParameters(params);

        rt.fillLoadoutsProbabilities(loadoutList, rucksackCapacity);
        List<Organism> organisms = new ArrayList<>(loadoutList);
        geneticProcess.setCurOrganisms(organisms);
        rt.showMaxMin(loadoutList);

        for (int i = 0; i < 10; i++) {

            geneticProcess.doGenerationStep();
            List<Organism> newOrganisms = geneticProcess.getCurOrganisms();
            List<Loadout> newLoadouts = new ArrayList<>();

            for (Organism org : newOrganisms) {
                newLoadouts.add(new Loadout(org.getGenome(), itemPool));
            }

            rt.fillLoadoutsProbabilities(newLoadouts, rucksackCapacity);

            for (int j = 0; j < newOrganisms.size(); j++) {
                newOrganisms.get(j).setReproductionProb(
                    newLoadouts.get(j).getReproductionProb());
            }

            System.out.println("--------------------------------------");
            rt.showMaxMin(newLoadouts);
        }
    }
}
