/*
 * Information about neighbors 
 */
public class NeighborInfo {
	
	public String dstIP;
	public int dstPort;
	public double weight;
	public boolean isSelf = false;
	public boolean isAlive = true;
	
	public NeighborInfo(String dstIP, int dstPort, double weight, boolean isSelf) {
		this.dstIP = dstIP;
		this.dstPort = dstPort;
		this.weight = weight;
		this.isSelf = isSelf;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.dstIP + ":" + Integer.toString(this.dstPort) ;
	}
	
	
	
}
