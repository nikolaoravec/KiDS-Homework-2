package servent.handler.snapshot;

import app.CausalBroadcastShared;
import app.CausalShared;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class ABMarkerHandler implements MessageHandler {

	private Message clientMessage;
	private ABBitcakeManager bitcakeManager;
	private SnapshotCollector snapshotCollector;

	public ABMarkerHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.bitcakeManager = (ABBitcakeManager)snapshotCollector.getBitcakeManager();
		this.snapshotCollector = snapshotCollector;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.AB_MARKER) {
//			bitcakeManager.handleMarker(clientMessage, snapshotCollector);	
			CausalShared.addPendingMessage(clientMessage);
			CausalShared.checkPendingMessages();
		}

	}

}
