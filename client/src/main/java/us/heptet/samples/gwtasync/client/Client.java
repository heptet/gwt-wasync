package us.heptet.samples.gwtwasync.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.heptet.samples.gwtwasync.common.RPCEvent;

public class Client
{
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args)
    {
	AsyncClient asyncClient = new AsyncClient(args[0]);
	asyncClient.setOnMessageHandler(new AsyncClient.OnMessage()
	    {
		@Override
		public void handle(final RPCEvent rpcEvent) {
		    logger.info("received {}", rpcEvent.getTextMessage());
		}
	    }
	    );
	asyncClient.connect();
	for(;;)
	    {
		try
		    {
			asyncClient.fire(new RPCEvent("test message"));
			Thread.sleep(5000);
		    } catch(Exception ex)
		    {
			ex.printStackTrace();
		    }
	    }
    }
}
