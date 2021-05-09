package servent.message.snapshot;

import java.util.Map;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class AVMarkerMessage extends BasicMessage {

	private static final long serialVersionUID = 388942509576636228L;
	private Map<Integer, Integer> senderVectorClock;

	public AVMarkerMessage(ServentInfo sender, ServentInfo receiver, int collectorId, Map<Integer, Integer> senderVectorClock) {
		super(MessageType.AV_MARKER, sender, receiver, String.valueOf(collectorId));
		this.senderVectorClock = senderVectorClock;
	}
	
	public Map<Integer, Integer> getSenderVectorClock() {
		return senderVectorClock;
	}
}
