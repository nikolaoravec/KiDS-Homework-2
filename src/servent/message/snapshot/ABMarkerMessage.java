package servent.message.snapshot;

import java.util.Map;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABMarkerMessage extends BasicMessage {

	private static final long serialVersionUID = -3114137381491356339L;

	public ABMarkerMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, ServentInfo targetInfo,
			Map<Integer, Integer> vectorClock) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, vectorClock);
	}
}
