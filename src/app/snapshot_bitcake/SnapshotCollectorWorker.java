package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;

/**
 * Main snapshot collector class. Has support AB and AV snapshot algorithms.
 * 
 * @author bmilojkovic
 *
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

	private volatile boolean working = true;

	private AtomicBoolean collecting = new AtomicBoolean(false);

	private Map<Integer, ABSnapshotResult> collectedABValues = new ConcurrentHashMap<>();
	private Map<Integer, AVSnapshotResult> collectedAVValues = new ConcurrentHashMap<>();
	private Map<Integer, Map<String, List<Integer>>> allChannelTransaction = new ConcurrentHashMap<>();

	private SnapshotType snapshotType = SnapshotType.AB;

	private BitcakeManager bitcakeManager;

	public SnapshotType getSnapshotType() {
		return snapshotType;
	}

	public SnapshotCollectorWorker(SnapshotType snapshotType) {
		this.snapshotType = snapshotType;

		switch (snapshotType) {
		case AB:
			bitcakeManager = new ABBitcakeManager();
			break;
		case AV:
			bitcakeManager = new AVBitcakeManager();
			break;
		case NONE:
			AppConfig.timestampedErrorPrint("Making snapshot collector without specifying type. Exiting...");
			System.exit(0);
		}
	}

	@Override
	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}

	@Override
	public void run() {
		while (working) {

			while (collecting.get() == false) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (working == false) {
					return;
				}
			}

			/*
			 * Collecting is done in three stages: 1. Send messages asking for values 2.
			 * Wait for all the responses 3. Print result
			 */
			// 1 send asks
			switch (snapshotType) {
			case AB:
				((ABBitcakeManager) bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
				break;
			case AV:
				((AVBitcakeManager) bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
				break;
			case NONE:
				// Shouldn't be able to come here. See constructor.
				break;
			}

			// 2 wait for responses or finish
			boolean waiting = true;
			while (waiting) {
				switch (snapshotType) {
				case AB:
					AppConfig.timestampedErrorPrint("BROJ COLLECTED VALUES " + collectedABValues.size());
					if (collectedABValues.size() == AppConfig.getServentCount()) {
						waiting = false;
					}
					break;
				case AV:
					AppConfig.timestampedErrorPrint("BROJ COLLECTED VALUES " + collectedAVValues.size());
					if (collectedAVValues.size() == AppConfig.getServentCount()) {
						((AVBitcakeManager) bitcakeManager).terminateEvent(AppConfig.myServentInfo.getId(), this);
						waiting = false;
					}
					break;
//					while (true) {
//						if(collectedAVValues.size() == AppConfig.getServentCount()) {
//							break;
//						}
//					}
//					((AVBitcakeManager) bitcakeManager).terminateEvent(AppConfig.myServentInfo.getId(), this);
//					while (true) {
//						if(allChannelTransaction.size() == AppConfig.getServentCount()) {
//							break;
//						}
//	
//					}
//					waiting = false;
//					break;
				case NONE:
					// Shouldn't be able to come here. See constructor.
					break;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (working == false) {
					return;
				}
			}

			// print

			int suma;
			switch (snapshotType) {
			case AB:
				suma = 0;
				for (Entry<Integer, ABSnapshotResult> nodeResult : collectedABValues.entrySet()) {
					suma += nodeResult.getValue().getRecordedAmount();
					AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = "
							+ nodeResult.getValue().getRecordedAmount());
				}

				synchronized (AppConfig.lock) {
					for (int i = 0; i < AppConfig.getServentCount(); i++) {
						for (int j = 0; j < AppConfig.getServentCount(); j++) {
							if (i != j) {

								int sentAmount = collectedABValues.get(i).getSent().get(j);
								int receivedAmount = collectedABValues.get(j).getReceived().get(i);

								if (sentAmount > receivedAmount) {

									int inTransit = sentAmount - receivedAmount;
									suma += inTransit;

									String outputString = String.format(
											"Unreceived bitcake amount: %d from servent %d to servent %d", inTransit, i,
											j);
									AppConfig.timestampedStandardPrint(outputString);
								}

							}
						}
					}
				}

				AppConfig.timestampedStandardPrint("Sum off all bitckaes in system is " + suma);
				suma = 0;
				break;
			case AV:
				suma = 0;
				
				AVBitcakeManager avBitcakeManager = (AVBitcakeManager) bitcakeManager;
				
				for (Entry<Integer, AVSnapshotResult> nodeResult : collectedAVValues.entrySet()) {
					suma += nodeResult.getValue().getRecordedAmount();
					AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = "
							+ nodeResult.getValue().getRecordedAmount());
				}
				for (Entry<String, List<Integer>> channel : avBitcakeManager.getAllChannelTransactions().entrySet()) {
					int sumOfChannel = 0;
					for (Integer val : channel.getValue()) {
						sumOfChannel += val;
					}
					//suma += sumOfChannel;
					AppConfig.timestampedStandardPrint("Bitcake transactions before the marker for servent "
							+ AppConfig.myServentInfo.getId() + " from " + channel.getKey() + " is" + sumOfChannel);
				}
//				for (Entry<Integer, AVSnapshotResult> nodeResult : collectedAVValues.entrySet()) {
//					suma += nodeResult.getValue().getRecordedAmount();
//					AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = "
//							+ nodeResult.getValue().getRecordedAmount());
//					if (nodeResult.getValue().getAllChannelTransaction().size() == 0) {
//						AppConfig.timestampedStandardPrint("No channel bitcake for " + nodeResult.getKey());
//					} else {
//						for (Entry<String, List<Integer>> channelMessages : nodeResult.getValue()
//								.getAllChannelTransaction().entrySet()) {
//							int channelSum = 0;
//							for (Integer val : channelMessages.getValue()) {
//								channelSum += val;
//							}
//							AppConfig.timestampedStandardPrint("Channel bitcake for " + channelMessages.getKey() + ": "
//									+ channelMessages.getValue() + " with channel bitcake sum: " + channelSum);
//
//							suma += channelSum;
//						}
//					}
//				}
//
//				for (Entry<Integer, Map<String, List<Integer>>> channelMessagesForServents : allChannelTransaction
//						.entrySet()) {
//
//					for (Entry<String, List<Integer>> channelMessages : channelMessagesForServents.getValue()
//							.entrySet()) {
//						int channelSum = 0;
//						for (Integer val : channelMessages.getValue()) {
//							channelSum += val;
//						}
//						AppConfig.timestampedStandardPrint("Channel bitcake for " + channelMessages.getKey() + ": "
//								+ channelMessages.getValue() + " with channel bitcake sum: " + channelSum);
//
//						suma += channelSum;
//					}
//
//				}
//
				AppConfig.timestampedStandardPrint("Sum off all bitckaes in system is " + suma);

				break;
			case NONE:
				// Shouldn't be able to come here. See constructor.
				break;
			}
			collecting.set(false);
		}

	}

	@Override
	public void addABSnapshotInfo(int id, ABSnapshotResult abSnapshotResult) {
		collectedABValues.put(id, abSnapshotResult);
	}

	@Override
	public void addAVSnapshotInfo(int id, AVSnapshotResult avSnapshotResult) {
		collectedAVValues.put(id, avSnapshotResult);
	}

	@Override
	public void startCollecting() {
		boolean oldValue = this.collecting.getAndSet(true);

		if (oldValue == true) {
			AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
		}
	}

	@Override
	public void stop() {
		working = false;
	}

	@Override
	public void addChannelMessages(int id, Map<String, List<Integer>> channel) {
		allChannelTransaction.put(id, channel);

	}

}
