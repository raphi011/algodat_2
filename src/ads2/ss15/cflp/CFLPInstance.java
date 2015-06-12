package ads2.ss15.cflp;

import java.util.Arrays;

/**
 * Speichert Instanzdaten des Capacitated Facility Location Problems
 */
public class CFLPInstance {
	public final int maxBandwidth;
	public final int distanceCosts;
	
	public int[] openingCosts;
	public int[] maxCustomers;
	public int[] bandwidths;
	public int[][] distances;
	private final int threshold;

	
	public CFLPInstance(int threshold, int maxBandwidth, int[] maxCustomers, int distanceCosts, int[] openingCosts, int[] bandwidths, int[][] distances) {
		this.threshold = threshold;
		this.maxBandwidth = maxBandwidth;
		this.maxCustomers = maxCustomers;
		
		this.distanceCosts = distanceCosts;
		this.openingCosts = openingCosts;
		
		this.bandwidths = bandwidths;
		this.distances = distances;
	}
	
	public CFLPInstance(CFLPInstance other) {
		maxBandwidth = other.maxBandwidth;
		maxCustomers = other.maxCustomers.clone();
		
		distanceCosts = other.distanceCosts;
		
		openingCosts = other.openingCosts.clone();
		bandwidths = other.bandwidths.clone();
		distances = new int[maxCustomers.length][bandwidths.length];
		for(int i=0; i < maxCustomers.length; ++i) {
			distances[i] = other.distances[i].clone(); 
		}
		
		threshold = other.threshold;
	}

	public int getNumCustomers() {
		return bandwidths.length;
	}
	
	public int getNumFacilities() {
		return openingCosts.length;
	}

	/**
	 * @param customerIdx Der Index des Kunden
	 * @return Die vom Kunden geforderte Bandbreite 
	 */
	public int bandwidthOf(int customerIdx) {
		return bandwidths[customerIdx];
	}
	
	/**
	 * @param facilityIdx Der Index der Facility
	 * @param customerIdx Der Index des Kunden
	 * @return Die Distanz zwischen Facility und Kunde
	 */
	public int distance(int facilityIdx, int customerIdx) {
		return distances[facilityIdx][customerIdx];
	}
	
	/**
	 * @param facilityIdx Der Index der Facility
	 * @return Die einmalignen Kosten zur Eröffnung der Facility
	 */
	public int openingCostsFor(int facilityIdx) {
		return openingCosts[facilityIdx];
	}
	
	/**
	 * @param facilityIdx Der Index der Facility
	 * @return Die maximale Anzahl an Kunden für die Facility
	 */
	public int maxCustomersFor(int facilityIdx) {
		return maxCustomers[facilityIdx];
	}

	public int getThreshold() {
		return threshold;
	}
	
	/**
	 * @param solution Eine (Teil-)L&ouml;sung für das CFLP.
	 *                 Der Index des Array gibt den Kunden an, der Wert an dieser Position die zugeordnete Facility.
	 *                 Ist der Wert kleiner 0 dann wird der Kunde als noch nicht zugeordnet angesehen.
	 * @return Gibt den Zielfunktionswert der aktuellen (Teil-)L&ouml;sung zur&uuml;ck.; ignoriert Arraywerte kleiner 0
	 */
	public int calcObjectiveValue(int[] solution) {
		boolean[] openedFacilities = new boolean[getNumFacilities()];
		Arrays.fill(openedFacilities, false);
		
		if(solution.length != getNumCustomers())
			throw new RuntimeException("Problem beim Ermitteln des Zielfunktionswertes (zu wenige/zu viele Kunden)");
		
		int sumCosts = 0;
		for(int i=0; i < Math.min(solution.length, getNumCustomers()); ++i) {
			if(solution[i] < 0)
				continue;
			
			if(!openedFacilities[solution[i]]) {
				sumCosts += openingCosts[solution[i]];
				openedFacilities[solution[i]] = true;
			}
			sumCosts += distanceCosts * distance(solution[i], i);
		}
		
		return sumCosts;
	}
}
