package com.mycompany.library_management_system.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** TCP framing: a signed, big-endian 32-bit length followed by UTF-8 JSON bytes. */
public final class Wire {
    public static final int MAX_FRAME_BYTES = 1024 * 1024;
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.STRICT).create();

    private Wire() { }

    public static void write(OutputStream output, Object value) throws IOException {
        byte[] payload = GSON.toJson(value).getBytes(StandardCharsets.UTF_8);
        if (payload.length == 0 || payload.length > MAX_FRAME_BYTES) {
            throw new IOException("Kích thước thông điệp vượt giới hạn 1 MiB.");
        }
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(payload.length);
        data.write(payload);
        data.flush();
    }

    public static <T> T read(InputStream input, Class<T> type) throws IOException {
        DataInputStream data = new DataInputStream(input);
        int length = data.readInt();
        if (length <= 0 || length > MAX_FRAME_BYTES) {
            throw new IOException("Độ dài thông điệp không hợp lệ (tối đa 1 MiB).");
        }
        byte[] payload = new byte[length];
        data.readFully(payload);
        try {
            String json = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(payload)).toString();
            JsonElement document = GSON.fromJson(json, JsonElement.class);
            validateDocument(document, type);
            T result = GSON.fromJson(document, type);
            if (result == null) {
                throw new IOException("Thông điệp JSON không được để trống.");
            }
            return result;
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException("Thông điệp JSON không hợp lệ.", ex);
        }
    }

    /** Gson otherwise coerces strings into numbers/booleans and absent int fields into zero. */
    private static void validateDocument(JsonElement document, Class<?> type) throws IOException {
        if (type != Request.class && type != Response.class) { return; }
        if (document == null || !document.isJsonObject()) { throw invalid(); }
        JsonObject object = document.getAsJsonObject();
        if (type == Request.class) {
            string(object, "command", false);
            string(object, "query", false);
            string(object, "code", false);
            string(object, "username", false);
            string(object, "role", false);
            string(object, "password", false);
            string(object, "udpToken", false);
            integer(object, "version", false);
            integer(object, "udpPort", false);
            integer(object, "loanId", false);
            JsonElement book = object.get("book");
            if (book != null && !book.isJsonNull()) {
                boolean update = object.has("command") && !object.get("command").isJsonNull()
                        && "UPDATE".equalsIgnoreCase(object.get("command").getAsString().strip());
                book(book, update, false);
            }
        } else {
            JsonElement success = object.get("success");
            if (success == null || !success.isJsonPrimitive() || !success.getAsJsonPrimitive().isBoolean()) {
                throw invalid();
            }
            string(object, "message", true);
            string(object, "errorCode", false);
            string(object, "username", false);
            JsonElement books = object.get("books");
            if (books == null || !books.isJsonArray()) { throw invalid(); }
            for (JsonElement value : books.getAsJsonArray()) { book(value, true, true); }
            JsonElement loans = object.get("loans");
            if (loans != null) {
                if (!loans.isJsonArray()) { throw invalid(); }
                for (JsonElement value : loans.getAsJsonArray()) {
                    if (!value.isJsonObject()) { throw invalid(); }
                    JsonObject loan = value.getAsJsonObject();
                    integer(loan, "id", true);
                    string(loan, "username", true);
                    string(loan, "code", true);
                    string(loan, "title", true);
                    string(loan, "borrowedAt", true);
                    string(loan, "returnedAt", false);
                }
            }
        }
    }

    private static void book(JsonElement value, boolean requireVersion, boolean response) throws IOException {
        if (!value.isJsonObject()) { throw invalid(); }
        JsonObject object = value.getAsJsonObject();
        string(object, "code", response);
        string(object, "title", response);
        string(object, "author", response);
        integer(object, "year", true);
        integer(object, "quantity", true);
        integer(object, "version", requireVersion);
    }

    private static void string(JsonObject object, String name, boolean required) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) {
            if (required) { throw invalid(); }
        } else if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw invalid();
        }
    }

    private static void integer(JsonObject object, String name, boolean required) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) {
            if (required) { throw invalid(); }
        } else {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) { throw invalid(); }
            try {
                if ("year".equals(name) || "quantity".equals(name) || "udpPort".equals(name)) {
                    value.getAsBigDecimal().intValueExact();
                } else {
                    value.getAsBigDecimal().longValueExact();
                }
            }
            catch (NumberFormatException | ArithmeticException ex) { throw invalid(); }
        }
    }

    private static IOException invalid() { return new IOException("Cấu trúc hoặc kiểu dữ liệu JSON không hợp lệ."); }
}
