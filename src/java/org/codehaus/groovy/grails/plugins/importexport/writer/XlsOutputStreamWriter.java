package org.codehaus.groovy.grails.plugins.importexport.writer;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.OutputStream;
import java.util.Map;

public class XlsOutputStreamWriter implements OSWriter {

    private final static boolean DEFAULT_AUTOFIT_COLUMNS_WIDTH = true;

    private WritableCellFormat headerCellFormat = new WritableCellFormat();
    private WritableCellFormat valueCellFormat = new WritableCellFormat();

    private String sheetName = "default";
    private boolean autoFitColumnsWidth = DEFAULT_AUTOFIT_COLUMNS_WIDTH;

    private WritableWorkbook workbook;
    private WritableSheet sheet;
    private int lineNumber = 0;

    @Override
    public void setOutputStream( OutputStream outputStream ) throws Exception {
        workbook = Workbook.createWorkbook( outputStream );

        sheet = workbook.createSheet( sheetName, 0 );

        headerCellFormat.setBackground( Colour.VERY_LIGHT_YELLOW );
        headerCellFormat.setFont( new WritableFont( WritableFont.ARIAL, 10, WritableFont.BOLD, false ) );
        headerCellFormat.setBorder( Border.ALL, BorderLineStyle.THIN );

        valueCellFormat.setBorder( Border.ALL, BorderLineStyle.THIN );
    }

    @Override
    public void setProperties( Map<String, Object> properties ) throws Exception {
        if ( properties.containsKey( "sheetName" ) ) {
            this.sheetName = (String) properties.get( "sheetName" );
        }
        if ( properties.containsKey( "autoFitColumnsWidth" ) ) {
            this.autoFitColumnsWidth = (Boolean) properties.get( "autoFitColumnsWidth" );
        }
    }

    @Override
    public void writeHeaders( String[] headers ) throws Exception {
        for ( int j = 0; j < headers.length; j++ ) {
            sheet.addCell( new Label( j, lineNumber, headers[j], headerCellFormat ) );
        }
        lineNumber++;
    }

    @Override
    public void writeNext( String[] next ) throws Exception {

        if ( next != null ) {
            for ( int j = 0; j < next.length; j++ ) {

                WritableCell cell = null;

                if ( next[j] != null ) {

                    // if (next[j] instanceof Integer) {
                    // cell = new jxl.write.Number(j, lineNumber,
                    // ((Integer) next[j]), valueCellFormat);
                    // } else if (o[j] instanceof Double) {
                    // cell = new jxl.write.Number(j, lineNumber, ((Double)
                    // o[j]),
                    // valueCellFormat);
                    // } else {
                    // cell = new Label(j, lineNumber, next[j].toString(),
                    // valueCellFormat);
                    // }

                    cell = new Label( j, lineNumber, next[j].toString(), valueCellFormat );

                } else {
                    cell = new Label( j, lineNumber, "", valueCellFormat );
                }

                sheet.addCell( cell );
            }
            lineNumber++;

        } else {
            if ( autoFitColumnsWidth ) {
                int columns = sheet.getColumns();
                for ( int i = 0; i < columns; i++ ) {
                    CellView columnView = sheet.getColumnView( i );
                    columnView.setAutosize( true );
                    sheet.setColumnView( i, columnView );
                }
            }
            workbook.write();
            workbook.close();
        }
    }
}