import org.codehaus.groovy.grails.plugins.importexport.ExportConfig
import org.codehaus.groovy.grails.plugins.importexport.ImportConfig
import org.springframework.web.multipart.commons.CommonsMultipartFile

class ImportExportGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.0 > *"
    def title = "Import Export Plugin"
    def author = "Mathieu Perez"
    def authorEmail = "mathieu.perez@novacodex.net"
    def description = 'This plugin lets you to import/export your domain classes with XLS/CSV files'
    def documentation = "https://github.com/mathpere/grails-import-export-plugin"
    def license = 'APACHE'
    def organization = [name: 'NovaCodex', url: 'http://www.novacodex.net/']
    def developers = [[name: 'Mathieu Perez', email: 'mathieu.perez@novacodex.net']]
    def issueManagement = [system: 'github', url: 'https://github.com/mathpere/grails-import-export-plugin/issues']
    def scm = [url: 'https://github.com/mathpere/grails-import-export-plugin']

    def onChange = { event ->
        configureImportExport( event.application )
    }

    def doWithDynamicMethods = { ctx ->
        configureImportExport( application )
    }

    private configureImportExport( application ) {

        application.domainClasses.each { grailsClass ->

            def mc = grailsClass.clazz.metaClass

            def importConfig = new ImportConfig( grailsClass )

            mc.static.importFrom = { InputStream input, String filename = null, Map properties = null ->
                importConfig.importFrom( input, filename, properties )
            }

            mc.static.importFrom = {CommonsMultipartFile multipartFile, Map properties = null ->
                importConfig.importFrom( multipartFile, properties )
            }

            def exportConfig = new ExportConfig( grailsClass )

            mc.static.exportTo = { List beans, String filenameOrExtension, OutputStream output = null, Map properties = null ->
                exportConfig.exportTo( beans, filenameOrExtension, output, properties )
            }
        }
    }

}