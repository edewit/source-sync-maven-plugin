package ch.nerdin.minecraft;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Handles sync.
 */
@WebSocket
public class SyncSocket {

    private final List<String> toSendMessages = new ArrayList<>();
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    public boolean awaitClose(int duration, TimeUnit unit) {
        try {
            return this.closeLatch.await(duration, unit);
        } catch (InterruptedException e) {
            Logger.getLogger(SyncSocket.class.getName()).log(SEVERE, "Oups, interrupted while having fun", e);
            return false;
        }
    }

    public void add(String diff) {
        toSendMessages.add(diff);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        try {
            for (String message : this.toSendMessages) {
                Future<Void> fut = session.getRemote().sendStringByFuture(message);
                fut.get(5, TimeUnit.SECONDS);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        if ("applied".equals(message)) {
            System.out.println("changes applied!");
            session.disconnect();
            closeLatch.countDown();
        }
    }
}
