package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.SnapshotCollectorWorker;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class AVTerminateHandler implements MessageHandler {

	private Message clientMessage;
	private SnapshotCollector snapshotCollector;
	
	public AVTerminateHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.snapshotCollector = snapshotCollector;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.AV_TERMINATE) {
			SnapshotCollectorWorker snapshotCollectorWorker = (SnapshotCollectorWorker) snapshotCollector;
			AVBitcakeManager avBitcakeManager = (AVBitcakeManager) snapshotCollectorWorker.getBitcakeManager();
			
			snapshotCollectorWorker.addChannelMessages(AppConfig.myServentInfo.getId(), avBitcakeManager.getAllChannelTransactions());
		
		} else {
			AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
		}

	}
}

