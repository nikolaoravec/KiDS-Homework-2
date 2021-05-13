package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransactionMessage;
import servent.message.snapshot.ABTellMessage;
import servent.message.util.MessageUtil;

/**
 * This will be used if no proper handler is found for the message.
 * 
 * @author bmilojkovic
 *
 */
public class RebroadcastHandler implements MessageHandler {

	private final Message clientMessage;

	public RebroadcastHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {

		for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
			try {
				if(clientMessage.getMessageType() == MessageType.AB_TELL) {
					ABTellMessage abTellMessage = (ABTellMessage) clientMessage;
					MessageUtil.sendMessage(abTellMessage.changeReceiver(neighbor).makeMeASender());
				}if(clientMessage.getMessageType() == MessageType.TRANSACTION) {
					TransactionMessage transactionMessage = (TransactionMessage) clientMessage;
					MessageUtil.sendMessage(transactionMessage.changeReceiver(neighbor).makeMeASender());
				}else {
					
					MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
