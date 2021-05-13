package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import app.AppConfig;
import app.Cancellable;
import app.CausalShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	private SnapshotCollector snapshotCollector;
	private final static Set<Message> receivedBroadcasts = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());
	
	public SimpleServentListener(SnapshotCollector snapshotCollector) {
		this.snapshotCollector = snapshotCollector;
	}

	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				AppConfig.timestampedErrorPrint(AppConfig.myServentInfo.getId() + " salje " + clientMessage.getOriginalSenderInfo().getId());
				if(clientMessage.getOriginalSenderInfo().getId() == AppConfig.myServentInfo.getId()) {
					continue;
				}
				
				if(!receivedBroadcasts.add(clientMessage)) {
					continue;
				}
				
				CausalShared.addPendingMessage(clientMessage);
				CausalShared.checkPendingMessages();
				
	
			} catch (SocketTimeoutException timeoutEx) {
//				Uncomment the next line to see that we are waking up every second.
				AppConfig.timestampedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
