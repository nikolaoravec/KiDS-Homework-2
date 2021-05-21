package servent.handler.snapshot;

import java.util.List;
import java.util.Map.Entry;

import app.AppConfig;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
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
			if (snapshotCollector.getBitcakeManager() instanceof AVBitcakeManager) {
				int sum = 0;
				AVBitcakeManager bitcakeManager = (AVBitcakeManager) snapshotCollector.getBitcakeManager();

				for (Entry<String, List<Integer>> channel : bitcakeManager.getAllChannelTransactions().entrySet()) {
					int sumOfChannel = 0;
					for (Integer val : channel.getValue()) {
						sumOfChannel += val;
					}
					sum += sumOfChannel;
					AppConfig.timestampedStandardPrint("Bitcake transactions before the marker for servent "
							+ AppConfig.myServentInfo.getId() + " from " + channel.getKey() + " is" + sumOfChannel);
				}

				AppConfig.timestampedStandardPrint("Sum of all bitcakes: " + sum);
				AVBitcakeManager.markerReceived.set(false);
			}
		} else {
			AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
		}
	}
}
