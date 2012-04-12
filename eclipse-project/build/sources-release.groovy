
/**
 * This script makes a zip with sources.
 */

def basedir = args[0]
def configDir = args[1]

def manifest = new XmlSlurper().parse(new File("${basedir}/AndroidManifest.xml"))

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

  def loadProperties = {
    Properties p = new Properties()
    p.load(new FileInputStream(it))
    return new ConfigSlurper().parse(p)
  }
  
  def localConfig = loadProperties("${configDir}/release.properties")
  def repoAccessFile = localConfig.repo.access.file.replace('${env.INTEGRATION_HOME}', System.getenv()['INTEGRATION_HOME'])
  
  echo "Repo access: ${repoAccessFile}"

  def securedConfig = loadProperties(repoAccessFile)
    
  def baseRemoteDir = securedConfig.scp.release.dir
  def user = securedConfig.scp.user
  def password = securedConfig.scp.password
  def host = securedConfig.scp.host
  def port = securedConfig.scp.port
  echo "Login: ${user}"
  
  scp todir : "${user}@${host}:${baseRemoteDir}/enroscar/android", password : password, port : port, {
    fileset dir : ".", includes : zipFileName
  }
  
}
