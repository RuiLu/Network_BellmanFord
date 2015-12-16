
public class RoutingInfo {

	public String destinaton;
	public double cost;
	public String nextHop;
	public boolean needToBeShown;
	
	public RoutingInfo(String destination, double cost, String nextHop, boolean needToBeShown) {
		this.destinaton = destination;
		this.cost = cost;
		this.nextHop = nextHop;
		this.needToBeShown = needToBeShown;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Destination = " + this.destinaton + ", Cost = " + this.cost + ", Link = (" + this.nextHop + ")";
	}
	
	
}
