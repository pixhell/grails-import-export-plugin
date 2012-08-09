package org.codehaus.groovy.grails.plugins.importexport

import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ClassPropertyFetcher
import org.codehaus.groovy.grails.plugins.importexport.writer.CsvOutputStreamWriter
import org.codehaus.groovy.grails.plugins.importexport.writer.OSWriter
import org.codehaus.groovy.grails.plugins.importexport.writer.XlsOutputStreamWriter
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class ExportConfig {

    private static final log = LogFactory.getLog( this )

    final Map temporaryHeaders
    final Map headers
    final Class domainClass
    final List order = []

    private final static def OUTPUT_STREAMS_WRITERS = ["csv": CsvOutputStreamWriter, "xls": XlsOutputStreamWriter];

    private final static def CONTENT_TYPE_BY_EXTENSION = ["csv": "text/csv", "xls": "application/vnd.ms-excel"]

    ExportConfig( GrailsDomainClass grailsDomainClass ) {

        this.domainClass = grailsDomainClass.clazz

        if ( log.debugEnabled ) {
            log.debug "Export config for domain class=[${domainClass}]"
        }

        temporaryHeaders = grailsDomainClass.persistentProperties.findAll { property ->

            !( property.isManyToOne() || property.isOneToOne() || property.isOneToMany() || property.isManyToMany() )

        }.collectEntries {
            def propertyName = it.name
            [( propertyName ): StringUtils.capitalize( StringUtils.splitByCharacterTypeCamelCase( propertyName ).join( " " ) )]
        }

        if ( log.debugEnabled ) {
            log.debug "Temporary headers=[${temporaryHeaders}]"
        }

        def exportConfigClosure = ClassPropertyFetcher.forClass( domainClass ).getStaticPropertyValue( "exportCfg", Closure )

        if ( exportConfigClosure ) {
            exportConfigClosure.delegate = this
            exportConfigClosure.resolveStrategy = Closure.DELEGATE_FIRST
            exportConfigClosure.call()
        }

        headers = temporaryHeaders.sort( { o1, o2 ->

            def idx1 = order.indexOf( o1 )
            def idx2 = order.indexOf( o2 )

            if ( idx1 == idx2 ) {
                o1.compareTo( o2 )
            } else if ( idx1 >= 0 && idx2 < 0 ) {
                -1
            } else if ( idx2 >= 0 && idx1 < 0 ) {
                +1
            } else {
                idx1.compareTo( idx2 )
            }

        } as Comparator )

    }

    def invokeMethod( String propertyName, argsAsList ) {
        def args = argsAsList ? ( argsAsList[0] ?: [:] ) : [:]

        if ( args.header ) {
            temporaryHeaders[propertyName] = args.header
        }

        order << propertyName
    }

    def excludes( String... propertiesToExclude ) {

        if ( log.debugEnabled ) {
            log.debug "Excludes fields=[${propertiesToExclude}]"
        }

        propertiesToExclude?.each { temporaryHeaders.remove( it ) }

        if ( log.debugEnabled ) {
            log.debug "Headers after exclusion=[${temporaryHeaders}]"
        }
    }

    def exportTo( List beans, String filenameOrExtension, OutputStream output = null, Map properties = null ) {

        if ( log.debugEnabled ) {
            log.debug "Start export for class=[${domainClass}], filenameOrExtension=[${filenameOrExtension}]..."
        }

        int sepIndex = filenameOrExtension.lastIndexOf( "." );
        def extension = sepIndex != -1 ? filenameOrExtension.substring( sepIndex + 1 ) : filenameOrExtension

        if ( log.debugEnabled ) {
            log.debug "Detected extension=[${extension}]"
        }

        def outputStreamWriterClass = OUTPUT_STREAMS_WRITERS[extension]

        assert outputStreamWriterClass != null: "Extension [${extension}] is not supported."

        OSWriter outputStreamWriter = outputStreamWriterClass.newInstance()

        if ( properties ) {
            outputStreamWriter.properties = properties
        }

        if ( output ) {

            outputStreamWriter.outputStream = output

        } else {

            def response = RequestContextHolder.currentRequestAttributes().currentResponse

            assert response != null: "Current response not found and 'outputStream' attribute is required!"

            outputStreamWriter.outputStream = response.outputStream
            response.setHeader( "Content-disposition", "attachment; filename=${filenameOrExtension}" )
            response.contentType = CONTENT_TYPE_BY_EXTENSION[extension]

        }

        outputStreamWriter.writeHeaders( headers.values() as String[] )

        beans.each { bean ->

            def values = []

            headers.each { propertyName, header ->
                values << bean."${propertyName}" ?: ""
            }

            if ( values ) {
                outputStreamWriter.writeNext( values as String[] )
            }
        }

        outputStreamWriter.writeNext( null )

    }

}