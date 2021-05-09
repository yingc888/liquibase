import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ProjectSetup {
    static String CHANGELOG_NAME = "changelog.sql";
    static String CHANGELOG_BODY = "-- liquibase formatted sql";
    static String LATEST_VERSION = "4.3.3";
    static String currentUsersHomeDir = System.getProperty("user.home");
	static int platformType;
	static String hostname;
	static String platform;
	static String port;
	static String service;
	static String username;
	static String password;
	static String database;
	static String project;
    static String fileSeparator = System.getProperty("file.separator");
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
	

    public static void main(String[] args) throws Exception {
        
        readInstallerEnvVariables(args);
        createDirectoryStructure();
        buildJdbcUrl();
        connectToLiquibase();
    }
	//read installer environment variables

    static void readInstallerEnvVariables(String[] args) {
        //  platformType = Integer.parseInt(System.getenv("installer:liquibase.platformType"));	
		//  platform = map.get(platformType);
        //  hostname = System.getenv("installer:liquibase.hostname");
        //  port = System.getenv("installer:liquibase.portnumber");
        //  service = System.getenv("installer:liquibase.serviceName");
        //  username = System.getenv("installer:liquibase.username");
        //  password = System.getenv("installer:liquibase.password");
        //  database= System.getenv("installer:liquibase.database");
		//  project = System.getenv("installer:liquibase.projectName");
        
        for (int i = 0; i < args.length; i++){
            String arg = args[i];
            if (arg.contains("--dbms=") || arg.contains("-t=")) {
                platform=arg.substring(arg.lastIndexOf("=") + 1);
            }
            
            else if (arg.contains("--host=") || arg.contains("-h=")) {
                hostname=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--port=") || arg.contains("-n=")) {
                port=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--username=") || arg.contains("-u=")) {
                username=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--password=") || arg.contains("-p=")) {
                password=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--service=") || arg.contains("-s=")) {
                service=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--database=") || arg.contains("-d=")) {
                database=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else if (arg.contains("--project=") || arg.contains("-o=")) {
                project=arg.substring(arg.lastIndexOf("=") + 1);
            }

            else {
                
            }
        }
        // printing all properties
        // System.out.println("hostname="+hostname+""+" platform="+platform+""+" port="+port+""+" service="+service+" database="+database+" project="+project+" username="+username+" password="+password);
        
        // if (platform == "") {
        //             System.err.println("Usage: ParseCmdLine [-verbose] [-xn] [-output afile] filename");
        // } else
        // System.out.println("Success!");
    }
    
    //     for(int i = 0; i<args.length; i++) {
    //         switch(i) {
    //             case 0 :
    //                LATEST_VERSION = args[i]; 
    //                break;
    //             case 1 :
    //                platformType = Integer.parseInt(args[i]);
    //                platform = map.get(platformType);
    //                break;
    //             case 2 :
    //                hostname = args[i]; 
    //                break;         
    //             case 3 :
    //                port = args[i];  
    //                break;    
    //             case 4 :
    //                username = args[i];
    //                break;
    //             case 5 :
    //                password = args[i]; 
    //                break;         
    //             case 6 :
    //                 database = args[i];
    //                 break;
    //             case 7 :
    //                 project = args[i];   
    //                 break;       
    //             case 8 :
    //                 service = args[i];
    //                 break;
    //          }
        
    //     }
	//construct jdbc url based on platform type,download drivers
    static void buildJdbcUrl() throws Exception {
        String jdbcUrl="";
		URL url=null;
		String driverJarFile="";
        String extensionJarFile=currentUsersHomeDir+fileSeparator+"lb_workspace"+fileSeparator+"extensions"+fileSeparator+"liquibase-"+platform+"-"+LATEST_VERSION+".jar";
        if(platform.equalsIgnoreCase("oracle")) {
            jdbcUrl = "url: jdbc:oracle:thin:@"+hostname+":"+port+"/"+service+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
			url = new URL("https://repo1.maven.org/maven2/com/oracle/ojdbc/ojdbc8/19.3.0.0/ojdbc8-19.3.0.0.jar");
			
        } else if(platform.equalsIgnoreCase("postgres")) {
            jdbcUrl = "url: jdbc:postgresql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar");
            
        } else if(platform.equalsIgnoreCase("sqlserver")){
            jdbcUrl = "url: jdbc:sqlserver://"+hostname+":"+port+";"+"databaseName="+database+";"+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/6.2.2.jre8/mssql-jdbc-6.2.2.jre8.jar");
            
        } else if(platform.equalsIgnoreCase("db2")){
            jdbcUrl = "url: jdbc:db2://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/com/ibm/db2/jcc/11.1.4.4/jcc-11.1.4.4.jar");
            
        } else if(platform.equalsIgnoreCase("mysql")){
            jdbcUrl = "url: jdbc:mysql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar");
            
        } else if(platform.equalsIgnoreCase("cockroachdb")){
            platform = "postgres";
            jdbcUrl = "url: jdbc:postgresql://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar");
            
        } else if(platform.equalsIgnoreCase("mariadb")){
            jdbcUrl = "url: jdbc:mariadb://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.0/mariadb-java-client-2.6.0.jar");
            
        } else if(platform.equalsIgnoreCase("h2")){
            jdbcUrl = "url: jdbc:h2:file:./"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("http://blah.meh.com/my/path");
            
        } else if(platform.equalsIgnoreCase("sybase")){
            jdbcUrl = "url: jdbc:jtds:sybase://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/net/sf/squirrel-sql/plugins/sybase/3.5.0/sybase-3.5.0.jar");
            
        } else if(platform.equalsIgnoreCase("sqlite")){
            jdbcUrl = "url: jdbc:sqlite:"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\n";
            url = new URL("https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.31.1/sqlite-jdbc-3.31.1.jar");
            
        } else if(platform.equalsIgnoreCase("mongodb")){
            CHANGELOG_NAME = "changelog.xml";
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar");
            downloadDriver(url, extensionJarFile);
            jdbcUrl = "url: mongodb://"+hostname+":"+port+"/"+database+"?authSource=admin"+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n";
            url = new URL("https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.8/mongo-java-driver-3.12.8.jar");
            CHANGELOG_BODY = "<databaseChangeLog\n\txmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n\txmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"\n\txsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd\n\thttp://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n\n\n</databaseChangeLog>";
            
        } else if(platform.equalsIgnoreCase("vertica")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"Database-"+LATEST_VERSION+"/liquibase-"+platform+"Database-"+LATEST_VERSION+".jar");
            downloadDriver(url, extensionJarFile);
            jdbcUrl = "url: jdbc:vertica://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n";
//The following url to jar will not download with openStream 
            url = new URL("https://www.vertica.com/client_drivers/10.0.x/10.0.0-0/vertica-jdbc-10.0.0-0.jar");
            
        } else if(platform.equalsIgnoreCase("snowflake")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar");
            downloadDriver(url, extensionJarFile);
            jdbcUrl = "url: jdbc:snowflake://"+hostname+":"+port+"/?db="+database+"&schema=public"+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n";
            url = new URL("https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.1/snowflake-jdbc-3.13.1.jar");
            
        } else if(platform.equalsIgnoreCase("redshift")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar");
            downloadDriver(url, extensionJarFile);;
            jdbcUrl = "url: jdbc:redshift://"+hostname+":"+port+"/"+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n";
            url = new URL("https://repository.mulesoft.org/nexus/content/repositories/public/com/amazon/redshift/redshift-jdbc42/1.2.1.1001/redshift-jdbc42-1.2.1.1001.jar");
            
        } else if(platform.equalsIgnoreCase("cassandra")){
            url = new URL("https://github.com/liquibase/liquibase-"+platform+"/releases/download/liquibase-"+platform+"-"+LATEST_VERSION+"/liquibase-"+platform+"-"+LATEST_VERSION+".jar");
            downloadDriver(url, extensionJarFile);
            jdbcUrl = "url: jdbc:cassandra://"+hostname+":"+port+"/"+database+";DefaultKeyspace="+database+"\n"+"username: "+username+"\n"+"password: "+password+"\nclasspath: ../extensions/liquibase-"+platform+"-"+LATEST_VERSION+".jar\n";
// We need to find a way to do the following to get the driver:
//    wget -q --no-verbose -O temp.zip https://downloads.datastax.com/jdbc/cql/2.0.8.1009/SimbaCassandraJDBC42-2.0.8.1009.zip
//    unzip -qq temp.zip
//    mv CassandraJDBC42.jar lib/CassandraJDBC42.jar
//    rm temp.zip EULA.txt
            url = new URL("");   
        }
        driverJarFile = "lib/"+platform+".jar";
        String PROPERTIES_FILE="changeLogFile: "+CHANGELOG_NAME+"\n"+jdbcUrl;
        downloadDriver(url, driverJarFile);
        createProjectFiles(PROPERTIES_FILE, CHANGELOG_NAME, CHANGELOG_BODY);
        System.out.println("Here is the properties file content in the following project file path "+currentUsersHomeDir+fileSeparator+"lb_workspace"+fileSeparator+project);
        System.out.println(PROPERTIES_FILE);
    }
	//Downloads jdbc driver from maven repository
	static void downloadDriver(URL url, String fileName) {
        try {
            InputStream inputStream= url.openStream();
            Files.copy(inputStream, Paths.get(fileName));
        }catch(IOException e) {
			
        }
	}
	//Creates directory structure needed to transfer files
    static void createDirectoryStructure() {
        try {

            Path path_to_workspace = Paths.get(currentUsersHomeDir + fileSeparator + "lb_workspace");
            Files.createDirectories(path_to_workspace);
            Path path_to_extensions = Paths.get(currentUsersHomeDir+ fileSeparator +"lb_workspace" + fileSeparator +"extensions");
            Files.createDirectories(path_to_extensions);
            Path path_to_project = Paths.get(currentUsersHomeDir+ fileSeparator +"lb_workspace"+ fileSeparator + project);
            Files.createDirectories(path_to_project);

        } catch (IOException e) {

            System.err.println("Failed to create directory!" + e.getMessage());
            }
    }
    
    static void createProjectFiles(String PROPERTIES_FILE, String CHANGELOG_NAME, String CHANGELOG_BODY) {
        try {
            
            File prop = new File(currentUsersHomeDir+ fileSeparator +"lb_workspace"+ fileSeparator +project + fileSeparator +"liquibase.properties");
            FileWriter myWriter = new FileWriter(prop);
            myWriter.write(PROPERTIES_FILE);
            myWriter.close();
            File chlog = new File(currentUsersHomeDir + fileSeparator + "lb_workspace" + fileSeparator + project + fileSeparator + CHANGELOG_NAME);
            myWriter = new FileWriter(chlog);
            myWriter.write(CHANGELOG_BODY);
            myWriter.close();
            
        } catch (IOException e) {

            System.err.println("Failed to create directory!" + e.getMessage());
            }
    }
	//Run liquibase command to connect to database
    static void connectToLiquibase() {
        String os = System.getProperty("os.name");
        // System.out.println(os);
        String command;
        String projectPath = currentUsersHomeDir + fileSeparator + "lb_workspace" + fileSeparator + project + fileSeparator;
        if(os.contains("Win")) {
            command = "liquibase.bat history";
        }
        else {
            command = "liquibase history";
        }
  
    try {       
            Process process = Runtime.getRuntime().exec(command, null, new File(projectPath));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}