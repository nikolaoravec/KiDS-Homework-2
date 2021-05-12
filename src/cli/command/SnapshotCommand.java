package cli.command;

import app.snapshot_bitcake.SnapshotCollector;

public class SnapshotCommand implements CLICommand{
	
	private SnapshotCollector collector;
	
	public SnapshotCommand(SnapshotCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(String args) {
		collector.startCollecting();
	}

	
	@Override
	public String commandName() {
		return "snapshot";
	}
}
