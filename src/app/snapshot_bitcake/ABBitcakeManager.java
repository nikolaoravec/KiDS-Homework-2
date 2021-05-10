package app.snapshot_bitcake;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TransactionMessage;
import servent.message.snapshot.ABMarkerMessage;
import servent.message.snapshot.AVMarkerMessage;
import servent.message.snapshot.CLMarkerMessage;
import servent.message.util.MessageUtil;

public class ABBitcakeManager implements BitcakeManager {

	private static final AtomicInteger currentAmount = new AtomicInteger(1000);
	public int recordedAmount = 0;
	
	private static Map<Integer, Integer> sent = new ConcurrentHashMap<>();
	private static Map<Integer, Integer> received = new ConcurrentHashMap<>();
	private static List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
	private static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
	private static Object pendingMessagesLock = new Object();
	
	public static void initializeVectorClock(int serventCount) {
		for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
			sent.put(neighbor, 0);
			received.put(neighbor, 0);
		}
	}
	
	public static void incrementSentClock(int serventId, int sentBitCakes) {
		sent.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

			@Override
			public Integer apply(Integer key, Integer oldValue) {
				return oldValue+1;
			}
		});
	}
	
	public static void incrementReceivedClock(int serventId, int receivedBitCakes) {
		sent.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

			@Override
			public Integer apply(Integer key, Integer oldValue) {
				return oldValue+1;
			}
		});
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
	
	public static int getCurrentBitcakeAmount() {
		return currentAmount.get();
	}
	
	
	public void markerEvent(int collectorId) {
		synchronized (AppConfig.colorLock) {
			AppConfig.timestampedStandardPrint("Going red");
			AppConfig.hasMarker.set(true);
			recordedAmount = getCurrentBitcakeAmount();
			
			for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
				//closedChannels.put(neighbor, false)
				Message marker = new ABMarkerMessage(AppConfig.myServentInfo, AppConfig.getInfoById(neighbor), sent);
				MessageUtil.sendMessage(marker);
				try {
					/**
					 * This sleep is here to artificially produce some white node -> red node messages
					 */
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public static Map<Integer, Integer> getSent() {
		return sent;
	}

	
	public static Map<Integer, Integer> getReceived() {
		return received;
	}


	public static List<Message> getCommitedCausalMessages() {
		List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);
		
		return toReturn;
	}
	
	public static void addPendingMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public static void commitCausalMessage(Message newMessage) {
		commitedCausalMessageList.add(newMessage);
		incrementClock(newMessage.getOriginalSenderInfo().getId());
		
		checkPendingMessages();
	}

	
	@Override
	public void takeSomeBitcakes(int amount) {
		currentAmount.getAndAdd(-amount);

	}

	@Override
	public void addSomeBitcakes(int amount) {
		currentAmount.getAndAdd(amount);

	}

	@Override
	public int getCurrentBitcakeAmount() {
		currentAmount.get();
		return 0;
	}
	
	public class MapValueUpdater implements BiFunction<Integer, Integer, Integer> {
		
		private int valueToAdd;
		
		public MapValueUpdater(int valueToAdd) {
			this.valueToAdd = valueToAdd;
		}
		
		@Override
		public Integer apply(Integer key, Integer oldValue) {
			return oldValue + valueToAdd;
		}
	}
		
		
	public void recordSendTransaction(int neighbor, int amount) {
		sent.compute(neighbor,  (k,v) -> {
            return v+1;
		});
	}
	
	public static void recordReceivedTransaction(int neighbor, int amount) {
		received.compute(neighbor,  (k,v) -> {
              return v+1;
       });
	}
	
	
	
	public static void checkPendingMessages() {
		boolean gotWork = true;
		
		while (gotWork) {
			gotWork = false;
			
			synchronized (pendingMessagesLock) {
				Iterator<Message> iterator = pendingMessages.iterator();
				
				Map<Integer, Integer> myReceivedClock = getReceived();
				while (iterator.hasNext()) {
					Message pendingMessage = iterator.next();
					
					/*		if (pendingMessage instanceof ABMarkerMessage) {
						ABMarkerMessage causalPendingMessage = (ABMarkerMessage)pendingMessage;			
						
						if (!otherClockGreater(myVectorClock, causalPendingMessage.getSenderVectorClock())) {
							gotWork = true;
							
							AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
							commitedCausalMessageList.add(pendingMessage);
							incrementClock(pendingMessage.getOriginalSenderInfo().getId());
							
							iterator.remove();
							
							break;
						}
					}*/
					
					if (pendingMessage.getMessageType() == MessageType.TRANSACTION) {
						TransactionMessage message = (TransactionMessage)pendingMessage;	
						if (!otherClockGreater(getReceived(), message.getSenderClock())) {
							gotWork = true;
							
						String amountString = pendingMessage.getMessageText();
						
						int amountNumber = 0;
						try {
							amountNumber = Integer.parseInt(amountString);
						} catch (NumberFormatException e) {
							AppConfig.timestampedErrorPrint("Couldn't parse amount: " + amountString);
							return;
						}
						
						//addSomeBitcakes(amountNumber);
			
						recordReceivedTransaction(pendingMessage.getOriginalSenderInfo().getId(), amountNumber);
					}
				}
					/*	
						bitcakeManager.addSomeBitcakes(amountNumber);
						synchronized (AppConfig.colorLock) {
						if (bitcakeManager instanceof AVBitcakeManager && clientMessage.isWhite()) {
								AVBitcakeManager avBitcakeManager = (AVBitcakeManager)bitcakeManager;
								
								avBitcakeManager.recordGetTransaction(clientMessage.getOriginalSenderInfo().getId(), amountNumber);
							}
						}
					} else {
						AppConfig.timestampedErrorPrint("Transaction handler got: " + clientMessage);
					}
					
					*/	
					
					if (pendingMessage instanceof AVMarkerMessage) {
						AVMarkerMessage causalPendingMessage = (AVMarkerMessage)pendingMessage;			
						
						if (!otherClockGreater(getReceived(), causalPendingMessage.getSenderVectorClock())) {
							gotWork = true;
							
							AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
							commitedCausalMessageList.add(pendingMessage);
							//incrementClock(pendingMessage.getOriginalSenderInfo().getId());
							
							iterator.remove();
							
							break;
						}
					}
					
				}
			}
		}
		
	}
		
	


}
