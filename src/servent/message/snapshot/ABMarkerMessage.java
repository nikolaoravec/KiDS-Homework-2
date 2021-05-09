package servent.message.snapshot;

import java.util.Map;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABMarkerMessage extends BasicMessage {

	private static final long serialVersionUID = -3114137381491356339L;
	
	private Map<Integer, Integer> senderVectorClock;

	public ABMarkerMessage(ServentInfo sender, ServentInfo receiver, Map<Integer, Integer> senderVectorClock) {
		super(MessageType.AB_MARKER, sender, receiver);
		
		this.senderVectorClock = senderVectorClock;
	}
	
	public Map<Integer, Integer> getSenderVectorClock() {
		return senderVectorClock;
	}
}
