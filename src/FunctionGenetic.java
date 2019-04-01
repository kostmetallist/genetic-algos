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

    private static List<Variable> prepareVariables(int n) {

        List<Variable> output = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            output.add(new Variable(0));
        }

        return output;
    }

    private static void initVariables(List<Variable> variables) {

        double rangeLength = 3;
        double from = -1.5;

        for (Variable each : variables) {
            each.setValue(rand.nextDouble()*rangeLength + from);
        }
    }

    private static void generateDotFile(String filename, TreeEntry root) {

        File file = new File(filename);
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {

            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            bw.write("digraph G {");
            bw.newLine();
            bw.newLine();
            root.writeDot(-1, bw);
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

    private static TreeEntry generateTree(
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

    	return root;
    }

    private static List<TreeEntry> generatePopulation(
    	int n, 
    	int maxDepth,
    	List<FunctionalElement> functionalElems, 
    	List<Variable> variableElems) {

    	List<TreeEntry> roots = new ArrayList<>();

    	if (n < 1) {
    		System.err.println("generatePopulation: n must be >= 1");
    		return roots;
    	}

    	if (maxDepth < 1) {
    		System.err.println("generatePopulation: maxDepth must be >= 1");
    		return roots;
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

    			TreeEntry root;
    			if (j < fullInitTrees) {
    				root = generateTree(i+1, true, 
    					functionalElems, variableElems);
    			}

    			else {
    				root = generateTree(i+1, false, 
    					functionalElems, variableElems);
    			}

    			roots.add(root);
    			generateDotFile("data/tree_" + i + "_" + j + ".dot", root);
    		}
    	}

    	return roots;
    }

    public static void main(String[] args) {

        // FunctionalElement rootElem = new FunctionalElement("+");
        // FunctionalElement absLeft = new FunctionalElement("abs");
        // FunctionalElement multRight = new FunctionalElement("*");
        // FunctionalElement diff = new FunctionalElement("-");
        // Variable x1 = new Variable(5);
        // Variable x2 = new Variable(7);
        // Variable x3 = new Variable(8);
        // Variable x4 = new Variable(3);

        // TreeEntry root = new TreeEntry(rootElem);
        // TreeEntry left = new TreeEntry(absLeft);
        // TreeEntry right = new TreeEntry(multRight);
        // root.addChild(left);
        // root.addChild(right);

        // TreeEntry diffEntry = new TreeEntry(diff);
        // left.addChild(diffEntry);

        // TreeEntry diffA = new TreeEntry(x1);
        // TreeEntry diffB = new TreeEntry(x2);
        // diffEntry.addChild(diffA);
        // diffEntry.addChild(diffB);

        // TreeEntry multA = new TreeEntry(x3);
        // TreeEntry multB = new TreeEntry(x4);
        // right.addChild(multA);
        // right.addChild(multB);

        // // FunctionalElement.verboseMode = true;

        // generateDotFile("data/tree.dot", root);
        // System.out.println(root.getData());

        List<FunctionalElement> fSet = prepareFunctionalElements(16);
        List<Variable> vSet = prepareVariables(8);

        List<TreeEntry> population = generatePopulation(30, 6, fSet, vSet);
    }
}