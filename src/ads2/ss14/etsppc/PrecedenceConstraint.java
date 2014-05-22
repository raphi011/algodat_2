package ads2.ss14.etsppc;

public class PrecedenceConstraint {
	private int first;
	private int second;
	
	public PrecedenceConstraint(int first, int second) {
		this.first = first;
		this.second = second;
	}
	
	public PrecedenceConstraint(PrecedenceConstraint other) {
		first = other.first;
		second = other.second;
	}
	
	public int getFirst() {
		return first;
	}
	
	public int getSecond() {
		return second;
	}
}
