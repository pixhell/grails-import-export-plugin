package org.codehaus.groovy.grails.plugins.importexport.writer;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class CsvOutputStreamWriter implements OSWriter {

    private final static Charset DEFAULT_CHARSET = Charset.forName( "UTF-8" );
    private final static char DEFAULT_DELIMITER = ';';
    private final static char DEFAULT_BYTE_ORDER_MARK = '\ufeff';

    private Character delimiter = DEFAULT_DELIMITER;
    private Charset charset = DEFAULT_CHARSET;
    private Character byteOrderMark = DEFAULT_BYTE_ORDER_MARK;

    private CSVWriter writer;

    @Override
    public void setOutputStream( OutputStream outputStream ) throws Exception {

        java.io.OutputStreamWriter outputStreamWriter = new java.io.OutputStreamWriter( outputStream, charset );

        if ( byteOrderMark != '\0' ) {
            outputStreamWriter.write( byteOrderMark );
        }

        writer = new CSVWriter( outputStreamWriter, delimiter );
    }

    @Override
    public void setProperties( Map<String, Object> properties ) throws Exception {
        if ( properties.containsKey( "delimiter" ) ) {
            this.delimiter = (Character) properties.get( "delimiter" );
        }
        if ( properties.containsKey( "charset" ) ) {
            this.charset = (Charset) properties.get( "charset" );
        }
        if ( properties.containsKey( "byteOrderMark" ) ) {
            this.byteOrderMark = (Character) properties.get( "byteOrderMark" );
        }
    }

    @Override
    public void writeHeaders( String[] headers ) throws Exception {
        writer.writeNext( headers );
    }

    @Override
    public void writeNext( String[] next ) throws Exception {
        if ( next != null ) {
            writer.writeNext( next );
        } else {
            writer.close();
        }
    }

}