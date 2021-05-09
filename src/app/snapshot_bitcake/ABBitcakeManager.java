package app.snapshot_bitcake;

import java.util.concurrent.atomic.AtomicInteger;

public class ABBitcakeManager implements BitcakeManager {

	private final AtomicInteger currentAmount = new AtomicInteger(1000);

	
	@Override
	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);

	}

	@Override
	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);

	}

	@Override
	public int getCurrentBitcakeAmount() {
		currentAmount.get();
		return 0;
	}

}
