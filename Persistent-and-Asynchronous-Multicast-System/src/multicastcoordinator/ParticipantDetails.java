package multicastcoordinator;

import java.sql.Timestamp;
import java.util.HashMap;

public class ParticipantDetails {

	private String participantIP;
	private String participantPort;
	private Boolean connectionStatus;
	private HashMap<Timestamp,String> storedMessages;
	
	public String getParticipantIP() {
		return participantIP;
	}
	public void setParticipantIP(String participantIP) {
		this.participantIP = participantIP;
	}
	public String getParticipantPort() {
		return participantPort;
	}
	public void setParticipantPort(String participantPort) {
		this.participantPort = participantPort;
	}
	public Boolean getConnectionStatus() {
		return connectionStatus;
	}
	public void setConnectionStatus(Boolean connectionStatus) {
		this.connectionStatus = connectionStatus;
	}
	public HashMap<Timestamp, String> getStoredMessages() {
		return storedMessages;
	}
	public void setStoredMessages(HashMap<Timestamp, String> storedMessages) {
		this.storedMessages = storedMessages;
	}
	
	
	
	
}
