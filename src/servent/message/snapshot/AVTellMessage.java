package servent.message.snapshot;

import java.util.List;
import java.util.Map;

import app.ServentInfo;
import app.snapshot_bitcake.AVSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

public class AVTellMessage extends BasicMessage {

	protected AVTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, List<ServentInfo> routeList, String messageText, int messageId,
			Map<Integer, Integer> vectorClock) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, routeList, messageText, messageId, vectorClock);
		// TODO Auto-generated constructor stub
	}


	private static final long serialVersionUID = 3116394054726162318L;

	private AVSnapshotResult avSnapshotResult;
	

	public AVSnapshotResult getAVSnapshotResult() {
		return avSnapshotResult;
	}
	
	
}
