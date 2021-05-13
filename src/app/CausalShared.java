package app;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.BasicMessageHandler;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.RebroadcastHandler;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.ABMarkerHandler;
import servent.handler.snapshot.ABTellHandler;
import servent.message.Message;
import servent.message.snapshot.ABTellMessage;


/**
 * This class contains shared data for the Causal Broadcast implementation:
 * <ul>
 * <li> Vector clock for current instance
 * <li> Commited message list
 * <li> Pending queue
 * </ul>
 * As well as operations for working with all of the above.
 * 

 *
 */
public class CausalShared {

	private final static Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
	private final static List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
	private final static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
	private final static Object pendingMessagesLock = new Object();
	private static SnapshotCollector snapshotCollector;
	
	private final static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	public static void initializeVectorClock(int serventCount) {
		for(int i = 0; i < serventCount; i++) {
			vectorClock.put(i, 0);
		}
	}
	
	public static void incrementClock(int serventId) {
		vectorClock.computeIfPresent(serventId,  (k,v) -> {
            return v+1;
		});
	}
	
	public static Map<Integer, Integer> getVectorClock() {
		return vectorClock;
	}
	
	public static void addPendingMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public static void commitCausalMessage(Message newMessage) {
		System.out.println("Committing my msg " + newMessage);
		commitedCausalMessageList.add(newMessage);
		incrementClock(newMessage.getOriginalSenderInfo().getId());
		
		checkPendingMessages();
	}
	
	public static List<Message> getCommitedCausalMessages() {
		List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);
		
		return toReturn;
	}
	
	public static ExecutorService getThreadpool() {
		return threadPool;
	}
	
	public static void setSnapshotCollector(SnapshotCollector snapshotCollector) {
		CausalShared.snapshotCollector = snapshotCollector;
	}
	
	private static boolean otherClockGreater(Map<Integer, Integer> clock1, Map<Integer, Integer> clock2) {
		if (clock1.size() != clock2.size()) {
			throw new IllegalArgumentException("Clocks are not same size how why");
		}
		
		for(int i = 0; i < clock1.size(); i++) {
			if (clock2.get(i) > clock1.get(i)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void checkPendingMessages() {
		boolean gotWork = true;
		
		while (gotWork) {
			gotWork = false;
			
			synchronized (pendingMessagesLock) {
				Iterator<Message> iterator = pendingMessages.iterator();
				
				
				Map<Integer, Integer> myVectorClock = getVectorClock();
				while (iterator.hasNext()) {
					
					Message pendingMessage = iterator.next();
//					AppConfig.timestampedErrorPrint("Poruka od: " + pendingMessage.getOriginalSenderInfo() + 
//							"  Za:" + pendingMessage.getTargetInfo() + "  VectorClock poruke:" +
//							pendingMessage.getVectorClock() + "  Moj VC:" + myVectorClock);
//					
//					
					MessageHandler messagehandler = new NullHandler(pendingMessage);
					
					if(!AppConfig.IS_CLIQUE) {
						messagehandler = new RebroadcastHandler(pendingMessage);
						threadPool.submit(messagehandler);
					}
					
					if (!otherClockGreater(myVectorClock, pendingMessage.getVectorClock())) {
						AppConfig.timestampedErrorPrint("da li je ab_tell: " + (pendingMessage instanceof ABTellMessage));
						gotWork = true;
						if (AppConfig.myServentInfo.getId() == pendingMessage.getTargetInfo().getId()) {
							
							
							switch (pendingMessage.getMessageType()) {
							case BASIC: 
								messagehandler = new BasicMessageHandler(pendingMessage);
								break;
							case TRANSACTION:
								messagehandler = new TransactionHandler(pendingMessage, snapshotCollector.getBitcakeManager());
								break;
							case AB_MARKER:
								messagehandler = new ABMarkerHandler(pendingMessage, snapshotCollector);
								break;
							case AB_TELL:
								AppConfig.timestampedStandardPrint("AB_TELL HANDLE" );
								messagehandler = new ABTellHandler(pendingMessage, snapshotCollector);
								break;
							case AV_MARKER:
								//messagehandler = new AVMarkerHandler();
								break;
						//	case AV_TELL:
								//messageHandler = new АVTellHandler(clientMessage, snapshotCollector);
							default :
								break;
							}
							try {
								threadPool.submit(messagehandler);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
						commitedCausalMessageList.add(pendingMessage);
						AppConfig.timestampedStandardPrint("Commiting from : " + pendingMessage);
						incrementClock(pendingMessage.getOriginalSenderInfo().getId());
						iterator.remove();
						
						break;
					}
			}
		}
		
	}}
}
