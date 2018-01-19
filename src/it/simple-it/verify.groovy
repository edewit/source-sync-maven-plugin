import org.eclipse.jetty.websocket.client.*
import ch.nerdin.minecraft.SyncSocket
import java.util.concurrent.*
import fi.iki.elonen.NanoWSD

def ws = new NanoWSD(9191, true)

def change = """
--- a/text.txt
+++ b/text.txt
@@ -1 +1 @@
- This is some text :)
+ This is some better text!
"""

ws.start()

def client = new WebSocketClient()
def socket = new SyncSocket()
socket.getToSendMessages().add(change)

client.start()
def uri = new URI("ws://localhost:9191")

def request = new ClientUpgradeRequest()
client.connect(socket, uri, request)
socket.awaitClose(5, TimeUnit.SECONDS)

assert "This is some better text!" == new File('text.txt').text