package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 06/04/15.
 */
public enum MessageType {
    AUTH(Authentication.class),
    TEXT(TextMessage.class),
    INST(InstructionMessage.class),
    BAD(RefuseMessage.class),
    OK(SuccessMessage.class),
    UPGRADE(UpgradeMessage.class),
    FOLDER(FolderUpdate.class),
    ;

    public final Class<? extends DefaultMessage> type;

    MessageType(Class<? extends DefaultMessage> type) {
        this.type = type;
    }
}
