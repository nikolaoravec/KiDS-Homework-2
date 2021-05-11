package servent.message.snapshot;

import java.util.List;
import java.util.Map;

import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABTellMessage extends BasicMessage {

	protected ABTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, List<ServentInfo> routeList, String messageText, int messageId,
			Map<Integer, Integer> vectorClock) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, routeList, messageText, messageId, vectorClock);
		// TODO Auto-generated constructor stub
	}



	private static final long serialVersionUID = 8224274653159843559L;

	private ABSnapshotResult clSnapshotResult;
	
	

	public ABSnapshotResult getCLSnapshotResult() {
		return clSnapshotResult;
	}
	
}
