grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.source.level = 1.6
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        inherits true
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenCentral()
        mavenRepo "http://repository.codehaus.org"
    }

    dependencies {
        compile 'org.apache.tika:tika-core:1.1',
                'net.sf.opencsv:opencsv:2.1',
                'net.sourceforge.jexcelapi:jxl:2.6.12',
                'org.apache.commons:commons-lang3:3.1'
    }

    plugins {

    }
}
