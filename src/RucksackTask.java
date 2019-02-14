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

    public double getFitnessValue() {

        double fitValue = .0;
        byte[] items = this.getGenome();
        for (int i = 0; i < items.length; i++) {
            fitValue += items[i] * itemPool.get(i).getPrice();
        }

        return fitValue;
    }

    public double getLogPenalty(
        double rucksackCapacity,
        double multiplier) {

        double penValue = .0;
        byte[] items = this.getGenome();

        for (int i = 0; i < itemPool.size(); i++) {
            penValue += items[i]*itemPool.get(i).getWeight();
        }

        penValue -= 1 + multiplier*(penValue - rucksackCapacity);
        // getting log2
        return Math.log(penValue) / Math.log(2);
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

    public static void main(String[] args) {

        // byte[] genome = new byte[8];
        // Random rnd = new Random();
        // rnd.nextBytes(genome);

        // for (int i = 0; i < 8; i++) {
        //     System.out.print(genome[i]);
        // }
        // System.out.println();
        // System.out.println(genome);

        // Loadout item = new Loadout(genome);
        // System.out.println(item.getGenome());

        int poolSize = 60;
        int casesNumber = 10;

        RucksackTask rt = new RucksackTask();
        List<Item> itemPool = rt.generateItemPool(poolSize);
        List<Loadout> loadoutList = new ArrayList<>();

        for (int i = 0; i < casesNumber; i++) {
            loadoutList.add(rt.generateRandomLoadout(itemPool));
        }

        for (Loadout loadout : loadoutList) {
            System.out.println(loadout);
        }
    }
}
