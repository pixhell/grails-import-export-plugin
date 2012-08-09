package org.codehaus.groovy.grails.plugins.importexport.writer;

import java.io.OutputStream;
import java.util.Map;

public interface OSWriter {

    void setProperties( Map<String, Object> properties ) throws Exception;

    void writeHeaders( String[] headers ) throws Exception;

    void setOutputStream( OutputStream outputStream ) throws Exception;

    void writeNext( String[] next ) throws Exception;
}
