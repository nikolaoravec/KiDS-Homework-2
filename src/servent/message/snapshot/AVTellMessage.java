package servent.message.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.AVSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

public class AVTellMessage extends BasicMessage {

	private static final long serialVersionUID = 3116394054726162318L;

	private AVSnapshotResult avSnapshotResult;
	

	public AVTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, Map<Integer, Integer> vectorClock, AVSnapshotResult avSnapshotResult) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, vectorClock);
		this.avSnapshotResult = avSnapshotResult;
	}

	protected AVTellMessage(MessageType messageType, ServentInfo originalSenderInfo, ServentInfo newReceiverInfo,
			ServentInfo targetInfo, List<ServentInfo> route, String messageText, int messageId,
			Map<Integer, Integer> vectorClock, AVSnapshotResult avSnapshotResult) {
		super(messageType, originalSenderInfo, newReceiverInfo, targetInfo,route,messageText,messageId,vectorClock);
		this.avSnapshotResult = avSnapshotResult;
		
	}
	
	public AVSnapshotResult getAvSnapshotResult() {
		return avSnapshotResult;
	}
	
	
	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId) || AppConfig.myServentInfo.getId() == newReceiverId) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);
			
			Message toReturn = new AVTellMessage(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, getTargetInfo(),getRoute(), getMessageText(), getMessageId(), getVectorClock(), getAvSnapshotResult());
			
			return toReturn;
		} else {
			AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
			
			return null;
		}
		
	}
	
	@Override
	public Message makeMeASender() {
		ServentInfo newRouteItem = AppConfig.myServentInfo;
		
		List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
		newRouteList.add(newRouteItem);
		Message toReturn = new AVTellMessage(getMessageType(), getOriginalSenderInfo(), 
				getReceiverInfo(), getTargetInfo(),newRouteList, getMessageText(), getMessageId(), getVectorClock(), getAvSnapshotResult());
		
		return toReturn;
	}
	
}
