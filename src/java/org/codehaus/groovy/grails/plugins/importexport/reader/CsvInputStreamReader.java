package org.codehaus.groovy.grails.plugins.importexport.reader;

import au.com.bytecode.opencsv.CSVReader;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class CsvInputStreamReader implements ISReader {

    private final static Charset DEFAULT_CHARSET = Charset.forName( "UTF-8" );
    private final static char DEFAULT_DELIMITER = ';';

    private Character delimiter = DEFAULT_DELIMITER;
    private Charset charset = DEFAULT_CHARSET;

    private CSVReader reader;

    @Override
    public String[] readHeaders() throws Exception {
        String[] readNext = reader.readNext();
        return readNext;
    }

    @Override
    public String[] readNext() throws Exception {
        return reader.readNext();
    }

    @Override
    public void setInputStream( InputStream inputStream ) throws Exception {
        reader = new CSVReader( new java.io.InputStreamReader( new BOMAwareInputStream( inputStream ), charset ), delimiter );
    }

    /**
     * delimiter - Character charset - String
     */
    @Override
    public void setProperties( Map<String, Object> properties ) throws Exception {
        if ( properties.containsKey( "delimiter" ) ) {
            this.delimiter = (Character) properties.get( "delimiter" );
        }
        if ( properties.containsKey( "charset" ) ) {
            this.charset = Charset.forName( (String) properties.get( "charset" ) );
        }
    }
}