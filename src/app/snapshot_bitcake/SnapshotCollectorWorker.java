package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import servent.message.Message;
//import servent.message.snapshot.NaiveAskAmountMessage;
import servent.message.util.MessageUtil;

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
	
	private SnapshotType snapshotType = SnapshotType.AB;
	
	private BitcakeManager bitcakeManager;

	public SnapshotCollectorWorker(SnapshotType snapshotType) {
		this.snapshotType = snapshotType;
		
		switch(snapshotType) {
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
		while(working) {
			
			/*
			 * Not collecting yet - just sleep until we start actual work, or finish
			 */
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
			 * Collecting is done in three stages:
			 * 1. Send messages asking for values
			 * 2. Wait for all the responses
			 * 3. Print result
			 */
			//1 send asks
			switch (snapshotType) {
			case AB:
				((ABBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId());
				break;
			case AV:
				((AVBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
				break;
			case NONE:
				//Shouldn't be able to come here. See constructor. 
				break;
			}
			
			//2 wait for responses or finish
			boolean waiting = true;
			while (waiting) {
				switch (snapshotType) {
				case AB:
					if (collectedABValues.size() == AppConfig.getServentCount()) {
						waiting = false;
					}
					break;
				case AV:
					if (collectedAVValues.size() == AppConfig.getServentCount()) {
						waiting = false;
					}
					break;
				case NONE:
					//Shouldn't be able to come here. See constructor. 
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
			
			//print
			int sum;
			switch (snapshotType) {
			case AB:
				sum = 0;
				for (Entry<Integer, ABSnapshotResult> nodeResult : collectedABValues.entrySet()) {
					sum += nodeResult.getValue().getRecordedAmount();
					AppConfig.timestampedStandardPrint(
							"Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
					if (nodeResult.getValue().getAllChannelMessages().size() == 0) {
						AppConfig.timestampedStandardPrint("No channel bitcake for " + nodeResult.getKey());
					} else {
						for (Entry<String, List<Integer>> channelMessages : nodeResult.getValue().getAllChannelMessages().entrySet()) {
							int channelSum = 0;
							for (Integer val : channelMessages.getValue()) {
								channelSum += val;
							}
							AppConfig.timestampedStandardPrint("Channel bitcake for " + channelMessages.getKey() +
									": " + channelMessages.getValue() + " with channel bitcake sum: " + channelSum);
							
							sum += channelSum;
						}
					}
				}
				
				AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
				
				collectedABValues.clear(); //reset for next invocation
				break;
			case AV:
				sum = 0;
				for (Entry<Integer, AVSnapshotResult> nodeResult : collectedAVValues.entrySet()) {
					sum += nodeResult.getValue().getRecordedAmount();
					AppConfig.timestampedStandardPrint(
							"Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
				}
				for(int i = 0; i < AppConfig.getServentCount(); i++) {
					for (int j = 0; j < AppConfig.getServentCount(); j++) {
						if (i != j) {
							if (AppConfig.getInfoById(i).getNeighbors().contains(j) &&
								AppConfig.getInfoById(j).getNeighbors().contains(i)) {
								int ijAmount = collectedAVValues.get(i).getGiveHistory().get(j);
								int jiAmount = collectedAVValues.get(j).getGetHistory().get(i);
								
								if (ijAmount != jiAmount) {
									String outputString = String.format(
											"Unreceived bitcake amount: %d from servent %d to servent %d",
											ijAmount - jiAmount, i, j);
									AppConfig.timestampedStandardPrint(outputString);
									sum += ijAmount - jiAmount;
								}
							}
						}
					}
				}
				
				AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
				
				collectedAVValues.clear(); //reset for next invocation
				break;
			case NONE:
				//Shouldn't be able to come here. See constructor. 
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

}
