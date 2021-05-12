package app.snapshot_bitcake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import app.AppConfig;
import app.CausalShared;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.ABTellMessage;
import servent.message.util.MessageUtil;

public class ABBitcakeManager implements BitcakeManager {

	public static ABBitcakeManager instancе = null;
	private final AtomicInteger currentAmount = new AtomicInteger(1000);
	public int recordedAmount = 0;
	private Map<Integer, Integer> sent = new ConcurrentHashMap<>();
	private Map<Integer, Integer> received = new ConcurrentHashMap<>();

	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);
	}

	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);
	}

	public int getCurrentBitcakeAmount() {
		return currentAmount.get();
	}

	public Map<Integer, Integer> getSent() {
		return sent;
	}

	public Map<Integer, Integer> getReceived() {
		return received;
	}

	public void recordSendTransaction(int neighbor, int amount) {
		sent.compute(neighbor, (k, v) -> {
			return v + 1;
		});
	}

	public void recordReceivedTransaction(int neighbor, int amount) {
		received.compute(neighbor, (k, v) -> {
			return v + 1;
		});
	}

	public void markerEvent(int collectorId) {
		synchronized (AppConfig.lock) {
			recordedAmount = getCurrentBitcakeAmount();
			
			for (int i = 0; i < AppConfig.getServentCount(); i++) {
				Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();
				if (AppConfig.myServentInfo.getId() == i) {
					continue;
				}
				Message abMarker = new ABMarkerMessage(MessageType.AB_MARKER, AppConfig.myServentInfo, null,
						AppConfig.getInfoById(i), vectorClock);

				for (Integer neighbour : AppConfig.myServentInfo.getNeighbors()) {
					abMarker = abMarker.changeReceiver(neighbour);
					MessageUtil.sendMessage(abMarker);
				}

				abMarker = abMarker.changeReceiver(AppConfig.myServentInfo.getId());
				CausalShared.commitCausalMessage(abMarker);
			}
		}
	}

	public void handleMarker(Message clientMessage, SnapshotCollector snapshotCollector) {
		AppConfig.timestampedStandardPrint("usao sam u handle marker");
		synchronized (AppConfig.lock) {
			
			ServentInfo iniciator = clientMessage.getRoute().get(0);
			recordedAmount = getCurrentBitcakeAmount();
			ABSnapshotResult snapshotResult = new ABSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount);

			if (AppConfig.myServentInfo.getId() == iniciator.getId()) {
				snapshotCollector.addABSnapshotInfo(iniciator.getId(), snapshotResult);
			} else {
				Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();

				Message abTellMessage = new ABTellMessage(MessageType.AB_TELL, AppConfig.myServentInfo, null, iniciator,
						vectorClock, snapshotResult);

				for (Integer neighbour : AppConfig.myServentInfo.getNeighbors()) {
					abTellMessage = abTellMessage.changeReceiver(neighbour);
					MessageUtil.sendMessage(abTellMessage);	
				}
				
				abTellMessage = abTellMessage.changeReceiver(AppConfig.myServentInfo.getId());
				CausalShared.commitCausalMessage(abTellMessage);
				recordedAmount = 0;
			}

			
		}
	}
}
