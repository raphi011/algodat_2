package ads2.ss14.etsppc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores instance data for euclidean TSP with precedence constraints
 */
public class ETSPPCInstance {
	private List<PrecedenceConstraint> constraints;
	private Map<Integer, Location> allLocations;
	private double threshold;

	public ETSPPCInstance(Map<Integer, Location> allCities, List<PrecedenceConstraint> constraints, double threshold) {
		this.allLocations = allCities;
		this.constraints = constraints;
		this.threshold = threshold;
	}
	
	public ETSPPCInstance(ETSPPCInstance other) {
		allLocations = new HashMap<Integer, Location>(other.allLocations.size());
		for(Integer id : other.allLocations.keySet()) {
			allLocations.put(id, new Location(other.allLocations.get(id)));
		}
		
		constraints = new ArrayList<PrecedenceConstraint>(other.constraints.size());
		for(PrecedenceConstraint constraint : other.constraints) {
			constraints.add(new PrecedenceConstraint(constraint));
		}
		
		threshold = other.threshold;
	}

	/**
	 * @return the list of precedence constraints
	 */
	public List<PrecedenceConstraint> getConstraints() {
		return constraints;
	}

	/**
	 * @return a mapping of the Location's ID to the Location
	 */
	public Map<Integer, Location> getAllLocations() {
		return allLocations;
	}

	public double getThreshold() {
		return threshold;
	}
}
