import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes and prints out the probability of any combination of events given any other combination of events
 * for a Bayesian network defined in method initializeData().
 * @author gautamshetty
 */
public class bnet {
	
	/**
	 * Variables or nodes in the bayesian network.
	 */
	List variables = new ArrayList();
	
	/**
	 * Links between nodes in the bayesian network.
	 */
	Map parentMap = new HashMap();
	
	/**
	 * CPT values at various nodes in the bayesian network. 
	 */
	Map probabilityMap = new HashMap();
	
	/**
	 * Initialize the bayesian network given and initialize the probabilities at the nodes.
	 */
	private void initializeData() {
		
		variables.add("B");
		variables.add("E");
		variables.add("A");
		variables.add("J");
		variables.add("M");
		
		parentMap.put("M", "A");
		parentMap.put("J", "A");
		parentMap.put("A", "B-E");
		
		probabilityMap.put("Bt", new Double(0.001));
		probabilityMap.put("Bf", new Double(0.999));
		probabilityMap.put("Et", new Double(0.002));
		probabilityMap.put("Ef", new Double(0.998));
		probabilityMap.put("At|Bt-Et", new Double(0.95));
		probabilityMap.put("Af|Bt-Et", new Double(0.05));
		probabilityMap.put("At|Bt-Ef", new Double(0.94));
		probabilityMap.put("Af|Bt-Ef", new Double(0.06));
		probabilityMap.put("At|Bf-Et", new Double(0.29));
		probabilityMap.put("Af|Bf-Et", new Double(0.71));
		probabilityMap.put("At|Bf-Ef", new Double(0.001));
		probabilityMap.put("Af|Bf-Ef", new Double(0.999));
		probabilityMap.put("Jt|At", new Double(0.9));
		probabilityMap.put("Jf|At", new Double(0.1));
		probabilityMap.put("Jt|Af", new Double(0.05));
		probabilityMap.put("Jf|Af", new Double(0.95));
		probabilityMap.put("Mt|At", new Double(0.7));
		probabilityMap.put("Mf|At", new Double(0.3));
		probabilityMap.put("Mt|Af", new Double(0.01));
		probabilityMap.put("Mf|Af", new Double(0.99));
	}
	
	/**
	 * Main method.
	 * @param args combination of events.
	 */
	public static void main(String[] args) {
		
		bnet b = new bnet();
		
		b.initializeData();
		
		double probability = 0.0;
		List argsList = Arrays.asList(args);
		if (argsList.contains("given")) {
			
			int givenIndex = argsList.indexOf("given");
			
			List numVariables = new ArrayList(argsList.subList(0, givenIndex));
			
			List denVariables = argsList.subList(givenIndex + 1, argsList.size());
			
			numVariables.addAll(denVariables);
			double numProbability = b.computeProbability(numVariables);
			
			double denProbability = b.computeProbability(denVariables);
			
			probability = numProbability / denProbability;
			
		} else {
			probability = b.computeProbability(Arrays.asList(args));
		}
		
		StringBuffer argsBuff = new StringBuffer(); 
		for (int i = 0;i < args.length; i++)
			argsBuff.append(args[i]).append(" ");
		
		System.out.format("P ( " + argsBuff.toString() + ") = %.15f\n", probability);
	}
	
	/**
	 * Forms the joint probability, adding the unobserved variables, for the combination of events input and 
	 * computes the probability of events.
	 * @param args combination of events.
	 * @return the probability.
	 */
	private double computeProbability(List args) {
		
		List jointDist = getJointProbability(args);
		
		return calculateProbability(jointDist);
	}
	
	/**
	 * Calculates the probabilities for all the joint probability distribution of the variables passed in the list.
	 * @param jointDist joint probability distribution of the variables
	 * @return the probability.
	 */
	private double calculateProbability(List jointDist) {
		
		List term = null;
		String var = null, parent = null;
		StringBuffer condProbVar = null;
		String [] parentToken = null;
		double probability = 0.0, termProbability = 1.0;
		for (int i = 0; i < jointDist.size(); i++) {
			
			term = (List) jointDist.get(i);
			termProbability = 1.0;
			for (int j = 0; j < term.size(); j++) {
				
				var = (String) term.get(j);
				parent= (String) parentMap.get(var.substring(0, 1));
				if (parent != null) {
					
					condProbVar = new StringBuffer(var).append("|");
					parentToken = parent.split("-");
					for (int k = 0; k < parentToken.length; k++) {
						
						if (term.contains(parentToken[k].substring(0, 1) + "t"))
							condProbVar.append(parentToken[k].substring(0, 1)).append("t-");
						else if (term.contains(parentToken[k].substring(0, 1) + "f"))
							condProbVar.append(parentToken[k].substring(0, 1)).append("f-");
					}
					
					var = condProbVar.substring(0, condProbVar.length()-1);
				} 
				
				termProbability *= (Double) probabilityMap.get(var);
			}
			
			probability += termProbability;
		}
		
		return probability;
	}
	
	/**
	 * Creates the joint probability distribution for the event variables by adding the unbserved variables 
	 * for all the combinations for unobserved variables.
	 * @param args combination of events input. 
	 * @return list of joint probability distributions for the events.
	 */
	private List getJointProbability(List args) {
		
		List argsVarList = new ArrayList();
		for (int i = 0; i < args.size(); i++) {
			argsVarList.add(((String) args.get(i)).substring(0, 1));
		}
		
		List vars = new ArrayList(variables);
		
		vars.removeAll(argsVarList);
		
		List jointDist = getUnObservedVariables(vars);
		
		for(int j = 0; j < args.size(); j++) {
			addToJointList(null, jointDist, (String) args.get(j));
		}
		
		return jointDist;
	}
	
	/**
	 * Adds the unobserved variables for the combination of events input.
	 * @param variables event variables input.
	 * @return list of joint probability distributions.
	 */
	private List getUnObservedVariables(List variables) {
		
		if (variables != null && variables.isEmpty())
			return new ArrayList();
		
		String [] val = {"t", "f"};
		String var = (String) variables.get(0);
		
		List tempJointDist = new ArrayList(), jointDist = new ArrayList();
		for (int i = 0; i < val.length; i++) {
			
			tempJointDist = getUnObservedVariables(variables.subList(1, variables.size()));
			
			addToJointList(jointDist, tempJointDist, var + val[i]);
			
		}
		
		return jointDist;
	}
	
	/**
	 * Adds the variable to the list of joint probability.
	 * @param jointDist list of joint probability.
	 * @param tempJointDist temporary list.
	 * @param variable variable to be added.s
	 */
	private void addToJointList(List jointDist, List tempJointDist, String variable) {
		
		List temp = null;
		if (tempJointDist.isEmpty()) {
			
			temp = new ArrayList();
			temp.add(variable);
			tempJointDist.add(temp);
		} else {
			
			for (int j = 0; j < tempJointDist.size(); j++) {
				temp = (List) tempJointDist.get(j);
				temp.add(variable);
			}
		}
		
		if (jointDist != null)
			jointDist.addAll(tempJointDist);
	}
	
}
