
/**
 * This script makes a zip with sources.
 */

def basedir = args[0]
def configDir = args[1]

def manifest = new XmlSlurper().parse(new File('${basedir}/AndroidManifest.xml'))

def version = manifest.@versionName.text()

def releaseName = "enroscar-sources-${version}"
def zipFileName = "${releaseName}.zip"

def ant = new AntBuilder()
ant.project.setProperty('basedir', configDir)
ant.sequential {
  
  echo "==== Sources release ===="
  echo "name: ${releaseName}"
  echo "base directory: ${basedir}"
  echo "config directory: ${configDir}"
  
  delete file : zipFileName
  
  zip(destfile : zipFileName) {
    fileset dir : ".", excludes : "bin/**, gen/**, *.zip"
  }

  property environment : "env"
  property file : "$configDir/release.properties"   
  property file : ant.project.getProperty('repo.access.file')
  
  def baseRemoteDir = ant.project.getProperty('scp.release.dir')
  def user = ant.project.getProperty('scp.user')
  def password = ant.project.getProperty('scp.password')
  def host = ant.project.getProperty('scp.host')
  def port = ant.project.getProperty('scp.port')
 
  scp todir : "${user}@${host}:${baseRemoteDir}/android", password : password, port : port, {
    fileset dir : ".", includes : zipFileName
  }
  
}
