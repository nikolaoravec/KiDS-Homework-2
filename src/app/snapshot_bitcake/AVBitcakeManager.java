package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import app.AppConfig;
import app.CausalShared;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.AVMarkerMessage;
import servent.message.snapshot.AVTellMessage;
import servent.message.snapshot.AVTerminateMessage;
import servent.message.util.MessageUtil;

public class AVBitcakeManager implements BitcakeManager, Serializable {

	private static final long serialVersionUID = -4655998658015758438L;

	private final AtomicInteger currentAmount = new AtomicInteger(1000);
	private Map<String, List<Integer>> allChannelTransactions = new ConcurrentHashMap<>();
	private Object allChannelTransactionsLock = new Object();
	public static AtomicBoolean markerReceived = new AtomicBoolean(false);
	public static Map<Integer, Integer> vectorClockForMarker = new ConcurrentHashMap<>();

	public static void setVectorClockForMarker(Map<Integer, Integer> vectorClockForMarker) {
		AVBitcakeManager.vectorClockForMarker = vectorClockForMarker;
	}

	public static Map<Integer, Integer> getVectorClockForMarker() {
		return vectorClockForMarker;
	}

	public static AtomicBoolean getMarkerReceived() {
		return markerReceived;
	}

	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);
	}

	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);
	}

	public int getCurrentBitcakeAmount() {
		return currentAmount.get();
	}

	public void markerEvent(int collectorId, SnapshotCollectorWorker snapshotCollectorWorker) {
		synchronized (AppConfig.lock) {
			markerReceived.set(true);
			Map<Integer, Integer> vc = CausalShared.getVectorClock();
			AVBitcakeManager.setVectorClockForMarker(vc);
			for (int i = 0; i < AppConfig.getServentCount(); i++) {
				if (i == AppConfig.myServentInfo.getId())
					continue;
				Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();

				Message avMarker = new AVMarkerMessage(MessageType.AV_MARKER, AppConfig.myServentInfo, null,
						AppConfig.getInfoById(i), vectorClock);

				for (Integer neighbour : AppConfig.myServentInfo.getNeighbors()) {
					avMarker = avMarker.changeReceiver(neighbour);
					MessageUtil.sendMessage(avMarker);
				}

				avMarker = avMarker.changeReceiver(AppConfig.myServentInfo.getId());
				CausalShared.commitCausalMessage(avMarker);
			}

			int recordedAmount = getCurrentBitcakeAmount();
			AVSnapshotResult snapshotResult = new AVSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount,
					allChannelTransactions);

			snapshotCollectorWorker.addAVSnapshotInfo(collectorId, snapshotResult);
		}
	}

	public void terminateEvent(int collectorId, SnapshotCollectorWorker snapshotCollectorWorker) {
		synchronized (AppConfig.lock) {
			
			for (int i = 0; i < AppConfig.getServentCount(); i++) {
				if (i == AppConfig.myServentInfo.getId())
					continue;
				Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();

				Message avTerm = new AVTerminateMessage(MessageType.AV_TERMINATE, AppConfig.myServentInfo, null,
						AppConfig.getInfoById(i), vectorClock);

				for (Integer neighbour : AppConfig.myServentInfo.getNeighbors()) {
					avTerm = avTerm.changeReceiver(neighbour);
					MessageUtil.sendMessage(avTerm);
				}

				avTerm = avTerm.changeReceiver(AppConfig.myServentInfo.getId());
				CausalShared.commitCausalMessage(avTerm);
			}

			//snapshotCollectorWorker.addChannelMessages(AppConfig.myServentInfo.getId(), getAllChannelTransactions());
		}
	}

	public void handleMarker(Message clientMessage, SnapshotCollector snapshotCollector) {

		synchronized (AppConfig.lock) {

			ServentInfo iniciator = clientMessage.getOriginalSenderInfo();
			int recordedAmount = getCurrentBitcakeAmount();
			AVSnapshotResult snapshotResult = new AVSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount,
					allChannelTransactions);

			Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();

			Message avTellMessage = new AVTellMessage(MessageType.AV_TELL, AppConfig.myServentInfo, null, iniciator,
					vectorClock, snapshotResult);

			for (Integer neighbour : AppConfig.myServentInfo.getNeighbors()) {
				avTellMessage = avTellMessage.changeReceiver(neighbour);

				MessageUtil.sendMessage(avTellMessage);
			}

			avTellMessage = avTellMessage.changeReceiver(AppConfig.myServentInfo.getId());
			CausalShared.commitCausalMessage(avTellMessage);
			allChannelTransactions.clear();
			recordedAmount = 0;

		}
	}

	public Map<String, List<Integer>> getAllChannelTransactions() {
		return allChannelTransactions;
	}

	public void addChannelMessage(Message clientMessage) {
		if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
			synchronized (allChannelTransactionsLock) {
				String channelName = "channel " + AppConfig.myServentInfo.getId() + "<-"
						+ clientMessage.getOriginalSenderInfo().getId();

				List<Integer> channelMessages = allChannelTransactions.getOrDefault(channelName, new ArrayList<>());
				channelMessages.add(Integer.parseInt(clientMessage.getMessageText()));
				allChannelTransactions.put(channelName, channelMessages);
			}
		}
	}

}
