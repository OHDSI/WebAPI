package org.ohdsi.webapi.report;

import java.util.List;

/**
 * Created by taa7016 on 10/4/2016.
 */
public class CDMAchillesHeel {

    private List<CDMAttribute> messages;

    public void setMessages(List<CDMAttribute> messages) {
        this.messages = messages;
    }

    public List<CDMAttribute> getMessages(){
        return messages;
    }
}
