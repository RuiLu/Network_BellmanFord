import java.net.*;
import java.nio.ByteBuffer;

public class MyProtocol
{
	private final static int CHECKSUM_INDEX = 17;
	private final int MAX_PAKCET_SIZE = 60 * 1024;
	
	private byte[] customizedPacket = null;
	private byte[] rcvData = null;
	private byte[] sendData = null;
	private InetAddress sendAddr = null;
	private int sendPort = 0;
	private InetAddress rcvAddr = null;
	private int rcvPort = 0;
	private int segmentID = 0;
	private int isFin = 0;
	private int dataLength = 0;
	
	public MyProtocol() {
		
	}
	
	public MyProtocol(byte[] originalData) {
		try {
			byte[] sendAddrByte = new byte[4];
			byte[] sendPortByte = new byte[4];
			byte[] rcvAddrByte = new byte[4];
			byte[] rcvPortByte = new byte[4];
			byte[] flagsByte = new byte[4];
			byte[] dataLengthByte = new byte[4];
			
			System.arraycopy(originalData, 0, sendAddrByte, 0, 4);
			System.arraycopy(originalData, 4, sendPortByte, 0, 4);
			System.arraycopy(originalData, 8, rcvAddrByte, 0, 4);
			System.arraycopy(originalData, 12, rcvPortByte, 0, 4);
			System.arraycopy(originalData, 16, flagsByte, 0, 4);
			System.arraycopy(originalData, 20, dataLengthByte, 0, 4);
			
			sendAddr = InetAddress.getByAddress(sendAddrByte);
			sendPort = ByteBuffer.wrap(sendPortByte).getInt();
			rcvAddr = InetAddress.getByAddress(rcvAddrByte);
			rcvPort = ByteBuffer.wrap(rcvPortByte).getInt();
			segmentID = flagsByte[0];
			isFin = flagsByte[2];
			dataLength = ByteBuffer.wrap(dataLengthByte).getInt();
			
			if (dataLength > 0) {
				rcvData = new byte[dataLength];
				System.arraycopy(originalData, 24, rcvData, 0, dataLength);
			} else {
				System.out.println("with no data");
				rcvData = null;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("fail to parse the packet");
		}
	}
	
	public byte[] constructPacket() {
		
		byte[] sendPortByte = ByteBuffer.allocate(4).putInt(sendPort).array();
		byte[] rcvPortByte = ByteBuffer.allocate(4).putInt(rcvPort).array();
		/*
		 * 17 for the checksum, 19 unused 
		 */
		byte[] flagsByte = {(byte)segmentID, (byte)0x00, (byte)isFin, (byte)0x00};
		
		byte[] dataLengthByte;
		
		if (sendData != null) {
			dataLengthByte = ByteBuffer.allocate(4).putInt(sendData.length).array();
		} else {
			dataLengthByte = ByteBuffer.allocate(4).putInt(0x00000000).array();
		}
		
		customizedPacket = new byte[MAX_PAKCET_SIZE];
		
		System.arraycopy(sendPortByte, 0, customizedPacket, 4, sendPortByte.length);
		System.arraycopy(rcvPortByte, 0, customizedPacket, 12, rcvPortByte.length);
		System.arraycopy(flagsByte, 0, customizedPacket, 16, flagsByte.length);
		System.arraycopy(dataLengthByte, 0, customizedPacket, 20, dataLengthByte.length);
		
		if (sendData != null) {
			System.arraycopy(sendData, 0, customizedPacket, 24, sendData.length);
		}
		
		/*
		 * compute the checksum here
		 */
		customizedPacket[CHECKSUM_INDEX] = ComputeCheckSum(customizedPacket);
		
		
		return customizedPacket;
	}
	
	public static byte ComputeCheckSum(byte[] customizedPacket) {
		byte checkSum = 0x00;
		
		for (int i = 0; i < customizedPacket.length; i++) {
			if (i == CHECKSUM_INDEX) {
				checkSum ^= 0x00;
			} else {
				checkSum ^= customizedPacket[i];
			}
		}
		
		return checkSum;
	}
	
	public static boolean CheckCheckSum(DatagramPacket datagramPacket) {
		
		byte[] packet = datagramPacket.getData();
		
		byte checkSum = ComputeCheckSum(packet);
		
		if (checkSum == packet[CHECKSUM_INDEX]) {
			return true;
		} else {
			return false;
		}
		
	}

	public byte[] getRcvData() {
		return rcvData;
	}

	public void setRcvData(byte[] rcvData) {
		this.rcvData = rcvData;
	}

	public byte[] getSendData() {
		return sendData;
	}

	public void setSendData(byte[] data) {
		dataLength = data.length;
		sendData = new byte[dataLength];
		System.arraycopy(data, 0, sendData, 0, dataLength);
	}

	public InetAddress getSendAddr() {
		return sendAddr;
	}

	public void setSendAddr(InetAddress sendAddr) {
		this.sendAddr = sendAddr;
	}

	public int getSendPort() {
		return sendPort;
	}

	public void setSendPort(int sendPort) {
		this.sendPort = sendPort;
	}

	public InetAddress getRcvAddr() {
		return rcvAddr;
	}

	public void setRcvAddr(InetAddress rcvAddr) {
		this.rcvAddr = rcvAddr;
	}

	public int getRcvPort() {
		return rcvPort;
	}

	public void setRcvPort(int rcvPort) {
		this.rcvPort = rcvPort;
	}

	public int getSegmentID() {
		return segmentID;
	}

	public void setSegmentID(int segmentID) {
		this.segmentID = segmentID;
	}

	public void unsetFin() {
		this.isFin = 0;
	}

	public void setFin() {
		this.isFin = 1;
	}
	
}
