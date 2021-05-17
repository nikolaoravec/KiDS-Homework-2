package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snapshot result for servent with id serventId.
 * The amount of bitcakes on that servent is written in recordedAmount.
 * The channel messages are recorded in giveHistory and getHistory.
 * In Lai-Yang, the initiator has to reconcile the differences between
 * individual nodes, so we just let him know what we got and what we gave
 * and let him do the rest.
 * 
 * @author bmilojkovic
 *
 */
public class AVSnapshotResult implements Serializable {

	private static final long serialVersionUID = 8939516333227254439L;
	
	private final int serventId;
	private final int recordedAmount;
	private final Map<String, List<Integer>> allChannelTransaction;
	
	public AVSnapshotResult(int serventId, 
			int recordedAmount,
			Map<String, List<Integer>> allChannelTransaction) {
		this.serventId = serventId;
		this.recordedAmount = recordedAmount;
		this.allChannelTransaction = new ConcurrentHashMap<>(allChannelTransaction);
	}
		
	public int getServentId() {
		return serventId;
	}
	public int getRecordedAmount() {
		return recordedAmount;
	}
	
	public Map<String, List<Integer>> getAllChannelTransaction() {
		return allChannelTransaction;
	}
	
}
