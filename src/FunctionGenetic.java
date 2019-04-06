import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


abstract class EntryContent {
    abstract public double getData(List<Double> arguments);
    abstract public int getArgumentsNumber();
}


class Variable extends EntryContent {

    private double value;
    public static boolean verboseMode = false;


    public Variable(double value) {
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    // ignoring `arguments`
    public double getData(List<Double> arguments) {

        double output = this.value;
        if (verboseMode) {
            System.out.println(output);
        }

        return output;
    }

    public int getArgumentsNumber() {
    	return 0;
    }

    @Override 
    public String toString() {
        return String.valueOf(this.value);
    }
}


class FunctionalElement extends EntryContent {

    private int simulationIndex;
    private double parameter;
    public static int powIndex = 9;
    public static boolean verboseMode = false;


    public FunctionalElement(String operationNotation) {

        switch (operationNotation) {

            case "+": this.simulationIndex = 1; break;
            case "-": this.simulationIndex = 2; break;
            case "*": this.simulationIndex = 3; break;
            case "/": this.simulationIndex = 4; break;
            case "abs": this.simulationIndex = 5; break;
            case "sin": this.simulationIndex = 6; break;
            case "cos": this.simulationIndex = 7; break;
            case "exp": this.simulationIndex = 8; break;
            default: 
                this.simulationIndex = 0;
                System.err.println("FunctionalElement is not recognised");
        }
    }

    public FunctionalElement(String operationNotation, double parameter) {

        switch (operationNotation) {

            case "pow": this.simulationIndex = powIndex; break;
            default: 
                this.simulationIndex = 0;
                System.err.println("FunctionalElement is not recognised");
        }

        this.parameter = parameter;
    }

    public FunctionalElement(int simulationIndex, double parameter) {

        this.simulationIndex = simulationIndex;
        this.parameter = parameter;
    }

    public double getData(List<Double> arguments) {

        if (verboseMode) {
            System.out.println(this);
        }

        switch (this.simulationIndex) {

            // acts like a stub
            case 0: return 1;
            case 1: 
                if (arguments.size() != 2) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return arguments.get(0) + arguments.get(1);

            case 2: 
                if (arguments.size() != 2) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return arguments.get(0) - arguments.get(1);

            case 3: 
                if (arguments.size() != 2) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return arguments.get(0) * arguments.get(1);

            case 4: 
                if (arguments.size() != 2) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                // TODO exception handling
                return arguments.get(0) / arguments.get(1);

            case 5: 
                if (arguments.size() != 1) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return Math.abs(arguments.get(0));

            case 6: 
                if (arguments.size() != 1) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return Math.sin(arguments.get(0));

            case 7: 
                if (arguments.size() != 1) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return Math.cos(arguments.get(0));

            case 8: 
                if (arguments.size() != 1) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return Math.exp(arguments.get(0));

            case 9: 
                if (arguments.size() != 1) {
                    System.err.println("FunctionalElement incorrect " +
                        "arguments number for code " + this.simulationIndex);
                    return 1;
                }

                return Math.pow(arguments.get(0), this.parameter);

            default: 
                System.err.println("FunctionalElement unexpected " + 
                        "simulationIndex " + this.simulationIndex);
                return 1;
        }
    }

    public int getArgumentsNumber() {
    	if (this.simulationIndex < 5)
    		return 2;
    	else
    		return 1;
    }

    @Override
    public String toString() {

        switch (this.simulationIndex) {
            case 1: return "+";
            case 2: return "-";
            case 3: return "*";
            case 4: return "/";
            case 5: return "abs";
            case 6: return "sin";
            case 7: return "cos";
            case 8: return "exp";
            case 9: return "pow(" + this.parameter + ")";
            default: return "undefined";
        }
    }
}


class TreeEntry {
    
    private List<TreeEntry> children = new ArrayList<>();
    private EntryContent content;
    private int id;
    private static int globalId = 0;


    public TreeEntry(EntryContent content) {

        this.content = content;
        this.id = globalId;
        globalId += 1;
    }

    public List<TreeEntry> getChildren() {
        return this.children;
    }

    public EntryContent getContent() {
    	return this.content;
    }

    public void setContent(EntryContent content) {
        this.content = content;
    }

    public void addChild(TreeEntry entry) {
        this.children.add(entry);
    }

    public boolean haveChildren() {
        return !this.children.isEmpty();
    }

    public double getData() {

        List<Double> childrenData = new ArrayList<>();
        for (TreeEntry child : this.children) {
            childrenData.add(child.getData());
        }

        return this.content.getData(childrenData);
    }

    public int getEntriesNumber() {

        if (!this.haveChildren()) {
            return 1;
        }

        // init value is 1 because of counting itself
        int accumulator = 1;
        for (TreeEntry child : this.children) {
            accumulator += child.getEntriesNumber();
        }

        return accumulator;
    }

    private void collectParentals(List<TreeEntry> parentals) {
        
        if (this.haveChildren()) {

            parentals.add(this);
            for (TreeEntry child : this.children) 
                child.collectParentals(parentals);
        }
    }

    public List<TreeEntry> getParentalEntries() {
        
        List<TreeEntry> parentals = new ArrayList<>();
        this.collectParentals(parentals);
        return parentals;
    }

    public void writeDot(int parentId, BufferedWriter bw) {

        try {
            if (parentId != -1) {
                bw.write("  " + parentId + " -> " + this.id + ";");
                bw.newLine();
            }

            bw.write("  " + this.id + "[label=\"" + this.toString() + "\"];");
            bw.newLine();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        for (TreeEntry child : this.children) {
            child.writeDot(this.id, bw);
        }
    }

    @Override
    public String toString() {
        return this.content.toString();
    }
}


class ProgramTree implements Comparable<ProgramTree> {

    private Random rand = new Random();
    private TreeEntry root;
    private static double targetValue;


    public ProgramTree(TreeEntry root) {
        this.root = root;
    }

    public TreeEntry getRoot() {
        return this.root;
    }

    public static void setTargetValue(double tv) {
        targetValue = tv;
    }

    public double getFitnessValue() {
        return Math.exp(-Math.abs(this.root.getData() - targetValue));
    }

    // implementing
    public int compareTo(ProgramTree another) {
        return (this.getFitnessValue() > another.getFitnessValue())? 1: -1;
    }

    // performs deep copying
    public ProgramTree cloneTree() {

        TreeEntry clonedRoot = new TreeEntry(this.root.getContent());

        List<TreeEntry> parents = Arrays.asList(this.root);
        List<TreeEntry> clonedParents = Arrays.asList(clonedRoot);

        while (!parents.isEmpty()) {

            List<TreeEntry> newParents = new ArrayList<>();
            List<TreeEntry> newClonedParents = new ArrayList<>();

            for (int i = 0; i < parents.size(); i++) {
                for (TreeEntry child : parents.get(i).getChildren()) {

                    EntryContent ec = child.getContent();
                    TreeEntry clonedChild = new TreeEntry(ec);
                    clonedParents.get(i).addChild(clonedChild);

                    if (ec.getArgumentsNumber() > 0) {
                        newParents.add(child);
                        newClonedParents.add(clonedChild);
                    }
                }
            }

            parents = newParents;
            clonedParents = newClonedParents;
        }

        return new ProgramTree(clonedRoot);
    }

    public ProgramTree[] crossWith(ProgramTree another) {

        ProgramTree offspring1 = this.cloneTree();
        ProgramTree offspring2 = another.cloneTree();

        List<TreeEntry> parentals1 = offspring1.getRoot().getParentalEntries();
        List<TreeEntry> recessiveCandidates = new ArrayList<>();
        int retriesLimit = 3, 
            crossingIteration = 0;
        TreeEntry chosenParent1 = null, 
            recombinationEntry1 = null;
        List<TreeEntry> recombinationCandidates1;
        int preferredArgNum = 0;

        // trying `retriesLimit` times before 
        // surrender to cross
        while (recessiveCandidates.isEmpty() && 
            crossingIteration < retriesLimit) {

            chosenParent1 = parentals1.get(rand.nextInt(parentals1.size()));
            recombinationCandidates1 = chosenParent1.getChildren();

            recombinationEntry1 = recombinationCandidates1.get(rand.nextInt(
                    recombinationCandidates1.size()));
            preferredArgNum = 
                recombinationEntry1.getContent().getArgumentsNumber();

            List<TreeEntry> parentals2 = 
                offspring2.getRoot().getParentalEntries();

            for (TreeEntry each : parentals2) {
                for (TreeEntry child : each.getChildren()) {
                    if (child.getContent().getArgumentsNumber() == 
                        preferredArgNum) {

                        recessiveCandidates.add(each);
                        break;
                    }
                }
            }

            crossingIteration++;
        }

        if (recessiveCandidates.isEmpty()) {
            ProgramTree[] result = {offspring1, offspring2};
            return result;
        }

        TreeEntry chosenParent2 = 
        	recessiveCandidates.get(rand.nextInt(recessiveCandidates.size()));
        List<TreeEntry> possibleChildren = new ArrayList<>();
        for (TreeEntry child : chosenParent2.getChildren()) {
			if (child.getContent().getArgumentsNumber()==preferredArgNum) {
    			possibleChildren.add(child);
    		}
        }

        TreeEntry recombinationEntry2 = 
        	possibleChildren.get(rand.nextInt(possibleChildren.size()));
        TreeEntry temp = recombinationEntry1;

        chosenParent1.getChildren().set(chosenParent1.getChildren().
        	indexOf(recombinationEntry1), recombinationEntry2);
        chosenParent2.getChildren().set(chosenParent2.getChildren().
        	indexOf(recombinationEntry2), temp);

        // TODO choose 2 strongest from offsprings and parents
        ProgramTree[] result = {offspring1, offspring2};
        return result;
    }

    public ProgramTree mutate(List<FunctionalElement> fSet, 
        List<Variable> vSet) {

        ProgramTree mutant = this.cloneTree();
        List<TreeEntry> parentals = mutant.getRoot().getParentalEntries();

        TreeEntry chosenParent = parentals.get(rand.nextInt(parentals.size()));
        int childrenNum = chosenParent.getChildren().size();
        int indexToMutate = rand.nextInt(childrenNum+1);

        // mutating parent element
        if (indexToMutate == childrenNum) {

            int argNum = chosenParent.getContent().getArgumentsNumber();
            List<FunctionalElement> possibleSubstitutes = new ArrayList<>();
            for (FunctionalElement elem : fSet) {
                if (elem.getArgumentsNumber() == argNum && 
                    elem != chosenParent.getContent()) {

                    possibleSubstitutes.add(elem);
                }
            }

            if (!possibleSubstitutes.isEmpty())
                chosenParent.setContent(possibleSubstitutes.get(rand.nextInt(
                    possibleSubstitutes.size())));
        }

        else {

            TreeEntry chosenChild = 
                chosenParent.getChildren().get(indexToMutate);
            int argNum = chosenChild.getContent().getArgumentsNumber();

            // `chosenChild`'s content is a Variable object
            if (argNum == 0) {
                chosenChild.setContent(vSet.get(rand.nextInt(vSet.size())));
            }

            else {

                List<FunctionalElement> possibleSubstitutes = new ArrayList<>();
                for (FunctionalElement elem : fSet) {
                    if (elem.getArgumentsNumber() == argNum && 
                        elem != chosenChild.getContent()) {

                        possibleSubstitutes.add(elem);
                    }
                }

                if (!possibleSubstitutes.isEmpty())
                    chosenChild.setContent(possibleSubstitutes.get(
                        rand.nextInt(possibleSubstitutes.size())));
            }
        }

        return mutant;
    }
}


public class FunctionGenetic {

    private static Random rand = new Random();


    private static List<FunctionalElement> prepareFunctionalElements(int n) {

        List<FunctionalElement> output = new ArrayList<>();
        int threshold = FunctionalElement.powIndex;

        for (int i = 1; i < n+1; i++) {

            FunctionalElement elem;

            if (i >= threshold) {
                elem = new FunctionalElement(threshold, i - threshold + 2);
            }

            else {
                elem = new FunctionalElement(i, 0);
            }

            output.add(elem);

            if (FunctionalElement.verboseMode)
            	System.out.println("Added " + elem + " to funtional set");
        }

        return output;
    }

    private static void initVariables(List<Variable> variables) {

        double rangeLength = 2;
        double from = -1;

        for (Variable each : variables) {
            each.setValue(rand.nextDouble()*rangeLength + from);
        }
    }

    private static List<Variable> prepareVariables(int n) {

        List<Variable> output = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            output.add(new Variable(0));
        }

        initVariables(output);
        return output;
    }

    private static double getInvestigatedValue(List<Variable> variables) {

        double sum = 0.0;
        int power = 2;
        for (Variable each : variables) {
            sum += Math.pow(Math.abs(each.getData(null)), power++);
        }

        return sum;
    }

    private static void generateDotFile(String filename, ProgramTree tree) {

        File file = new File(filename);
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {

            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            bw.write("digraph G {");
            bw.newLine();
            bw.newLine();
            tree.getRoot().writeDot(-1, bw);
            bw.write("}");
            bw.newLine();
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

    private static ProgramTree generateTree(
    	int depth, 
    	boolean fullInitialization,
    	List<FunctionalElement> functionalElems, 
    	List<Variable> variableElems) {

    	double funcElemProbability = fullInitialization? 1.0: 0.5;
    	FunctionalElement rootContent = 
    		functionalElems.get(rand.nextInt(functionalElems.size()));
    	TreeEntry root = new TreeEntry(rootContent);
    	List<TreeEntry> parents = Arrays.asList(root);

		for (int i = 0; i < depth; i++) {

			if (parents.isEmpty()) { break; }

			List<TreeEntry> newParents = new ArrayList<>();
			for (TreeEntry parent : parents) {

				int childrenNum = parent.getContent().getArgumentsNumber();
				for (int j = 0; j < childrenNum; j++) {
					
					boolean isFunctional;
					EntryContent ec;
					if (i == depth-1) {

						ec = variableElems.get(
							rand.nextInt(variableElems.size()));
						isFunctional = false;
					}

					else {
						if (rand.nextDouble() < funcElemProbability) {
							ec = functionalElems.get(
								rand.nextInt(functionalElems.size()));
							isFunctional = true;
						}

						else {
							ec = variableElems.get(
								rand.nextInt(variableElems.size()));
							isFunctional = false;
						}
					}

					TreeEntry child = new TreeEntry(ec);
					parent.addChild(child);

					if (isFunctional)
						newParents.add(child);
				}
			}

			parents = newParents;
		}

    	return new ProgramTree(root);
    }

    private static List<ProgramTree> generatePopulation(
    	int n, 
    	int maxDepth,
    	List<FunctionalElement> functionalElems, 
    	List<Variable> variableElems) {

    	List<ProgramTree> trees = new ArrayList<>();

    	if (n < 1) {
    		System.err.println("generatePopulation: n must be >= 1");
    		return trees;
    	}

    	if (maxDepth < 1) {
    		System.err.println("generatePopulation: maxDepth must be >= 1");
    		return trees;
    	}

    	// defines percentage of certain depth trees for applying
    	// grow initialization mechanism
    	double growMethodFraction = 0.6;
    	for (int i = 0; i < maxDepth; i++) {

    		int treesByDepth = (i == maxDepth-1)? 
    			(n - i*n/maxDepth): 
    			(n/maxDepth);
    		int fullInitTrees = 
    			(int) (treesByDepth - treesByDepth*growMethodFraction);

    		System.out.println("treesByDepth: " + treesByDepth + 
    			" fullInitTrees: " + fullInitTrees);

    		for (int j = 0; j < treesByDepth; j++) {

    			ProgramTree tree;
    			if (j < fullInitTrees) {
    				tree = generateTree(i+1, true, 
    					functionalElems, variableElems);
    			}

    			else {
    				tree = generateTree(i+1, false, 
    					functionalElems, variableElems);
    			}

    			trees.add(tree);
    			generateDotFile("data/initial/tree_" + i + "_" + j + ".dot", 
                    tree);
    		}
    	}

    	return trees;
    }

    private static List<Double> prepareRouletteMarkup(
        List<ProgramTree> population) {

        List<Double> probabilities = new ArrayList<>();
        double sum = 0;
        for (ProgramTree tree : population) {

            double fitness = tree.getFitnessValue();
            probabilities.add(fitness);
            sum += fitness;
        }

        // after this for loop, `probabilities` will really contain values
        // that sums to 1
        for (int i = 0; i < probabilities.size(); i++) {

            double old = probabilities.get(i);
            probabilities.set(i, old/sum);
        }

        List<Double> markupPoints = new ArrayList<>();
        double accumulator = 0;
        for (Double prob : probabilities) {

            accumulator += prob;
            markupPoints.add(accumulator);
        }

        return markupPoints;
    }

    private static int getRouletteInterval(List<Double> markupPoints) {

        double randomValue = rand.nextDouble(), 
            from = 0;

        for (int i = 0; i < markupPoints.size(); i++) {

            if (randomValue > from && 
                randomValue < markupPoints.get(i)) {

                return i;
            }

            from = markupPoints.get(i);
        }

        // for exclusive scenarios
        System.err.println("getRouletteInterval: not found exact index");
        return rand.nextInt(markupPoints.size());
    }

    public static List<ProgramTree> doEvolutionStep(
        List<ProgramTree> population, 
        List<FunctionalElement> fSet, 
        List<Variable> vSet) {
    	
        List<Double> markupPoints = prepareRouletteMarkup(population);
        List<ProgramTree> intermediatePopulation = new ArrayList<>();

        // crossing
        for (int pairNum = 0; pairNum < population.size()/2; pairNum++) {
            
            int parentId1 = getRouletteInterval(markupPoints);
            int parentId2 = parentId1;

            // choosing some another tree in population
            while (parentId2 == parentId1) {
                parentId2 = getRouletteInterval(markupPoints);
            }

            ProgramTree parent1 = population.get(parentId1);
            ProgramTree parent2 = population.get(parentId2);
            ProgramTree[] offsprings = parent1.crossWith(parent2);
            for (int i = 0; i < offsprings.length; i++) {
                intermediatePopulation.add(offsprings[i]);
            }
        }

        double mutationProbability = 0.05;
        for (ProgramTree each : intermediatePopulation) {
            if (rand.nextDouble() < mutationProbability) {
                each = each.mutate(fSet, vSet);
            }
        }

        return intermediatePopulation;
    }

    public static void main(String[] args) {

        List<FunctionalElement> fSet = prepareFunctionalElements(16);
        List<Variable> vSet = prepareVariables(8);

        ProgramTree.setTargetValue(getInvestigatedValue(vSet));
        List<ProgramTree> population = generatePopulation(40, 9, fSet, vSet);

        for (ProgramTree each : population) {
            System.out.println("Fitness: " + each.getFitnessValue());
        }

        int epochNumber = 10;
        for (int i = 0; i < epochNumber; i++) {

        	population = doEvolutionStep(population, fSet, vSet);
            System.out.println("---- iteration " + i + " ----");
            for (ProgramTree each : population) {
                System.out.println("Fitness: " + each.getFitnessValue());
            }

            if (i == epochNumber-1) {
                for (int j = 0; j < population.size(); j++) {
                    generateDotFile("data/tree" + j + ".dot", 
                        population.get(j));
                }
            }
        }

        TreeEntry var0 = new TreeEntry(vSet.get(0));
        TreeEntry var1 = new TreeEntry(vSet.get(1));
        TreeEntry var2 = new TreeEntry(vSet.get(2));
        TreeEntry var3 = new TreeEntry(vSet.get(3));
        TreeEntry var4 = new TreeEntry(vSet.get(4));
        TreeEntry var5 = new TreeEntry(vSet.get(5));
        TreeEntry var6 = new TreeEntry(vSet.get(6));
        TreeEntry var7 = new TreeEntry(vSet.get(7));

        TreeEntry rootEntry = new TreeEntry(fSet.get(0));
        TreeEntry plusL = new TreeEntry(fSet.get(0));
        TreeEntry plusR = new TreeEntry(fSet.get(0));
        TreeEntry plusLL = new TreeEntry(fSet.get(0));
        TreeEntry plusLR = new TreeEntry(fSet.get(0));
        TreeEntry plusRL = new TreeEntry(fSet.get(0));
        TreeEntry plusRR = new TreeEntry(fSet.get(0));
        TreeEntry abs0 = new TreeEntry(fSet.get(4));
        TreeEntry abs1 = new TreeEntry(fSet.get(4));
        TreeEntry abs2 = new TreeEntry(fSet.get(4));
        TreeEntry abs3 = new TreeEntry(fSet.get(4));
        TreeEntry abs4 = new TreeEntry(fSet.get(4));
        TreeEntry abs5 = new TreeEntry(fSet.get(4));
        TreeEntry abs6 = new TreeEntry(fSet.get(4));
        TreeEntry abs7 = new TreeEntry(fSet.get(4));
        TreeEntry pow0 = new TreeEntry(fSet.get(8));
        TreeEntry pow1 = new TreeEntry(fSet.get(9));
        TreeEntry pow2 = new TreeEntry(fSet.get(10));
        TreeEntry pow3 = new TreeEntry(fSet.get(11));
        TreeEntry pow4 = new TreeEntry(fSet.get(12));
        TreeEntry pow5 = new TreeEntry(fSet.get(13));
        TreeEntry pow6 = new TreeEntry(fSet.get(14));
        TreeEntry pow7 = new TreeEntry(fSet.get(15));

        rootEntry.addChild(plusL);
        rootEntry.addChild(plusR);
        plusL.addChild(plusLL);
        plusL.addChild(plusLR);
        plusR.addChild(plusRL);
        plusR.addChild(plusRR);
        plusLL.addChild(abs0);
        plusLL.addChild(abs1);
        plusLR.addChild(abs2);
        plusLR.addChild(abs3);
        plusRL.addChild(abs4);
        plusRL.addChild(abs5);
        plusRR.addChild(abs6);
        plusRR.addChild(abs7);
        abs0.addChild(pow0);
        abs1.addChild(pow1);
        abs2.addChild(pow2);
        abs3.addChild(pow3);
        abs4.addChild(pow4);
        abs5.addChild(pow5);
        abs6.addChild(pow6);
        abs7.addChild(pow7);
        pow0.addChild(var0);
        pow1.addChild(var1);
        pow2.addChild(var2);
        pow3.addChild(var3);
        pow4.addChild(var4);
        pow5.addChild(var5);
        pow6.addChild(var6);
        pow7.addChild(var7);

        ProgramTree solution = new ProgramTree(rootEntry);
        generateDotFile("data/solution.dot", solution);
    }
}