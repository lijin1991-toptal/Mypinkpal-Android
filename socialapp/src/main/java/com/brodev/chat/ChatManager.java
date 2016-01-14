package com.brodev.chat;

import com.quickblox.chat.model.QBChatMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

/**
 * Created by Bebel on 1/12/15.
 */
public interface ChatManager {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;
}
