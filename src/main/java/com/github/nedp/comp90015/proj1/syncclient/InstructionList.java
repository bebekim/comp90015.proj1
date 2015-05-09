package com.github.nedp.comp90015.proj1.syncclient;

import com.github.nedp.comp90015.proj1.connection.TCPConnection;
import com.github.nedp.comp90015.proj1.util.Log;
import com.github.nedp.comp90015.proj1.connection.message.*;
import filesync.CopyBlockInstruction;
import filesync.Instruction;
import filesync.NewBlockInstruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nedp on 06/04/15.
 */
public class InstructionList implements Update {
    private final List<Instruction> instList = new ArrayList<>();

    // NOTE: moves the contents of the input list.
    public InstructionList(List<Instruction> instList) {
        this.instList.addAll(instList);
        instList.clear();
    }

    @Override
    public void sendThrough(TCPConnection conn) throws BadMessageException, IOException {
        while (!this.instList.isEmpty()) {
            final Instruction inst = this.instList.remove(0);

            final DefaultMessage msg = new InstructionMessage(inst);
            final String json = msg.toJSON();

            // Send the instruction.
            try {
                conn.write(json);
            } catch (IOException e) {
                throw new IOException("sending json through connection: " + e.getMessage(), e);
            }

            // Get the response.
            final ServerMessage response;
            try {
                response = JSONBoxedMessage.ReadFrom(conn).toMessage().toServerMessage();
            } catch (IOException e) {
                throw new IOException("reading server response: " + e.getMessage(), e);
            }

            // If the response is a request for upgrade, and the instruction was a
            // CopyBlockInstruction, upgrade it to a NewBlockInstruction.
            if (response instanceof UpgradeMessage) {
                if (inst instanceof CopyBlockInstruction) {
                    this.instList.add(0, new NewBlockInstruction((CopyBlockInstruction) inst));
                    continue;
                }
                throw new BadMessageException("asked to upgrade non-CopyBlockInstruction");
            }

            // If the response is a success report, continue to the next instruction.
            if (response instanceof SuccessMessage) {
                Log.F("Instruction received successfully.");
                continue;
            }

            // Otherwise fail loudly.
            throw new BadMessageException("unexpected response: {" + response.toJSON() + "}");
        }
    }
}
