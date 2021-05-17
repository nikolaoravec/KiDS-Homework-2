package servent.handler.snapshot;

import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class AVMarkerHandler implements MessageHandler {

	private Message clientMessage;
	private AVBitcakeManager bitcakeManager;
	private SnapshotCollector snapshotCollector;

	public AVMarkerHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.bitcakeManager = (AVBitcakeManager) snapshotCollector.getBitcakeManager();
		this.snapshotCollector = snapshotCollector;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.AV_MARKER) {
			if (snapshotCollector.getBitcakeManager() instanceof AVBitcakeManager) {
				bitcakeManager = (AVBitcakeManager) snapshotCollector.getBitcakeManager();
				bitcakeManager.handleMarker(clientMessage, snapshotCollector);
			}
		}
	}

}
