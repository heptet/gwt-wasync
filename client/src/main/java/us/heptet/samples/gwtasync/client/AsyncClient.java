package us.heptet.samples.gwtwasync.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.heptet.samples.gwtwasync.common.RPCEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by kay on 7/5/2014.
 */
public class AsyncClient {
    private static Logger logger = LoggerFactory.getLogger(AsyncClient.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    private List<BlockingQueue<RPCEvent>> listeners = new ArrayList<BlockingQueue<RPCEvent>>();
    private Socket socket;
    AtmosphereClient client;
    private final RequestBuilder request;
    private int timeout = 5000;
    interface OnMessage {
        void handle(RPCEvent rpcEvent);
    }

    OnMessage onMessageHandler;

    public AsyncClient(String url) {
        logger.info("In constructor");
        //String url = "http://localhost:8080/gameserver/atm";
        assert url != null : "Url can't be null";
        client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(url)
                .encoder(new Encoder<RPCEvent, String>() {

                    @Override
                    public String encode(RPCEvent s) {
                        try {
                            return mapper.writeValueAsString(s);
                        } catch (JsonProcessingException ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                })
                .decoder(new Decoder<String, RPCEvent>() {

                    @Override
                    public RPCEvent decode(Event e, String s) {
                        s = s.trim();
                        if(s.length() == 0)
                        {
                            return null;
                        }
                        if(e.equals(Event.MESSAGE))
                        {
                            try {
				logger.debug("reading JSON {}", s);
                                return mapper.readValue(s, RPCEvent.class);
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                                return null;
                            }
                        }
                        return null;
                    }
                })
                //.transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.SSE)
                .transport(Request.TRANSPORT.LONG_POLLING);



    }
    public void connect()
    {
        assert request.uri() != null;

        logger.debug("Calling client.create");
        socket = client.create();
        logger.debug("finished client.create");
        socket.on("message", new Function<RPCEvent>() {
            @Override
            public void on(RPCEvent rpcEvent) {
                logger.debug("Received {}", rpcEvent);
                onMessageHandler.handle(rpcEvent);
                for (BlockingQueue<RPCEvent> queue : listeners) {
                    try {
                        queue.put(rpcEvent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).on(new Function<Throwable>() {

            @Override
            public void on(Throwable t) {
                t.printStackTrace();
            }
        }
        );
        logger.debug("finished socket.on");
        try {
            logger.debug("calling socket.open");
            socket.open(request.build(), timeout, TimeUnit.MILLISECONDS);
            logger.debug("finished socket.open");
        } catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        logger.info("returning from connect");
    }

    public void addQueue(BlockingQueue<RPCEvent> queue)
    {
        listeners.add(queue);
    }

    public void close()
    {
        socket.close();
    }

    public void fire(RPCEvent rpcEvent) throws IOException
    {
        socket.fire(rpcEvent);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setOnMessageHandler(OnMessage onMessageHandler) {
        this.onMessageHandler = onMessageHandler;
    }
}
