#!/bin/bash

current_time="$(date "+%Y.%m.%d-%H.%M.%S")"
PROPERTIES_FILE=""
JDBC_DRIVER_WGET=""
CHANGELOG_BODY="--liquibase formatted sql\n"
CHANGELOG_NAME="changelog.sql"
PROKEY=""
APIKEY=""

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

   sampleH2 | sampleh2 | 2)
   if mkdir -p ~/lb_workspace; then echo " ";else echo " "; fi
   mkdir -p ~/lb_workspace/$project
   cp -a examples/. ~/lb_workspace/$project
   echo "Here is your prject location:"
   tput setaf 3;echo $(dirname ~/lb_workspace/$project/start-h2);tput sgr0
   echo "For information on using and configuring the h2 database see: 
https://www.liquibase.org/documentation/tutorials/h2.html"
   exit
    ;;

   Vertica | vertica | 21 )

   JDBC_DRIVER_WGET="lib/vertica.jar https://www.vertica.com/client_drivers/10.0.x/10.0.0-0/vertica-jdbc-10.0.0-0.jar"
   if mkdir -p ~/lb_workspace; then echo " " ;else echo " ";fi
   if mkdir -p ~/lb_workspace/extensions; then echo " ";else echo " ";fi
   EXTENSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-vertica/releases/latest | grep -o "vertica-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'vertica-'//g)
   wget -q --no-verbose -O ~/lb_workspace/extensions/liquibase-vertica-${EXTENSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-vertica/releases/download/liquibase-vertica-${EXTENSION_LATEST_VERSION}/liquibase-vertica-${EXTENSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:vertica://$host:$port/${database}\nusername: $username\npassword: $password\ndriver: com.vertica.jdbc.Driver\nclasspath: ../extensions/liquibase-vertica-${EXTENSION_LATEST_VERSION}.jar\n"
    ;;

   MariaDB | mariadb |20)

   JDBC_DRIVER_WGET="lib/mariadb.jar https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.6.0/mariadb-java-client-2.6.0.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:mariadb://$host:$port/$database\nusername: $username\npassword: $password\n"
    ;;

   CockroachDB | cockroachdb | Cockroachdb |20)

   JDBC_DRIVER_WGET="lib/postgresql.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:postgresql://$host:$port/$database\nusername: $username\npassword: $password\n"
    ;;

   Redshift | redshift | 12 )

   JDBC_DRIVER_WGET="lib/redshift.jar https://repository.mulesoft.org/nexus/content/repositories/public/com/amazon/redshift/redshift-jdbc42/1.2.1.1001/redshift-jdbc42-1.2.1.1001.jar"
   if mkdir -p ~/lb_workspace; then echo " " ;else echo " ";fi
   if mkdir -p ~/lb_workspace/extensions; then echo " ";else echo " ";fi
   EXTENSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-redshift/releases/latest | grep -o "redshift-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'redshift-'//g)
   wget -q --no-verbose -O ~/lb_workspace/extensions/liquibase-redshift-${EXTENSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-redshift/releases/download/liquibase-redshift-${EXTENSION_LATEST_VERSION}/liquibase-redshift-${EXTENSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:redshift://$host:$port/${database}\nusername: $username\npassword: $password\nclasspath: ../extensions/liquibase-redshift-${EXTENSION_LATEST_VERSION}.jar"
    ;;

   H2 | h2| 17)

   JDBC_DRIVER_WGET="/dev/null http://blah.meh.com/my/path"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:h2:file:./$database\nusername: $username\npassword: $password\n"
    ;;

   Sybase | sybase | 16)

   JDBC_DRIVER_WGET="lib/sybase.jar https://repo1.maven.org/maven2/net/sf/squirrel-sql/plugins/sybase/3.5.0/sybase-3.5.0.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:jtds:sybase://$host:$port/$database\nusername: $username\npassword: $password\ndriver: net.sourceforge.jtds.jdbc.Driver\n"
    ;;

   DB2 | db2 | 15)

   JDBC_DRIVER_WGET="lib/db2.jar https://repo1.maven.org/maven2/com/ibm/db2/jcc/11.1.4.4/jcc-11.1.4.4.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:db2://$host:$port/$database\nusername: $username\npassword: $password\n"
    ;;

   SqlServer | mssql | sqlserver | 14)

   JDBC_DRIVER_WGET="lib/mssql.jar https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/6.2.2.jre8/mssql-jdbc-6.2.2.jre8.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:sqlserver://$host:$port;databaseName=$database;\nusername: $username\npassword: $password\n"
    ;;

  cassandra | 13 )

   JDBC_DRIVER_WGET="/dev/null http://blah.meh.com/my/path"
   wget -q --no-verbose -O temp.zip https://downloads.datastax.com/jdbc/cql/2.0.8.1009/SimbaCassandraJDBC42-2.0.8.1009.zip
   unzip -qq temp.zip
   mv CassandraJDBC42.jar lib/CassandraJDBC42.jar
   rm temp.zip EULA.txt
   if mkdir -p ~/lb_workspace; then echo " " ;else echo " ";fi
   if mkdir -p ~/lb_workspace/extensions; then echo " ";else echo " ";fi
   EXTENSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-cassandra/releases/latest | grep -o "cassandra-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'cassandra-'//g)
   wget -q --no-verbose -O ~/lb_workspace/extensions/liquibase-cassandra-${EXTENSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-cassandra/releases/download/liquibase-cassandra-${EXTENSION_LATEST_VERSION}/liquibase-cassandra-${EXTENSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:cassandra://$host:$port/${database};DefaultKeyspace=${database}\nusername: $username\npassword: $password\nclasspath: ../extensions/liquibase-cassandra-${EXTENSION_LATEST_VERSION}.jar"
    ;;

  snowflake | 12 )

   JDBC_DRIVER_WGET="lib/snowflake.jar https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.13.1/snowflake-jdbc-3.13.1.jar"
   if mkdir -p ~/lb_workspace; then echo " " ;else echo " ";fi
   if mkdir -p ~/lb_workspace/extensions; then echo " ";else echo " ";fi
   EXTENSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-snowflake/releases/latest | grep -o "snowflake-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'snowflake-'//g)
   wget -q --no-verbose -O ~/lb_workspace/extensions/liquibase-snowflake-${EXTENSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-snowflake/releases/download/liquibase-snowflake-${EXTENSION_LATEST_VERSION}/liquibase-snowflake-${EXTENSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:snowflake://$host:$port/?db=${database}&schema=public\nusername: $username\npassword: $password\nclasspath: ../extensions/liquibase-snowflake-${EXTENSION_LATEST_VERSION}.jar"
    ;;

  mysql | 11 )

   JDBC_DRIVER_WGET="lib/mysql.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:mysql://$host:$port/$database\nusername: $username\npassword: $password\n"
    ;;