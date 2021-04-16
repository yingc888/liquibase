#!/bin/bash

current_time="$(date "+%Y.%m.%d-%H.%M.%S")"
PROPERTIES_FILE=""
JDBC_DRIVER_WGET=""
CHANGELOG_BODY="--liquibase formatted sql\n"
CHANGELOG_NAME="changelog.sql"

# -t${installer:liquibase.platformType}
#   0 = Oracle
#   1 = Postgres
#   2 = SqlServer
#   3 = DB2
#   4 = MySql
#   5 = Sybase
#   6 = MongoDB
#   7 = MariaDB
#   8 = Snowflake
#   9 = Redshift
#   10 = Sqlite
#   11 = Cassandra
#   12 = CockroachDB
# -h${installer:liquibase.hostname}
# -n${installer:liquibase.portnumber}
# -s${installer:liquibase.serviceName}
# -u${installer:liquibase.username}
# -p${installer:liquibase.password}
# -d${installer:liquibase.database}
# -o${installer:liquibase.projectName}

while getopts ":t:h:n:s:u:p:d:o:" opt; do
  case $opt in
    t) 
      platform="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    h) 
      host="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    n) 
      port="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    s) 
      service="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    u) 
      username="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    p) 
      password="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    d) 
      database="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    o) 
      project="$(echo $OPTARG | cut -c 2-)"
    ;;
    
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

echo "platform=$platform"
echo "host=$host"
echo "port=$port"
echo "service=$service"
echo "username=$username"
echo "password=$password"
echo "database=$database"
echo "project=$project"

case $platform in

  sqlite | 10 )

    echo "sqlite"
    ;;

  Oracle | oracle | 0 )
   JDBC_DRIVER_WGET="lib/ojdbc8.jar https://repo1.maven.org/maven2/com/oracle/ojdbc/ojdbc8/19.3.0.0/ojdbc8-19.3.0.0.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:oracle:thin:@$host:$port/$service\nusername: $username\npassword: $password\n"
   ;;

  postgres | postgresql | 1)
    echo -n "Postgresql"
    ;;

  mongodb | Mongodb | 6)
   JDBC_DRIVER_WGET="lib/mongodb.jar https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.8/mongo-java-driver-3.12.8.jar"
   CHANGELOG_NAME=changelog.xml
   CHANGELOG_BODY="<databaseChangeLog
        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"
        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
        xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"
        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">

</databaseChangeLog>"
   
   if mkdir -p ~/lb_workspace; then
        echo " "
   else
        echo " "
   fi

   if mkdir -p ~/lb_workspace/extensions; then
        echo " "
   else
        echo " "
   fi
   EXTENSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-mongodb/releases/latest | grep -o "mongodb-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'mongodb-'//g)
   
   wget -q --no-verbose -O ~/lb_workspace/extensions/liquibase-mongodb-${EXTENSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-mongodb/releases/download/liquibase-mongodb-${EXTENSION_LATEST_VERSION}/liquibase-mongodb-${EXTENSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: mongodb://$host:$port/$database?authSource=admin\nusername: $username\npassword: $password\nclasspath: ../extensions/liquibase-mongodb-${EXTENSION_LATEST_VERSION}.jar"
   
    ;;

  *)
    echo -n "unknown"
    ;;
esac
   if mkdir -p ~/lb_workspace; then
   	#echo "creating a Liquibase workspace folder 'LB_WORKSPACE' in your user root directory"
         echo " "
   else
   	#echo "Folder 'LB_WORKSPACE' already exists.  Creating a project folder '$PROJ_NAME'"
        echo " "
   fi
   mkdir -p ~/lb_workspace/$project
   wget -q --no-verbose -O $JDBC_DRIVER_WGET
   echo -e "$CHANGELOG_BODY" > ~/lb_workspace/$project/${CHANGELOG_NAME}
   echo -e "$PROPERTIES_FILE" > ~/lb_workspace/$project/liquibase.properties   
   echo "Here is your liquibase.properties file location:"
   tput setaf 3;echo $(dirname ~/lb_workspace/$project/liquibase.properties);tput sgr0
   echo "Here is your liquibase.properties file content:"
   tput setaf 2;cat ~/lb_workspace/$project/liquibase.properties;tput sgr0
#   while true; do
#   echo " "
#   read -p "Would you like to connect to the database $DB_NAME (Y/N)? " yn;tput sgr0
#   case $yn in
#     [Yy]* ) break;;
#     [Nn]* ) exit;;
#     * ) echo "Please answer Y or N.";;
#    esac
#   done
   cd ~/lb_workspace/$project
   if liquibase history > /dev/null 2>&1; then
      echo "Connection was successful!"
   else
      echo "Please check the following errors: "
      liquibase history      
   fi
#   read -p "Press Enter to continue"
   echo "Your project $project location is here:"
   echo $(pwd)
