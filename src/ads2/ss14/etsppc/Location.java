package ads2.ss14.etsppc;

public class Location {
	private int id;
	private double x;
	private double y;
	
	public Location(int id, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public Location(Location other) {
		this.id = other.id;
		this.x = other.x;
		this.y = other.y;
	}

	public int getCityId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double distanceTo(Location other) {
		return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
	}
	
	@Override
	public String toString() {
		return "Location " + String.valueOf(id);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() == Location.class) {
			Location other = (Location) obj;
			return id == other.id && x == other.x && y == other.y;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id + (int) x + (int) y;
	}
}
