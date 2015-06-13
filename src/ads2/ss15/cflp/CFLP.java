package ads2.ss15.cflp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse zum Berechnen der L&ouml;sung mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */
public class CFLP extends AbstractCFLP {

	CFLPInstance instance;
    int upperBound;


    /**
     * Contains sorted factoryDistances (smallest distance for a certain customer
     * Key = customerId
     * Value = KeyValue object ( Key = facility, Value = distance to  customer )
     */
    Map<Integer, KeyValue[]> sortedFactoryDistances;

	public CFLP(CFLPInstance instance) {
		this.instance = instance;
		this.


        sortFactoryDistances();



		upperBound = Integer.MAX_VALUE;
		//firstFitHeuristic();
	}

    private void sortFactoryDistances() {
        // get sorted factory distances from minimal to maximum
        sortedFactoryDistances = new HashMap<Integer, KeyValue[]>();

        for (int c = 0; c < instance.getNumCustomers(); c++) {
            KeyValue[] listToSort = new KeyValue[instance.getNumFacilities()];
            for (int f = 0; f < instance.getNumFacilities(); f++) {
                listToSort[f] = new KeyValue(f, instance.distance(f, c));
            }
            Arrays.sort(listToSort);
            sortedFactoryDistances.put(c, listToSort);
        }
    }

    class KeyValue implements Comparable<KeyValue> {

        int key;
        int value;

        public KeyValue(int key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(KeyValue that) {
            return Integer.compare(value, that.value);
        }
    }


	private void firstFitHeuristic() {
		Problem p = new Problem();

		// first fit algorithm
		int c;

		while ((c = p.getNextBiggestCustomer()) != -1) {
			for (int f = 0; f < instance.getNumFacilities(); f++) {
				if ((p.usedBandwidth[f] + instance.bandwidthOf(c)) <= instance.maxBandwidth &&
						p.usedSlots[f] < instance.maxCustomersFor(f)) {
					p.customerAssignments[c] = f;
					p.usedSlots[f]++;
					p.facilityStatus[f] = true;
					p.usedBandwidth[f] += instance.bandwidthOf(c);
					break;
				}
			}
		}

		Main.printDebug("Max bandwidth: " + instance.maxBandwidth);

		for (int f = 0; f < instance.getNumFacilities(); f++) {
			Main.printDebug("Used bandwidth of facility " + f + ":" + p.usedBandwidth[f]);
		}
		setSolution(p.getLowerBound(), p.customerAssignments);
	}

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verf&uuml;gung gestellt um eine g&uuml;ltige L&ouml;sung
	 * zu finden.
	 * 
	 * <p>
	 * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 */
	@Override
    public void run() {

        branchAndBound(new Problem());
    }

    private void branchAndBound(Problem p) {

        if (p.getLowerBound() > upperBound) {
            // partial solution can't get better than the best
            // existing solution
            return;
        }  else if (p.currentCustomer == instance.getNumCustomers()) {
            // we have a solution
			int calcUpperBound = instance.calcObjectiveValue(p.customerAssignments);
			// Main.printDebug("We have a new solution!");

			if (setSolution(calcUpperBound,p.customerAssignments)) {
				upperBound = calcUpperBound;
				Main.printDebug("We have a new upperbound: " + upperBound);
			}
        } else {
            // we're not done yet and have to branch

            // create a new branch for every facility
            for (int f = 0; f < instance.getNumFacilities(); f++) {
                int facilityNumber = sortedFactoryDistances.get(p.currentCustomer)[f].key;
                // branch factories with the smallest distance first
                if (p.canUseFacility(facilityNumber)) {
                    branchAndBound(p.subProblem(facilityNumber));
                }
            }
        }
    }

    class Problem {

        int[] usedBandwidth;
        boolean[] facilityStatus;
        int[] usedSlots;
        int[] customerAssignments;

        int currentCustomer = 0;

        private int upperBound;

		Problem() {
			usedBandwidth = new int[instance.getNumFacilities()];
			facilityStatus = new boolean[instance.getNumFacilities()];
			usedSlots = new int[instance.getNumFacilities()];
            customerAssignments = new int[instance.getNumCustomers()];
			Arrays.fill(customerAssignments, -1);
		}

		private Problem(Problem other) {
			this.usedBandwidth = Arrays.copyOf(other.usedBandwidth, other.usedBandwidth.length);
			this.facilityStatus =  Arrays.copyOf(other.facilityStatus, other.facilityStatus.length);
			this.usedSlots = Arrays.copyOf(other.usedSlots, other.usedSlots.length);
			this.customerAssignments = Arrays.copyOf(other.customerAssignments, other.customerAssignments.length);

			this.currentCustomer = other.currentCustomer;
			this.upperBound = other.upperBound;
		}

        boolean canUseFacility(int facilityId) {
            return  usedSlots[facilityId] < instance.maxCustomersFor(facilityId) &&
                    (usedBandwidth[facilityId] + instance.bandwidthOf(currentCustomer)) <= instance.maxBandwidth;
        }

        Problem subProblem(int nextFacility) {
            Problem subProblem = new Problem(this);
            subProblem.customerAssignments[currentCustomer] = nextFacility;
            subProblem.usedSlots[nextFacility]++;
            subProblem.facilityStatus[nextFacility] = true;
            subProblem.usedBandwidth[nextFacility] += instance.bandwidthOf(currentCustomer);

            subProblem.currentCustomer++;

            return subProblem;
        }

		int getNextBiggestCustomer() {
			int custId = -1;
			int bandWidth = 0;

			for (int c = 0; c < instance.getNumCustomers(); c++) {
				if (customerAssignments[c] == -1 && instance.bandwidthOf(c) > bandWidth) {
					custId = c;
					bandWidth = instance.bandwidthOf(c);
				}
			}

			return custId
		}

		int getLowerBound() {
			/* int cost = 0;
			for (int f = 0; f < instance.getNumFacilities(); f++) {
				if (facilityStatus[f]) {
					cost += instance.openingCostsFor(f);
				}
			}

			for (int c = 0; c < instance.getNumCustomers(); c++) {
				if (customerAssignments[c] != -1)
					cost += instance.distance(customerAssignments[c], c);
			}

			return cost; */

            return Integer.MIN_VALUE; // not implemented yet
		}
	}
}


