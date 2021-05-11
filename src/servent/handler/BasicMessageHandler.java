package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class BasicMessageHandler implements MessageHandler {

	private final Message clientMessage;
	
	public BasicMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		System.out.println(clientMessage);
	}

}
