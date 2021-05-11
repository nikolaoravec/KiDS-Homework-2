package cli.command;

import java.util.Map;

import app.AppConfig;
import app.CausalShared;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;


public class BasicCommand implements CLICommand{

	@Override
	public void execute(String args) {
		String msgToSend = "";
		
		msgToSend = args;
		
		if (args == null) {
			AppConfig.timestampedErrorPrint("No message to broadcast");
			return;
		}
		
		Map<Integer, Integer> vectorClock = CausalShared.getVectorClock();
		Message broadcastMessage = new BasicMessage(MessageType.BASIC,AppConfig.myServentInfo,null,null,msgToSend,vectorClock); 
		for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
			/*
			 * It is important to modify the existing message, and not create a new one.
			 * 
			 * If we do new BroadcastMessage(...) the message id will change,
			 * and thus, different neighbors will get non-equal messages, and
			 * there will be unnecessary rebroadcast.
			 */
			broadcastMessage = broadcastMessage.changeReceiver(neighbor);

			MessageUtil.sendMessage(broadcastMessage);
			
			
		}
		
		broadcastMessage = broadcastMessage.changeReceiver(AppConfig.myServentInfo.getId());
		//Message msg = new BasicMessage(MessageType.BASIC,AppConfig.myServentInfo,AppConfig.myServentInfo,null,msgToSend,vectorClock);
		CausalShared.commitCausalMessage(broadcastMessage);
		
	}
	

	
	@Override
	public String commandName() {
		return "basic";
	}
}
