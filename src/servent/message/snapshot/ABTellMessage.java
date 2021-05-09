package servent.message.snapshot;

import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class ABTellMessage extends BasicMessage {

	private static final long serialVersionUID = 8224274653159843559L;

	private ABSnapshotResult clSnapshotResult;
	
	public ABTellMessage(ServentInfo sender, ServentInfo receiver, ABSnapshotResult clSnapshotResult) {
		super(MessageType.AB_TELL, sender, receiver);
		
		this.clSnapshotResult = clSnapshotResult;
	}

	public ABSnapshotResult getCLSnapshotResult() {
		return clSnapshotResult;
	}
	
}
