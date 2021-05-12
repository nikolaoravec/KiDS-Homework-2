package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snapshot result for servent with id serventId.
 * The amount of bitcakes on that servent is recordedAmount,
 * and messages from others to that servent are recorded in
 * received, all messages that I sent to others are in received.
 * 
 * @author bmilojkovic
 *
 */
public class ABSnapshotResult implements Serializable {

	private static final long serialVersionUID = -1443515806440079979L;

	private final int serventId;
	private final int recordedAmount;
//	private final Map<Integer, Integer> sent;
//	private final Map<Integer, Integer> received;
	
	public ABSnapshotResult(int serventId, int recordedAmount/*, Map<Integer, Integer> sent, Map<Integer, Integer> received*/) {
		this.serventId = serventId;
		this.recordedAmount = recordedAmount;
//		this.sent = new ConcurrentHashMap<>(sent);
//		this.received = new ConcurrentHashMap<>(received);
	}
	
	public int getServentId() {
		return serventId;
	}
	public int getRecordedAmount() {
		return recordedAmount;
	}
//	public Map<Integer, Integer> getSent() {
//		return sent;
//	}
//	public Map<Integer, Integer> getReceived() {
//		return received;
//	}
	
	
	
}
