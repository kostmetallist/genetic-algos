import java.util.*;


class Organism {

    private byte[] genome;
    private double reproductionProb;

    public Organism(byte[] genome) {
        this.genome = genome;
    }

    public byte[] getGenome() {
        return this.genome;
    }

    public double getReproductionProb() {
        return this.reproductionProb;
    }

    public void setReproductionProb(double prob) {
        this.reproductionProb = prob;
    }
}


public class Genetic {

    private int genomeLength;

    // genetic algorithm related parameters
    private Parameters parameters;
    private List<Organism> curOrganisms = new ArrayList<>();


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

    public void makeRandomColony(int n) {

        Random rand = new Random();

        for (int i = 0; i < n; i++) {

            byte[] genome = new byte[genomeLength];

            for (int j = 0; j < genomeLength; j++) {
                genome[j] = (byte) (rand.nextBoolean()? 1 : 0);
            }

            Organism org = new Organism(genome);
            curOrganisms.add(org);
        }
    }

    public void showOrganisms() {

        System.out.println("Current organisms:");

        for (Organism every : this.curOrganisms) {

            for (int i = 0; i < genomeLength; i++)
                System.out.print(every.getGenome()[i] + "");

            System.out.println();
        }
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

    // careful -- modifies curOrganisms
    // assure you have assigned probabilities to organisms
    public void doGenerationStep() {

        List<Organism> selected = new ArrayList<>();
        List<Double> probNodes = new ArrayList<>();
        double accumulator = 0.0;

        for (Organism each : curOrganisms) {

            accumulator += each.getReproductionProb();
            probNodes.add(accumulator);
        }

        probNodes.remove(probNodes.size()-1);

        // reproducing
        Random rand = new Random();

        for (int i = 0; i < curOrganisms.size(); i++) {

            int index = getInterval(rand.nextDouble(), probNodes);
            selected.add(curOrganisms.get(index));
        }

        // System.out.println("Selected:");
        // for (Organism each : selected) {

        //     byte[] genome = each.getGenome();

        //     for (int i = 0; i < genomeLength; i++) {
        //         System.out.print(genome[i] + "");
        //     }
        //     System.out.println();
        // }

        // crossing
        List<Organism> afterCrossing = new ArrayList<>();
        List<Integer> availableIndices = new ArrayList<>();

        for (int i = 0; i < selected.size(); i++) {
            availableIndices.add(i);
        }

        while (availableIndices.size() > 1) {

            int n1 = rand.nextInt(availableIndices.size());
            Organism org1 = selected.get(n1);
            byte[] genome1 = org1.getGenome();
            availableIndices.remove(n1);

            int n2 = rand.nextInt(availableIndices.size());
            Organism org2 = selected.get(n2);
            byte[] genome2 = org2.getGenome();
            availableIndices.remove(n2);

            if (rand.nextDouble() >= parameters.getCrossingoverProb()) {

                availableIndices.add(n1);
                availableIndices.add(n2);
                continue;
            }

            //System.out.println("crossing " + n1 + " with " + n2);

            int crossingoverPoint = rand.nextInt(genomeLength-1) + 1;
            byte[] newGenome1 = new byte[genomeLength], newGenome2 = new byte[genomeLength];

            for (int j = 0; j < crossingoverPoint; j++) {
                newGenome1[j] = genome1[j];
                newGenome2[j] = genome2[j];
            }

            for (int j = crossingoverPoint; j < genomeLength; j++) {
                newGenome1[j] = genome2[j];
                newGenome2[j] = genome1[j];
            }

            Organism offspring1 = new Organism(newGenome1);
            Organism offspring2 = new Organism(newGenome2);

            afterCrossing.add(offspring1);
            afterCrossing.add(offspring2);
        }

        // mutation
        for (Organism each : afterCrossing) {

            byte[] genome = each.getGenome();

            if (rand.nextDouble() >= parameters.getMutationProb()) {
                continue;
            }

            for (int i = 0; i < genomeLength; i++) {

                if (rand.nextDouble() < parameters.getMutationProb()) {
                    genome[i] = (byte) ((genome[i] == 1)? 0: 1);
                }
            }
        }
        
        curOrganisms = afterCrossing;
    }
}