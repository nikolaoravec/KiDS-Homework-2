package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class AVBitcakeManager implements BitcakeManager, Serializable {

private final AtomicInteger currentAmount = new AtomicInteger(1000);
	
	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);
	}
	
	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);
	}
	
	public int getCurrentBitcakeAmount() {
		return currentAmount.get();
	}

	
	
}
