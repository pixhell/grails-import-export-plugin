package org.codehaus.groovy.grails.plugins.importexport

import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ClassPropertyFetcher
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.importexport.reader.CsvInputStreamReader
import org.codehaus.groovy.grails.plugins.importexport.reader.ISReader
import org.codehaus.groovy.grails.plugins.importexport.reader.XlsInputStreamReader
import org.springframework.web.multipart.commons.CommonsMultipartFile

class ImportConfig {

    private static final log = LogFactory.getLog( this )

    final Map fields
    final Class domainClass

    private final static def INPUT_STREAM_READERS = ["text/csv": CsvInputStreamReader,
            "application/vnd.ms-excel": XlsInputStreamReader]

    ImportConfig( GrailsDomainClass grailsDomainClass ) {

        this.domainClass = grailsDomainClass.clazz

        if ( log.debugEnabled ) {
            log.debug "Import config for domain class=[${domainClass}]"
        }

        fields = grailsDomainClass.persistentProperties.findAll { property ->

            !( property.isManyToOne() || property.isOneToOne() || property.isOneToMany() || property.isManyToMany() )

        }.collectEntries {
            def propertyName = it.name
            [( propertyName.toLowerCase() ): propertyName,
                    ( StringUtils.splitByCharacterTypeCamelCase( propertyName ).join( " " ).toLowerCase() ): propertyName]
        }

        if ( log.debugEnabled ) {
            log.debug "Discovered fields=[${fields}]"
        }

        def importConfigClosure = ClassPropertyFetcher.forClass( domainClass ).getStaticPropertyValue( "importCfg", Closure )

        if ( importConfigClosure ) {
            importConfigClosure.delegate = this
            importConfigClosure.resolveStrategy = Closure.DELEGATE_FIRST
            importConfigClosure.call()
        }

    }

    def invokeMethod( String propertyName, argsAsList ) {
        def args = argsAsList[0] ?: [:]

        if ( args.synonyms ) {

            if ( log.debugEnabled ) {
                log.debug "Field name=[${propertyName}] has following synonyms=[${args.synonyms}]"
            }

            args.synonyms.each { fields[it.toLowerCase()] = propertyName }
        }
    }

    def excludes( String... propertiesToExclude ) {

        if ( log.debugEnabled ) {
            log.debug "Excludes fields=[${propertiesToExclude}]"
        }

        propertiesToExclude?.each {
            while ( fields.values().remove( it ) ) {
            }
        }
    }

    def importFrom( CommonsMultipartFile multipartFile, Map properties = null ) {
        importFrom( new ByteArrayInputStream( multipartFile.inputStream.bytes ), multipartFile.originalFilename, properties )
    }

    def importFrom( InputStream input, String filename = null, Map properties = null ) {

        if ( log.debugEnabled ) {
            log.debug "Start import for class=[${domainClass}], filename=[${filename}]..."
        }

        def contentType = FileUtils.getContentType( input, filename )

        if ( log.debugEnabled ) {
            log.debug "Detected Content type=[${contentType}]"
        }

        def inputStreamReaderClass = INPUT_STREAM_READERS[contentType]

        assert inputStreamReaderClass != null: "Content type [${contentType}] is not supported."

        ISReader inputStreamReader = inputStreamReaderClass.newInstance()

        if ( properties ) {
            inputStreamReader.properties = properties
        }

        inputStreamReader.inputStream = input

        def headers = inputStreamReader.readHeaders();

        assert headers != null: "'Headers' not found"

        if ( log.debugEnabled ) {
            log.debug "Detected headers=[${headers}]"
        }

        def standardizedHeaders = headers.collect { fields[it.toLowerCase()] }

        if ( log.debugEnabled ) {
            log.debug "Standardized headers=[${standardizedHeaders}]"
        }

        def nextLine

        def beans = []

        while ( nextLine = inputStreamReader.readNext() ) {

            def dataAsMap = [:]

            nextLine.eachWithIndex { column, idx ->
                if ( standardizedHeaders[idx] ) {
                    dataAsMap[standardizedHeaders[idx]] = column
                }
            }

            if ( log.debugEnabled ) {
                log.debug "New line=[${dataAsMap}]"
            }

            beans << domainClass.newInstance( dataAsMap )
        }

        if ( log.debugEnabled ) {
            log.debug "Successfully imported ${beans.size()} entities"
        }

        beans
    }
}