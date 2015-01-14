package us.heptet.samples.gwtwasync.common;

import java.io.Serializable;


public class RPCEvent implements Serializable
{
    private String textMessage;
    public RPCEvent() {
    }
    public String getTextMessage()
    {
	return textMessage;
    }
    public void setTextMessage(String textMessage)
    {
	this.textMessage = textMessage;
    }
}
