package servent.message.snapshot;

import java.util.List;

import app.ServentInfo;
import app.snapshot_bitcake.AVSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

public class AVTellMessage extends BasicMessage {

	private static final long serialVersionUID = 3116394054726162318L;

	private AVSnapshotResult avSnapshotResult;
	
	public AVTellMessage(ServentInfo sender, ServentInfo receiver, AVSnapshotResult lySnapshotResult) {
		super(MessageType.AV_TELL, sender, receiver);
		
		this.avSnapshotResult = lySnapshotResult;
	}
	
	private AVTellMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, 
			boolean white, List<ServentInfo> routeList, String messageText, int messageId,
			AVSnapshotResult lySnapshotResult) {
		super(messageType, sender, receiver, white, routeList, messageText, messageId);
		this.avSnapshotResult = lySnapshotResult;
	}

	public AVSnapshotResult getAVSnapshotResult() {
		return avSnapshotResult;
	}
	
	@Override
	public Message setRedColor() {
		Message toReturn = new AVTellMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(),
				false, getRoute(), getMessageText(), getMessageId(), getAVSnapshotResult());
		return toReturn;
	}
}
