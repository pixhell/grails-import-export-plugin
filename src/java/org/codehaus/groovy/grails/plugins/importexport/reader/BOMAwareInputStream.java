package org.codehaus.groovy.grails.plugins.importexport.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * recognizes BOM and skips them while reading
 */
public class BOMAwareInputStream extends InputStream {

    private PushbackInputStream pushbackInputStream;

    private static final int BOM_LENGTH = 4;

    private boolean init = false;

    public BOMAwareInputStream( InputStream in ) {
        pushbackInputStream = new PushbackInputStream( in, BOM_LENGTH );
    }

    protected void init() throws IOException {

        byte bom[] = new byte[BOM_LENGTH];
        int n = pushbackInputStream.read( bom, 0, bom.length );
        int unread = 0;

        if ( ( bom[0] == (byte) 0x00 ) && ( bom[1] == (byte) 0x00 ) && ( bom[2] == (byte) 0xFE ) && ( bom[3] == (byte) 0xFF ) ) {
            unread = n - 4;
        } else if ( ( bom[0] == (byte) 0xFF ) && ( bom[1] == (byte) 0xFE ) && ( bom[2] == (byte) 0x00 ) && ( bom[3] == (byte) 0x00 ) ) {
            unread = n - 4;
        } else if ( ( bom[0] == (byte) 0xEF ) && ( bom[1] == (byte) 0xBB ) && ( bom[2] == (byte) 0xBF ) ) {
            unread = n - 3;
        } else if ( ( bom[0] == (byte) 0xFE ) && ( bom[1] == (byte) 0xFF ) ) {
            unread = n - 2;
        } else if ( ( bom[0] == (byte) 0xFF ) && ( bom[1] == (byte) 0xFE ) ) {
            unread = n - 2;
        } else {
            unread = n;
        }

        if ( unread > 0 ) {
            pushbackInputStream.unread( bom, ( n - unread ), unread );
        }
    }

    public void close() throws IOException {
        pushbackInputStream.close();
    }

    public int read() throws IOException {
        if ( !init ) {
            init();
            init = true;
        }
        return pushbackInputStream.read();
    }
}