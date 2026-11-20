package com.mycompany.library_management_system.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/** Wire frame carried in one UDP packet, with a random token tied to the TCP session. */
public record NoticeDatagram(String token, LibraryNotice notice) {
    public static final int MAX_BYTES = 1200;

    public byte[] encode() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Wire.write(output, this);
        if (output.size() > MAX_BYTES) throw new IOException("Thông báo UDP quá dài.");
        return output.toByteArray();
    }

    public static NoticeDatagram decode(DatagramPacket packet) throws IOException {
        if (packet.getLength() > MAX_BYTES) throw new IOException("Thông báo UDP quá dài.");
        var input = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
        if (input.available() < 4) throw new IOException("Thông báo UDP không hợp lệ.");
        // Check the prefix before asking Wire to allocate the payload buffer.
        int length = new java.io.DataInputStream(input).readInt();
        if (length != input.available()) throw new IOException("Độ dài thông báo UDP không hợp lệ.");
        input.reset();
        return Wire.read(input, NoticeDatagram.class);
    }
}
