package servent.message.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

public class ABTellMessage extends BasicMessage{
	
	private static final long serialVersionUID = 5197200699569608649L;
	private final ABSnapshotResult abSnapshotResult;

	public ABTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
			ServentInfo targetInfo, Map<Integer, Integer> vectorClock, ABSnapshotResult abSnapshotResult) {
		super(type, originalSenderInfo, receiverInfo, targetInfo, vectorClock);
		this.abSnapshotResult = abSnapshotResult;
	}

	protected ABTellMessage(MessageType messageType, ServentInfo originalSenderInfo, ServentInfo newReceiverInfo,
			ServentInfo targetInfo, List<ServentInfo> route, String messageText, int messageId,
			Map<Integer, Integer> vectorClock, ABSnapshotResult abSnapshotResult) {
		super(messageType, originalSenderInfo, newReceiverInfo, targetInfo,route,messageText,messageId,vectorClock);
		this.abSnapshotResult = abSnapshotResult;
		
	}

	public ABSnapshotResult getAbSnapshotResult() {
		return abSnapshotResult;
	}
	
	
	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId) || AppConfig.myServentInfo.getId() == newReceiverId) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);
			
			Message toReturn = new ABTellMessage(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, getTargetInfo(),getRoute(), getMessageText(), getMessageId(), getVectorClock(), getAbSnapshotResult());
			
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
		Message toReturn = new ABTellMessage(getMessageType(), getOriginalSenderInfo(), 
				getReceiverInfo(), getTargetInfo(),newRouteList, getMessageText(), getMessageId(), getVectorClock(), getAbSnapshotResult());
		
		return toReturn;
	}
}
