package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;

import app.Cancellable;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();

	void addABSnapshotInfo(int id, ABSnapshotResult abSnapshotResult);
	void addAVSnapshotInfo(int id, AVSnapshotResult avSnapshotResult);
	void addChannelMessages(int id, Map<String, List<Integer>> channel);
	void startCollecting();

}