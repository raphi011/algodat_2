package ads2.ss14.etsppc;

import java.util.List;

/**
 * Abstrakte Klasse zum Berechnen der Tour mittels Branch-and-Bound.
 * 
 * <p>
 * <b>WICHTIG:</b> Nehmen Sie keine &Auml;nderungen in dieser Klasse vor. Bei
 * der Abgabe werden diese &Auml;nderungen verworfen und es k&ouml;nnte dadurch
 * passieren, dass Ihr Programm somit nicht mehr korrekt funktioniert.
 * </p>
 */
public abstract class AbstractETSPPC implements Runnable {
	
	/** Die bisher beste L&ouml;sung */
	private BnBSolution bestBnBSolution;

	final public synchronized boolean setSolution(double newUpperBound, List<Location> newSolution) {
		if (bestBnBSolution == null || newUpperBound < bestBnBSolution.getUpperBound()) {
			bestBnBSolution = new BnBSolution(newUpperBound, newSolution);
			return true;
		}
		return false;
	}
	
	/**
	 * Gibt die bisher beste gefundene L&ouml;sung zur&uuml;ck.
	 * 
	 * @return Die bisher beste gefundene L&ouml;sung.
	 */
	final public BnBSolution getBestSolution() {
		return bestBnBSolution;
	}
	
	public final class BnBSolution {

		private double upperBound = -Integer.MAX_VALUE;
		private List<Location> bestTour;
		
		public BnBSolution(double newUpperBound, List<Location> newSolution) {
			upperBound = newUpperBound;
			bestTour = newSolution;
		}

		/**
		 * @return Die obere Schranke
		 */
		public double getUpperBound() {
			return upperBound;
		}

		/**
		 * @return Die Items der bisher besten L&ouml;sung
		 */
		public List<Location> getBestSolution() {
			return bestTour;
		}
		
	}

}
