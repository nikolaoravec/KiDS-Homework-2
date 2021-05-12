package servent.message.snapshot;

import java.util.Map;

import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABTellMessage extends BasicMessage {

	private static final long serialVersionUID = 8224274653159843559L;
	private ABSnapshotResult abSnapshotResult;

	public ABTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, Map<Integer, Integer> vectorClock, ABSnapshotResult abSnapshotResult) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, vectorClock);
		
		this.abSnapshotResult = abSnapshotResult;
	}

	public ABSnapshotResult getABSnapshotResult() {
		return abSnapshotResult;
	}
	
}
