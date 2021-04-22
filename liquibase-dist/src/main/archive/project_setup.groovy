import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
public class project_setup {
    static def CHANGELOG_NAME = "changelog.sql"
    static def LATEST_VERSION = "4.3.3"
    static def currentUsersHomeDir = System.getProperty("user.home");
	static int platformType
	static def hostname
	static def platform
	static def port
	static def service
	static def username
	static def password
	static def database
	static def project
    private static Map<Integer, String> map;
	static
    {
        map = new HashMap<>();
        map.put(0, "oracle");
        map.put(1, "postgres");
        map.put(2, "sqlserver");
	    map.put(3, "db2");
        map.put(4, "mysql");
        map.put(5, "sqlite");
        map.put(6, "h2");
        map.put(7, "cockroachdb");
        map.put(8, "mariadb");
        map.put(9, "sybase");
        map.put(10, "vertica");
        map.put(11, "snowflake");
        map.put(12, "redshift");
        map.put(13, "mongodb");
        map.put(14, "cassandra");
        map.put(15, "sampleh2");
    }
	
    static void main(String[] args){
		readInstallerEnvVariables()
        buildJdbcUrl()
        // createDirectoryStructure()
        // connectToLiquibase()
    }
	//read installer environment variables
    static void readInstallerEnvVariables() {
        //  platformType = Integer.parseInt(System.getenv("installer:liquibase.platformType"))	
		//  platform = map.get(platformType)
        //  hostname = System.getenv("installer:liquibase.hostname")
        //  port = System.getenv("installer:liquibase.portnumber")
        //  service = System.getenv("installer:liquibase.serviceName")
        //  username = System.getenv("installer:liquibase.username")
        //  password = System.getenv("installer:liquibase.password")
        //  database= System.getenv("installer:liquibase.database")
		//  project = System.getenv("installer:liquibase.projectName")
         platformType = Integer.parseInt("12")
		 platform = map.get(platformType)
         hostname = "training20.datical.net"
         port = "1521"
         service = "BUCKET_02"
         username = "HR"
         password = "password"
         database= "mydatabase"
		 project = "Postgres_Project"
    }
	//construct jdbc url based on platform type,download drivers
    static void buildJdbcUrl() {
        def jdbcUrl
		def url
		def driverJarFile
        def extensionJarFile
		def databasePlatform = (String)map.get(platformType)
        if(platform.equalsIgnoreCase("oracle")) {
            jdbcUrl = "url: jdbc:oracle:thin:@"+hostname+":"+port+"/"+service+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
			url = new URL("https://repo1.maven.org/maven2/com/oracle/ojdbc/ojdbc8/19.3.0.0/ojdbc8-19.3.0.0.jar");
			
        } else if(platform.equalsIgnoreCase("postgres")) {
            jdbcUrl = "url: jdbc:postgresql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar");
            
        } else if(platform.equalsIgnoreCase("sqlserver")){
            jdbcUrl = "url: jdbc:sqlserver://"+hostname+":"+port+";"+"databaseName="+database+";"+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/6.2.2.jre8/mssql-jdbc-6.2.2.jre8.jar");
            
        } else if(platform.equalsIgnoreCase("db2")){
            jdbcUrl = "url: jdbc:db2://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/com/ibm/db2/jcc/11.1.4.4/jcc-11.1.4.4.jar");
            
        } else if(platform.equalsIgnoreCase("mysql")){
            jdbcUrl = "url: jdbc:mysql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar");
            
        } else if(platform.equalsIgnoreCase("cockroachdb")){
            platform = "postgres"
            jdbcUrl = "url: jdbc:postgresql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar");
            
        } else if(platform.equalsIgnoreCase("mariadb")){
            jdbcUrl = "url: jdbc:mariadb://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.0/mariadb-java-client-2.6.0.jar");
            
        } else if(platform.equalsIgnoreCase("h2")){
            jdbcUrl = "url: jdbc:h2:file:./"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("http://blah.meh.com/my/path");
            
        } else if(platform.equalsIgnoreCase("sybase")){
            jdbcUrl = "url: jdbc:jtds:sybase://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/net/sf/squirrel-sql/plugins/sybase/3.5.0/sybase-3.5.0.jar");
            
        } else if(platform.equalsIgnoreCase("sqlite")){
            jdbcUrl = "url: jdbc:sqlite:"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n"
            url = new URL("https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.31.1/sqlite-jdbc-3.31.1.jar");
            
        } else if(platform.equalsIgnoreCase("mongodb")){
            CHANGELOG_NAME = "changelog.xml"
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar")  
            extensionJarFile = "${currentUsersHomeDir}"+"/lb_workspace/extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar"
            downloadDriver(url, extensionJarFile)
            jdbcUrl = "url: mongodb://"+hostname+":"+port+"/"+database+"?authSource=admin"+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n"
            url = new URL("https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.8/mongo-java-driver-3.12.8.jar");
            
        } else if(platform.equalsIgnoreCase("vertica")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"Database-"+LATEST_VERSION+"/liquibase-"+platform+"Database-"+LATEST_VERSION+".jar")
            extensionJarFile = "${currentUsersHomeDir}"+"/lb_workspace/extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar"
            downloadDriver(url, extensionJarFile)
            jdbcUrl = "url: jdbc:vertica://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n"
            
//The following url to jar will not download with openStream 
            url = new URL("https://www.vertica.com/client_drivers/10.0.x/10.0.0-0/vertica-jdbc-10.0.0-0.jar");
            
        } else if(platform.equalsIgnoreCase("snowflake")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar")
            extensionJarFile = "${currentUsersHomeDir}"+"/lb_workspace/extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar"
            downloadDriver(url, extensionJarFile)
            jdbcUrl = "url: jdbc:snowflake://"+hostname+":"+port+"/?db="+database+"&schema=public"+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n"
            url = new URL("https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.1/snowflake-jdbc-3.13.1.jar");
            
        } else if(platform.equalsIgnoreCase("redshift")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar")
            extensionJarFile = "${currentUsersHomeDir}"+"/lb_workspace/extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar"
            downloadDriver(url, extensionJarFile)
            jdbcUrl = "url: jdbc:redshift://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n"
            url = new URL("https://repository.mulesoft.org/nexus/content/repositories/public/com/amazon/redshift/redshift-jdbc42/1.2.1.1001/redshift-jdbc42-1.2.1.1001.jar");
            
        } else if(platform.equalsIgnoreCase("cassandra")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar")
            extensionJarFile = "${currentUsersHomeDir}"+"/lb_workspace/extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar"
            downloadDriver(url, extensionJarFile)
            jdbcUrl = "url: jdbc:cassandra://"+hostname+":"+port+"/"+database+";DefaultKeyspace="+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n"
// We need to find a way to do the following to get the driver:
//    wget -q --no-verbose -O temp.zip https://downloads.datastax.com/jdbc/cql/2.0.8.1009/SimbaCassandraJDBC42-2.0.8.1009.zip
//    unzip -qq temp.zip
//    mv CassandraJDBC42.jar lib/CassandraJDBC42.jar
//    rm temp.zip EULA.txt
            url = new URL("");
            
        }
        driverJarFile = "lib/"+platform+".jar"
        def PROPERTIES_FILE="changeLogFile: "+CHANGELOG_NAME+"\n"+jdbcUrl
		downloadDriver(url, driverJarFile)
        println "${PROPERTIES_FILE}"
    }
	//Downloads jdbc driver from maven repository
	static void downloadDriver(URL url, def fileName) {
        try {
            InputStream inputStream= url.openStream()
            Files.copy(inputStream, Paths.get(fileName))
        }catch(IOException e) {
			
        }
	}
	//Creates directory structure needed to transfer files
    static void createDirectoryStructure() {
        
    }
	//Run liquibase command to connect to database
    static void connectToLiquibase() {
    }
}