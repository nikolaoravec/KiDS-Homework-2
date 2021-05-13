package servent.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.ABBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another
 * node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {
	private transient BitcakeManager bitcakeManager;
	private static final long serialVersionUID = -333251402058492901L;

	public TransactionMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, Map<Integer, Integer> vectorClock, String messageText,
			BitcakeManager bitcakeManager) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, messageText, vectorClock);
		this.bitcakeManager = bitcakeManager;
	}

	protected TransactionMessage(MessageType messageType, ServentInfo originalSenderInfo, ServentInfo newReceiverInfo,
			ServentInfo targetInfo, List<ServentInfo> route, String messageText, int messageId,
			Map<Integer, Integer> vectorClock, BitcakeManager bitcakeManager) {
		super(messageType, originalSenderInfo, newReceiverInfo, targetInfo, route, messageText, messageId, vectorClock);
		this.bitcakeManager = bitcakeManager;

	}

	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}

	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)
				|| AppConfig.myServentInfo.getId() == newReceiverId) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

			Message toReturn = new TransactionMessage(getMessageType(), getOriginalSenderInfo(), newReceiverInfo,
					getTargetInfo(), getRoute(), getMessageText(), getMessageId(), getVectorClock(),
					getBitcakeManager());
			AppConfig.timestampedErrorPrint("Novi receiver " + newReceiverInfo.getId());
			return toReturn;
		} else {
			AppConfig
					.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

			return null;
		}

	}

	@Override
	public Message makeMeASender() {
		ServentInfo newRouteItem = AppConfig.myServentInfo;

		List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
		newRouteList.add(newRouteItem);
		Message toReturn = new TransactionMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(),
				getTargetInfo(), newRouteList, getMessageText(), getMessageId(), getVectorClock(), getBitcakeManager());

		return toReturn;
	}

	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots
	 * don't mess up. This method is invoked by the sender just before sending, and
	 * with a lock that guarantees that we are white when we are doing this in
	 * Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());

		if (bitcakeManager instanceof ABBitcakeManager) {
			AppConfig.timestampedErrorPrint("usao sam u bitcake");
			ABBitcakeManager manager = (ABBitcakeManager) bitcakeManager;

			manager.takeSomeBitcakes(amount);

			// manager.recordSendTransaction(getReceiverInfo().getId(), amount);
		}
	}
}
