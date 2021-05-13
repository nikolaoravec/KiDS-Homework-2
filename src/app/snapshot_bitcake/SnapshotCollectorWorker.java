package app.snapshot_bitcake;

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
				((ABBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
				break;
			case AV:
				//((AVBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);
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
					AppConfig.timestampedErrorPrint("BROJ COLLECTED VALUES "+collectedABValues.size());
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
				for (Entry<Integer, ABSnapshotResult> nodeResult : collectedABValues.entrySet()) {
				
					AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
				}
				break;
			case AV:
				
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
