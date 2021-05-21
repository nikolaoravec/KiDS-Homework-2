package cli.command;

import java.util.Map;

import app.AppConfig;
import app.CausalShared;
import app.snapshot_bitcake.BitcakeManager;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

public class TransactionBurstCommand implements CLICommand {

	private static final int BURST_WORKERS =1;
	private static final int MAX_TRANSFER_AMOUNT = 50;
	
	private final BitcakeManager bitcakeManager;
	
	public TransactionBurstCommand(BitcakeManager bitcakeManager) {
		this.bitcakeManager = bitcakeManager;
	}
	
	private class TransactionBurstWorker implements Runnable {
		
		@Override
		public void run() {
			for (int i = 0; i < AppConfig.getServentCount(); i++) {
				if (AppConfig.myServentInfo.getId() == i) {
					continue;
				}
				int amount = 1 + (int)(Math.random() * MAX_TRANSFER_AMOUNT);
				Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();
				Message transactionMessage = new TransactionMessage(MessageType.TRANSACTION,AppConfig.myServentInfo, null, AppConfig.getInfoById(i),vectorClock, String.valueOf(amount), bitcakeManager);
				for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
					transactionMessage = transactionMessage.changeReceiver(neighbor);
					AppConfig.timestampedErrorPrint("Ja sam " + AppConfig.myServentInfo.getId() + " i saljem bitcake " + AppConfig.getInfoById(i));
					/*
					 * The message itself will reduce our bitcake count as it is being sent.
					 * The sending might be delayed, so we want to make sure we do the
					 * reducing at the right time, not earlier.
					 */
					
					MessageUtil.sendMessage(transactionMessage);
					
				}
				transactionMessage.sendEffect();
				transactionMessage = transactionMessage.changeReceiver(AppConfig.myServentInfo.getId());
				CausalShared.commitCausalMessage(transactionMessage);
				
				
			}
		}
	}
	
	@Override
	public String commandName() {
		return "transaction_burst";
	}

	@Override
	public void execute(String args) {
		for (int i = 0; i < BURST_WORKERS; i++) {
			Thread t = new Thread(new TransactionBurstWorker());
			
			t.start();
		}
	}

	
}
