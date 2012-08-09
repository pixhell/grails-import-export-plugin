package org.codehaus.groovy.grails.plugins.importexport

import org.apache.commons.logging.LogFactory
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.apache.tika.mime.MimeTypes

class FileUtils {

    private static final log = LogFactory.getLog( this )

    private static final MimeTypes MIME_TYPES = TikaConfig.defaultConfig.mimeRepository

    static def getContentType( InputStream inputStream, String fileName = null ) {

        MediaType mediaType

        try {

            def metadata = new Metadata()

            if ( fileName ) {
                metadata.set( Metadata.RESOURCE_NAME_KEY, fileName );
            }

            mediaType = MIME_TYPES.detect inputStream, metadata

            if ( log.debugEnabled )
                log.debug "Detected content type for filename=[${fileName}] is mediaType=[${mediaType}]"

        } finally {
            inputStream?.close()
        }

        mediaType?.toString()
    }

}