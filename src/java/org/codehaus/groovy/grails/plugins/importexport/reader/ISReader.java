package org.codehaus.groovy.grails.plugins.importexport.reader;

import java.io.InputStream;
import java.util.Map;

public interface ISReader {

    void setProperties( Map<String, Object> properties ) throws Exception;

    String[] readHeaders() throws Exception;

    void setInputStream( InputStream inputStream ) throws Exception;

    String[] readNext() throws Exception;

}
