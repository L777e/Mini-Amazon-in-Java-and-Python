package edu.duke.ece568.proj;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessageV3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GoogleProtocolIO {

    /**
     * google protocol abstract message to outputstream (send), true on success
     * @param msg
     * @param out
     * @param <T>
     * @return
     */
    public static <T extends GeneratedMessageV3> boolean messToStream(T msg, OutputStream out) {
        try {
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(out);
            byte[] data = msg.toByteArray();
            codedOutputStream.writeUInt32NoTag(data.length);
            codedOutputStream.writeRawBytes(data);
            codedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * receive .. from ..
     * @param response
     * @param in
     * @param <T>
     * @return
     */
    public static <T extends GeneratedMessageV3.Builder<?>> boolean messFromStream(T response, InputStream in) {
        try {
            CodedInputStream codedInputStream = CodedInputStream.newInstance(in);
            int len = codedInputStream.readRawVarint32();
            int limit = codedInputStream.pushLimit(len);
            response.mergeFrom(codedInputStream);
            codedInputStream.popLimit(limit);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
