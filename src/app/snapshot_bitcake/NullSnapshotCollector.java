package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;

/**
 * This class is used if the user hasn't specified a snapshot type in config.
 * 
 * @author bmilojkovic
 *
 */
public class NullSnapshotCollector implements SnapshotCollector {

	@Override
	public void run() {}

	@Override
	public void stop() {}

	@Override
	public BitcakeManager getBitcakeManager() {
		return null;
	}

	@Override
	public void addABSnapshotInfo(int id, ABSnapshotResult abSnapshotResult) {}

	@Override
	public void addAVSnapshotInfo(int id, AVSnapshotResult avSnapshotResult) {}

	@Override
	public void startCollecting() {}

	@Override
	public void addChannelMessages(int id, Map<String, List<Integer>> channel) {
		// TODO Auto-generated method stub
		
	}

}
