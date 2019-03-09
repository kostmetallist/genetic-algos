import java.util.*;


// class Organism {

//     private static Random rand = new Random();

//     private byte[] genome;
//     private double fitnessValue;
//     public double reproductionProbability;

//     public Organism(byte[] genome) {
//         this.genome = genome;
//     }

//     // implements deep copy of `another`
//     public Organism(Organism another) {
//         this.genome = another.getGenome().clone();
//     }

//     public byte[] getGenome() {
//         return this.genome;
//     }

//     public void setGenome(byte[] genome) {
//         this.genome = genome;
//     }

//     public double getFitnessValue() {
//         return this.fitnessValue;
//     }

//     public void setFitnessValue(double fitnessValue) {
//         this.fitnessValue = fitnessValue;
//     }

//     // Imitates children generation
//     public Organism[] crossWith(Organism another) {

//         int genomeLength = this.genome.length;
//         int crossingoverPoint = rand.nextInt(genomeLength-1) + 1;
//         byte[] newGenome1 = new byte[genomeLength], 
//             newGenome2 = new byte[genomeLength];

//         for (int j = 0; j < crossingoverPoint; j++) {
//             newGenome1[j] = this.genome[j];
//             newGenome2[j] = another.genome[j];
//         }

//         for (int j = crossingoverPoint; j < genomeLength; j++) {
//             newGenome1[j] = another.genome[j];
//             newGenome2[j] = this.genome[j];
//         }

//         Organism offspring1 = new Organism(newGenome1);
//         Organism offspring2 = new Organism(newGenome2);
//         Organism[] result = {offspring1, offspring2};

//         return result;
//     }

//     public Organism mutateGens(List<Integer> genIndices) {

//         Organism mutant = new Organism(this);
//         byte[] mutantGenome = mutant.getGenome();

//         for (int index : genIndices) {
//             mutantGenome[index] = (byte) ((mutantGenome[index] == 1)? 0: 1);
//         }

//         return mutant;
//     }

//     @Override 
//     public String toString() {

//         String output = new String();
//         for (byte gen : this.getGenome()) {
//             output += gen + " ";
//         }

//         return output;
//     }
// }

abstract class Organism {

    protected static Random rand = new Random();

    protected Number[] genome;
    private double fitnessValue;
    public  double reproductionProbability;

    // public Organism(byte[] genome) {
    //     this.genome = genome;
    // }

    // // implements deep copy of `another`
    // public Organism(Organism another) {
    //     this.genome = another.getGenome().clone();
    // }

    public Number[] getGenome() {
        return this.genome;
    }

    public void setGenome(Number[] genome) {
        this.genome = genome;
    }

    public double getFitnessValue() {
        return this.fitnessValue;
    }

    public void setFitnessValue(double fitnessValue) {
        this.fitnessValue = fitnessValue;
    }

    // Imitates children generation
    abstract public Organism[] crossWith(Organism another);
    // public Organism[] crossWith(Organism another) {

    //     int genomeLength = this.genome.length;
    //     int crossingoverPoint = rand.nextInt(genomeLength-1) + 1;
    //     byte[] newGenome1 = new byte[genomeLength], 
    //         newGenome2 = new byte[genomeLength];

    //     for (int j = 0; j < crossingoverPoint; j++) {
    //         newGenome1[j] = this.genome[j];
    //         newGenome2[j] = another.genome[j];
    //     }

    //     for (int j = crossingoverPoint; j < genomeLength; j++) {
    //         newGenome1[j] = another.genome[j];
    //         newGenome2[j] = this.genome[j];
    //     }

    //     Organism offspring1 = new Organism(newGenome1);
    //     Organism offspring2 = new Organism(newGenome2);
    //     Organism[] result = {offspring1, offspring2};

    //     return result;
    // }

    abstract public Organism mutateGens(List<Integer> genIndices);
    // public Organism mutateGens(List<Integer> genIndices) {

    //     Organism mutant = new Organism(this);
    //     byte[] mutantGenome = mutant.getGenome();

    //     for (int index : genIndices) {
    //         mutantGenome[index] = (byte) ((mutantGenome[index] == 1)? 0: 1);
    //     }

    //     return mutant;
    // }

    @Override 
    public String toString() {

        String output = new String();
        for (Number gen : this.getGenome()) {
            output += gen + " ";
        }

        return output;
    }
}


public class Genetic {

    private Random rand = new Random();
    private int genomeLength;

    // genetic algorithm related parameters
    private Parameters parameters;
    private List<Organism> curOrganisms = new ArrayList<>();


    // TODO declare static & make changes in using classes
    public class Parameters {

        // TODO assertions [0,1] for all probs
        private double crossingoverProb;
        private double mutationProb;
        private double delta;
        private double percentage;

        public double getCrossingoverProb() {
            return this.crossingoverProb;
        }

        public void setCrossingoverProb(double prob) {
            this.crossingoverProb = prob;
        }

        public double getMutationProb() {
            return this.mutationProb;
        }

        public void setMutationProb(double prob) {
            this.mutationProb = prob;
        }

        public double getDelta() {
            return this.delta;
        }

        public void setDelta(double val) {
            this.delta = val;
        }

        public double getPercentage() {
            return this.percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }


    public Genetic(int genomeLength) {

        //genomeLength = (int) Math.ceil(Math.log(n)/Math.log(2));
        this.genomeLength = genomeLength;
        this.parameters = new Parameters();
    }
    
    public Genetic(int genomeLength, Parameters parameters) {

        //genomeLength = (int) Math.ceil(Math.log(n)/Math.log(2));
        this.genomeLength = genomeLength;
        this.parameters = parameters;
    }

    public int getGenomeLength() {
        return this.genomeLength;
    }

    public void setGenomeLength(int genomeLength) {
        this.genomeLength = genomeLength;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<Organism> getCurOrganisms() {
        return this.curOrganisms;
    }

    public void setCurOrganisms(List<Organism> curOrganisms) {
        this.curOrganisms = curOrganisms;
    }

    public void showOrganisms() {
        this.curOrganisms.forEach(System.out::println);
    }

    // markup contains values describing division points of segment, 
    // so we will have (N+1) parts whilst markup.size() == N
    private int getInterval(double val, List<Double> markup) {

        int i = 0;

        for (Double node : markup) {

            if (val <= node) { break; }
            i++;
        }

        return i;
    }

    public boolean isLocal(List<Double> values) {

        double mean = 0.0;

        for (Double each : values) {
            mean += each;
        }

        mean /= values.size();
        int locallyPlaced = 0;

        for (Double each : values) {
            if (Math.abs(each-mean) < parameters.getDelta()) {
                locallyPlaced++;
            }
        }

        return (locallyPlaced/values.size() > parameters.getPercentage());
    }

    private void fillReproductionProbs(List<Organism> organisms) {

        if (!organisms.isEmpty()) {

            double minFitness = organisms.get(0).getFitnessValue();
            for (Organism org : organisms) {

                double fitVal = org.getFitnessValue();
                org.reproductionProbability = fitVal;
                if (fitVal < minFitness) {
                    minFitness = fitVal;
                }
            }

            // We need all positive values to construct probabilites.
            // In case of negative just lift up all the values.
            if (minFitness < 0) {

                // `liftingDelta` describes difference between min fitness
                // value and zero.
                double liftingDelta = 1;
                for (Organism org : organisms) {
                    org.reproductionProbability += liftingDelta - minFitness;
                }
            }

            double sumValue = .0;
            for (Organism org : organisms) {
                sumValue += org.reproductionProbability;
            }
            
            for (Organism org : organisms) {
                org.reproductionProbability /= sumValue;
            }
        }

        else {
            System.err.println("fillReproductionProbs: `organisms` is empty");
        }
    }

    // Careful -- modifies curOrganisms.
    // Assure you have assigned fitness values to organisms.
    public void doGenerationStep() {

        List<Organism> selected = new ArrayList<>();
        List<Double> probNodes = new ArrayList<>();

        fillReproductionProbs(curOrganisms);
        double accumulator = 0.0;
        for (Organism each : curOrganisms) {

            accumulator += each.reproductionProbability;
            probNodes.add(accumulator);
        }

        probNodes.remove(probNodes.size()-1);

        // reproducing
        for (int i = 0; i < curOrganisms.size(); i++) {

            int index = getInterval(rand.nextDouble(), probNodes);
            selected.add(curOrganisms.get(index));
        }

        // crossing
        List<Organism> afterCrossing = new ArrayList<>();
        List<Integer> availableIndices = new ArrayList<>();

        for (int i = 0; i < selected.size(); i++) {
            availableIndices.add(i);
        }

        while (availableIndices.size() > 1) {

            int n1 = rand.nextInt(availableIndices.size());
            Organism org1 = selected.get(n1);
            availableIndices.remove(n1);

            int n2 = rand.nextInt(availableIndices.size());
            Organism org2 = selected.get(n2);
            availableIndices.remove(n2);

            if (rand.nextDouble() >= parameters.getCrossingoverProb()) {

                availableIndices.add(n1);
                availableIndices.add(n2);
                continue;
            }

            Organism[] offsprings = org1.crossWith(org2);
            Organism offspring1 = offsprings[0];
            Organism offspring2 = offsprings[1];

            afterCrossing.add(offspring1);
            afterCrossing.add(offspring2);
        }

        // mutation
        for (Organism each : afterCrossing) {

            // mutate this organism?
            if (rand.nextDouble() >= parameters.getMutationProb()) {
                continue;
            }

            List<Integer> gensToMutate = new ArrayList<>();
            for (int i = 0; i < genomeLength; i++) {
                if (rand.nextDouble() < parameters.getMutationProb()) {
                    gensToMutate.add(i);
                }
            }

            each = each.mutateGens(gensToMutate);
        }
        
        curOrganisms = afterCrossing;
    }
}