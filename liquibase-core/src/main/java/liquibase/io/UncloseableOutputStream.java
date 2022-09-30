package liquibase.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is a wrapper around OutputStreams, and makes them impossible for callers to close.
 */
public class UncloseableOutputStream extends FilterOutputStream {
    public UncloseableOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * This method does not actually close the underlying stream, but rather only flushes it. Callers should not be
     * closing the stream they are given.
     */
    @Override
    public void close() throws IOException {
        out.flush();
    }

    /**
     * This method should be used with extreme caution. It closes the underlying stream which might have unintended
     * consequences for other consumers of this stream.
     */
    public void closeUnderlyingStream() throws IOException {
        super.close();
    }
}
