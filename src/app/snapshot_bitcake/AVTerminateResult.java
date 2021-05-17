package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AVTerminateResult implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int serventId;
	private final Map<String, List<Integer>> allChannelTransaction;
	
	public AVTerminateResult(int serventId, Map<String, List<Integer>> allChannelTransaction) {
		this.serventId = serventId;
		this.allChannelTransaction = new ConcurrentHashMap<>(allChannelTransaction);
	}
	
	public int getServentId() {
		return serventId;
	}
	
	public Map<String, List<Integer>> getAllChannelTransaction() {
		return allChannelTransaction;
	}
	

}
