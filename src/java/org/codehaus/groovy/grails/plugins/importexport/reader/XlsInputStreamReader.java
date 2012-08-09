package org.codehaus.groovy.grails.plugins.importexport.reader;

import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class XlsInputStreamReader implements ISReader {

    private WorkbookSettings workbookSettings = new WorkbookSettings();

    private final static String DEFAULT_ENCODING = "windows-1252";
    private final static Locale DEFAULT_LOCALE = Locale.FRANCE;

    private String encoding = DEFAULT_ENCODING;
    private Locale locale = DEFAULT_LOCALE;

    private Sheet sheet;
    private int numberOfRows;
    private int numberOfColumns;
    private int currentRow = 0;

    @Override
    public String[] readHeaders() throws Exception {

        String[] headers = new String[numberOfColumns];

        for ( int column = 0; column < numberOfColumns; column++ ) {
            headers[column] = sheet.getCell( column, 0 ).getContents();
        }

        return headers;
    }

    @Override
    public String[] readNext() throws Exception {

        currentRow++;

        if ( currentRow == numberOfRows ) {
            return null;
        }

        String[] next = new String[numberOfColumns];

        for ( int column = 0; column < numberOfColumns; column++ ) {
            CellType cellType = sheet.getCell( column, currentRow ).getType();
            if ( cellType != null && cellType == CellType.NUMBER ) {
                next[column] = sheet.getCell( column, currentRow ).getContents().replaceAll( "\r\n", " " ).replaceAll( "\n", " " ).replaceAll( ",", "." );
            } else {
                next[column] = sheet.getCell( column, currentRow ).getContents().replaceAll( "\r\n", " " ).replaceAll( "\n", " " );
            }
        }

        return next;
    }

    @Override
    public void setInputStream( InputStream inputStream ) throws Exception {
        workbookSettings = new WorkbookSettings();
        workbookSettings.setEncoding( encoding );
        workbookSettings.setLocale( locale );

        Workbook workbook = Workbook.getWorkbook( inputStream, workbookSettings );
        sheet = workbook.getSheet( 0 );
        numberOfRows = sheet.getRows();
        numberOfColumns = sheet.getColumns();
    }

    @Override
    public void setProperties( Map<String, Object> properties ) throws Exception {
        if ( properties.containsKey( "encoding" ) ) {
            this.encoding = (String) properties.get( "encoding" );
        }
        if ( properties.containsKey( "locale" ) ) {
            this.locale = (Locale) properties.get( "locale" );
        }
    }
}