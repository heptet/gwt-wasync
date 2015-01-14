package us.heptet.samples.gwtwasync.server;

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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import us.heptet.samples.gwtwasync.common.RPCEvent;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Integration test class for "asyncweb" web application that encapsulates
 * the asynchronous atmosphere portion of the application.
 *
 * Created by kay on 6/25/2014.
 *
 */

public class ITCase {
    private static Logger logger = LoggerFactory.getLogger(ITCase.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    String propertyProjectPart = "gwt-wasync";
    String propertyModulePart = "gwt-wasync";
    String propertyPrefix = propertyProjectPart + "." + propertyModulePart + ".";
    String hostProperty = propertyPrefix + "host";
    String portProperty = propertyPrefix + "port";
    String contextPathProperty = propertyPrefix + "contextPath";
    String managedPathProperty = propertyPrefix + "managedPath";
    String webAppUrlProperty = propertyPrefix + "webAppUrl";

    private String contextPath = "/" + /*propertyProjectPart + "-" + */propertyModulePart;// + "-0";
    private String host = "localhost";
    private String managedPath = "/atmosphere";
    private int port = 5040;
    private String webAppUrl;
    private String managedEndpoint;

    @BeforeMethod
    public void setUp() throws Exception {
        //mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        host = System.getProperty(hostProperty, host);
        port = Integer.parseInt(System.getProperty(portProperty, String.valueOf(port)));
        contextPath = System.getProperty(contextPathProperty, contextPath);
        managedPath = System.getProperty(managedPathProperty, managedPath);
        webAppUrl = "http://" + host + (port == 80 ? "" : (":" + String.valueOf(port))) + contextPath;
        webAppUrl = System.getProperty(webAppUrlProperty, webAppUrl);
        URL url = new URL(webAppUrl);
        URI uri = new URI(webAppUrl);
        managedEndpoint = webAppUrl + managedPath;
    }

    //

    @Test
    public void testConnect() throws Exception {
        String url = managedEndpoint;
        logger.info("{}", url);
        BlockingQueue<RPCEvent> eventQueue = new LinkedBlockingQueue<>();

        connect(url, eventQueue);
    }

    class ConnectInfo
    {
        AtmosphereClient client;
        Socket socket;
    }
    public ConnectInfo connect(String url, final BlockingQueue<RPCEvent> eventQueue) throws Exception
    {
        ConnectInfo connectInfo = new ConnectInfo();
       RPCEvent testRpcEvent = new RPCEvent();
        logger.info("{}", mapper.writeValueAsString(testRpcEvent));
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        RequestBuilder request = client.newRequestBuilder()
                //.header("")
                .method(Request.METHOD.GET)
                .uri(url)
//                .trackMessageLength(true)
                .encoder(new Encoder<RPCEvent, String>() {
                    @Override
                    public String encode(RPCEvent data) {
                        try {
                            String s = mapper.writeValueAsString(data);
                            logger.info("encoder returning {}", s);
                            return s;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, RPCEvent>() {
                    @Override
                    public RPCEvent decode(Event type, String data) {

                        data = data.trim();

                        // Padding
                        if (data.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            logger.info("in Decoder with message = {}", data);
                            atomicBoolean.set(true);
                            try {
                                return mapper.readValue(data, RPCEvent.class);
                            } catch (IOException e) {
                                e.printStackTrace();
                                logger.debug("Invalid message {}", data);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.SSE)
                .transport(Request.TRANSPORT.LONG_POLLING);

        final Socket socket = client.create();
        socket.on("message", new Function<RPCEvent>() {
            @Override
            public void on(RPCEvent t) {
                logger.info("on message with {}", t);
                try {
                    eventQueue.put(t);
                } catch(Exception ex)
                {
                    ex.printStackTrace(System.err);
                }
            }
        }).on(new Function<Throwable>() {

            @Override
            public void on(Throwable t) {
                logger.info("on throwable");
                t.printStackTrace();
            }

        }).on(Event.CLOSE.name(), new Function<String>() {
            @Override
            public void on(String t) {
                logger.info("Connection closed");

            }
        }).on(Event.ERROR.name(), new Function<Object>() {
            @Override
            public void on(Object o) {
                logger.info("error");
            }

        }).on(Event.OPEN.name(), new Function<String>() {
            @Override
            public void on(String t) {
                logger.info("Connection opened");
            }
        })
                .open(request.build());

        connectInfo.client = client;
        connectInfo.socket = socket;
        return connectInfo;
    }
}
