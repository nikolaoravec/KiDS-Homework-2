package servent.message.snapshot;

import java.util.List;
import java.util.Map;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABMarkerMessage extends BasicMessage {

	protected ABMarkerMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, List<ServentInfo> routeList, String messageText, int messageId,
			Map<Integer, Integer> vectorClock) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, routeList, messageText, messageId, vectorClock);
		// TODO Auto-generated constructor stub
	}



	private static final long serialVersionUID = -3114137381491356339L;
	
	private Map<Integer, Integer> senderVectorClock;


	
	public Map<Integer, Integer> getSenderVectorClock() {
		return senderVectorClock;
	}
}
