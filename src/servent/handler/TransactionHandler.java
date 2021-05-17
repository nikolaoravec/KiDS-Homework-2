package servent.handler;

import app.AppConfig;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;
import servent.message.Message;
import servent.message.MessageType;

public class TransactionHandler implements MessageHandler {

	private Message clientMessage;
	private BitcakeManager bitcakeManager;

	public TransactionHandler(Message clientMessage, BitcakeManager bitcakeManager) {
		this.clientMessage = clientMessage;
		this.bitcakeManager = bitcakeManager;
	}

	@Override
	public void run() {
		try {
			if (bitcakeManager instanceof ABBitcakeManager) {

				ABBitcakeManager manager = (ABBitcakeManager) bitcakeManager;

				int amount = Integer.parseInt(clientMessage.getMessageText());

				manager.addSomeBitcakes(amount);
				
				manager.recordReceivedTransaction(clientMessage.getOriginalSenderInfo().getId(), amount);
			}
			
			if (bitcakeManager instanceof AVBitcakeManager) {

				AVBitcakeManager manager = (AVBitcakeManager) bitcakeManager;

				int amount = Integer.parseInt(clientMessage.getMessageText());

				manager.addSomeBitcakes(amount);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
