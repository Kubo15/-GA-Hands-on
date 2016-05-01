package grafAlgo.DB;

public class DBNode {
	private int name;
	private int neighbor;
	
	public DBNode(int name, int neighbor) {
		this.name = name;
		this.neighbor = neighbor;
	}
	public int getName() {
		return name;
	}
	public void setName(int name) {
		this.name = name;
	}
	public int getNeighbor() {
		return neighbor;
	}
	public void setNeighbor(int neighbor) {
		this.neighbor = neighbor;
	}
}
