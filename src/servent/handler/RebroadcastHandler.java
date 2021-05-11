package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.util.MessageUtil;


/**
 * This will be used if no proper handler is found for the message.
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
				MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());
			}catch(Exception e ) {
				e.printStackTrace();
			}
			
		}
	}

}
