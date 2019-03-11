import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


class Vector extends Organism {

    public Vector(Number[] coords) {
        this.genome = coords;
    }

    public Vector(Vector another) {
        this.genome = another.genome.clone();
    }

    @Override
    public Vector[] crossWith(Organism another) {

        Number[] genome1 = this.genome;
        Number[] genome2 = another.genome;

        Number[] newGenome1 = new Number[genome1.length];
        Number[] newGenome2 = new Number[genome1.length];

        double delta = Math.abs(0.1);
        for (int i = 0; i < genome1.length; i++) {

            // a1, a2 randomly from [-delta, 1+delta]
            double a1 = rand.nextDouble() * (2*delta + 1) - delta;
            double a2 = rand.nextDouble() * (2*delta + 1) - delta;

            double value1 = genome1[i].doubleValue();
            double value2 = genome2[i].doubleValue();
            newGenome1[i] = value1 + a1*(value2 - value1);
            newGenome2[i] = value1 + a2*(value2 - value1);
        }

        Vector offspring1 = new Vector(newGenome1);
        Vector offspring2 = new Vector(newGenome2);

        // System.out.println("Crossing");
        // System.out.println(this);
        // System.out.println(another);
        System.out.println(offspring1);
        System.out.println(offspring2);

        Vector[] output = {offspring1, offspring2};
        return output;
    }

    @Override
    public Vector mutateGens(List<Integer> genIndices) {

        Vector mutant = new Vector(this);
        double delta = 5;

        for (Integer i : genIndices) {
            mutant.genome[i] = mutant.genome[i].doubleValue() + 
                rand.nextDouble() * delta;
        }

        return mutant;
    }

    // modifies Vector's coordinates in case they are out of bounds
    public void repair(Number from, Number to) {

        for (int i = 0; i < genome.length; i++) {

            if (genome[i].doubleValue() > to.doubleValue()) {
                genome[i] = to;
            }

            if (genome[i].doubleValue() < from.doubleValue()) {
                genome[i] = from;
            }
        }
    }

    // forall i input[i] must be in bounds [-600, 600]
    public static double griewankFunction(Number[] input) {

        double sum = 0;
        double product = 1;

        for (int i = 0; i < input.length; i++) {

            double element = input[i].doubleValue();
            sum += element * element;
            product *= Math.cos(element / Math.sqrt(i+1));
        }

        return sum/4000 - product + 1;
    }

    public double calculateFitnessValue(double numerator) {
        return numerator / griewankFunction(this.genome);
    }
}


public class MinimumSearcher {

    private static Random rand = new Random();

    // `from` & `to` represent bounds for generated Vector components
    private static List<Vector> generateVectorPopulation(int dimension, 
        int cardinality, Number from, Number to) {

        List<Vector> result = new LinkedList<>();
        for (int i = 0; i < cardinality; i++) {

            Number[] genome = new Number[dimension];
            for (int j = 0; j < dimension; j++) {
                genome[j] = rand.nextDouble() * 
                    (to.doubleValue()-from.doubleValue()) + from.doubleValue();
            }

            Vector vector = new Vector(genome);
            result.add(vector);
        }

        return result;
    }

    private static void writeCsvData(String filename, List<Vector> data) {

        File file = new File(filename);
        FileWriter fw = null;
        BufferedWriter bw = null;
        String sep = System.getProperty("line.separator");

        try {

            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (Vector v : data) {

                Number[] coords = v.getGenome();
                for (int i = 0; i < coords.length; i++) {
                    bw.write(coords[i].doubleValue() + ",");
                }

                bw.write(Vector.griewankFunction(coords) + sep);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            try {

                bw.close();
                fw.close();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void fillVectorsFitnessValues(List<Vector> vectors, 
        double numerator) {

        vectors.forEach(v -> 
            v.setFitnessValue(v.calculateFitnessValue(numerator)));
    }

    public static void main(String[] args) {

        long timeStart = System.nanoTime();

        List<Vector> gridVectors = new ArrayList<>();
        double from = -20.0, 
            to = 20.0, 
            step = 0.5;

        double x = from;
        while (x < to) {

            double y = from;
            while (y < to) {

                Number[] arr = {x, y};
                Vector v = new Vector(arr);
                gridVectors.add(v);
                y += step;
            }

            x += step;
        }

        writeCsvData("data/griewank.csv", gridVectors);
        int genomeLength = 2;
        double numerator = 3.0;

        List<Vector> vectors = 
            generateVectorPopulation(genomeLength, 40, from, to);
        Genetic geneticProcess = new Genetic(genomeLength);
        Genetic.Parameters params = geneticProcess.new Parameters();
        params.setCrossingoverProb(0.7);
        params.setMutationProb(0.15);
        geneticProcess.setParameters(params);

        fillVectorsFitnessValues(vectors, numerator);
        List<Organism> organisms = new ArrayList<>(vectors);
        geneticProcess.setCurOrganisms(organisms);

        int epochNum = 100;
        for (int i = 0; i < epochNum; i++) {

            geneticProcess.doGenerationStep();
            List<Organism> newOrganisms = geneticProcess.getCurOrganisms();
            List<Vector> newVectors = new ArrayList<>();

            for (Organism org : newOrganisms) {
                newVectors.add(new Vector(org.getGenome()));
            }

            newVectors.forEach(v -> v.repair(from, to));
            writeCsvData("data/griewank_" + i + ".csv", newVectors);
            fillVectorsFitnessValues(newVectors, numerator);

            for (int j = 0; j < newOrganisms.size(); j++) {
                newOrganisms.get(j).setFitnessValue(
                    newVectors.get(j).getFitnessValue());
            }

            System.out.println("--------------------------------------");

            if (i == epochNum-1) {

                Vector someVector = newVectors.get(0);
                Number[] genome = someVector.getGenome();
                double distFrom0 = 0.0;

                for (int j = 0; j < genome.length; j++) {

                    distFrom0 += 
                        genome[j].doubleValue()*genome[j].doubleValue();
                }

                System.out.println("Distance: " + Math.sqrt(distFrom0));
            }
        }

        long timeDelta = System.nanoTime() - timeStart;
        System.out.println("Time used: " + timeDelta/1000000);
    }
}