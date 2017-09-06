package ch.nerdin.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;

/**
 * Handles sync.
 */
public class SyncSocket {
    private final List<String> toSendMessages = new ArrayList<String>();
    private final CountDownLatch closeLatch  = new CountDownLatch(1);


    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    public void add(String diff) {
        toSendMessages.add(diff);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        try {
            Future<Void> fut;

            for (String message : this.toSendMessages) {
                fut = session.getRemote().sendStringByFuture(message);
                fut.get(5, TimeUnit.SECONDS);
            }
            session.close(StatusCode.NORMAL, "I'm done");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
