target:
	mkdir -p classes
	javac ./src/MyProtocol.java -d ./classes
	javac ./src/NeighborInfo.java -d ./classes
	javac ./src/RoutingInfo.java -d ./classes
	javac ./src/NeighborState.java -d ./classes -classpath ./classes
	javac ./src/RoutingTable.java -d ./classes -classpath ./classes
	javac ./src/DistanceVector.java -d ./classes -classpath ./classes
	javac ./src/bfclient.java -d ./classes -classpath ./classes

clean:
	rm -f ./classes/MyProtocol.class
	rm -f ./classes/NeighborInfo.class
	rm -f ./classes/RoutingInfo.class
	rm -f ./classes/NeighborState.class
	rm -f ./classes/DistanceVector.class
	rm -f ./classes/RoutingTable.class
	rm -f ./classes/bfclient.class
	rm -rf ./classes