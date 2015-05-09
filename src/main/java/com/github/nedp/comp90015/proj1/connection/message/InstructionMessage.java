package com.github.nedp.comp90015.proj1.connection.message;

import filesync.Instruction;
import filesync.InstructionFactory;

/**
 * Created by nedp on 06/04/15.
 */
public class InstructionMessage extends DefaultMessage implements ClientMessage {
    private static final InstructionFactory InstructionFactory = new InstructionFactory();

    private transient Instruction inst;
    private final String instJSON;

    public InstructionMessage(Instruction inst) {
        this.inst = inst;
        this.instJSON = inst.ToJSON();
    }

    @Override
    protected MessageType messageType() {
        return MessageType.INST;
    }

    public Instruction instruction() {
        if (this.inst == null) {
            this.inst = InstructionFactory.FromJSON(this.instJSON);
        }
        return this.inst;
    }
}
