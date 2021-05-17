package servent.message.snapshot;

import java.util.Map;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class AVMarkerMessage extends BasicMessage {

	private static final long serialVersionUID = 388942509576636228L;
	
	public AVMarkerMessage(MessageType messageType ,ServentInfo sender, ServentInfo receiver, ServentInfo target, Map<Integer, Integer> vectorClock) {
		super(MessageType.AV_MARKER, sender, receiver, target,vectorClock);
	}
	
}
