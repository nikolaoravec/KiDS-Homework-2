package servent.message;

import java.util.List;
import java.util.Map;

import app.ServentInfo;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.AVBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {

	protected TransactionMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, List<ServentInfo> routeList, String messageText, int messageId,
			Map<Integer, Integer> vectorClock) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, routeList, messageText, messageId, vectorClock);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -333251402058492901L;

	private transient BitcakeManager bitcakeManager;
	private Map<Integer, Integer> senderClock;
	
	
	
	public Map<Integer, Integer> getSenderClock() {
		return senderClock;
	}
	
	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());
		
		bitcakeManager.takeSomeBitcakes(amount);
		if (bitcakeManager instanceof ABBitcakeManager) {
			ABBitcakeManager manager = (ABBitcakeManager)bitcakeManager;
			manager.recordSendTransaction(getReceiverInfo().getId(), amount);
		}
	}
}
