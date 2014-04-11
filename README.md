# MicroPaaS
MicroPaas est une application permettant de déployer des applications Java et Java EE. MicroPaas a été développée sous la forme d'un plugin Maven et utilise en backend [Docker.io](http://www.docker.io). Ce projet développé par l'équipe de [CloudUnit](http://www.cloudunit.fr) a été conçu à l'occasion de [DevoxxFR-2014](http://cfp.devoxx.fr/devoxxfr2014/talk/CCA-308/Comment%20d%C3%A9velopper%20un%20PaaS%20Java%20en%2030%20minutes%20avec%20docker) 

## Pré-requis 
* Docker 0.8.1 installé et accessible `sudo apt-get install lxc-docker-0.8.1`.
* Les images Docker compatibles construites dans Docker (cf : projet [micropaas-image](https://github.com/Treeptik/micropaas-image))
* Une version de Maven 3.* installée sur le poste de développement

**Note: Une box Vagrant contenant une installation de Docker et toutes les images est disponible dans le projet [micro-paas-docker-vagrant](https://github.com/Treeptik/micropaas-docker-vagrant)**

## Installation
### Version de développement
Il est possible d'installer la dernière version du plugin localement en clonant le dépôt Git du projet. 

```bash
git clone https://github.com/Treeptik/micropaas-docker-plugin.git
cd micropaas-docker-plugin
mvn install 
```
### Version stable
Il est aussi possible d'installer la dernière version stable en utilisant le dépôt Maven du projet 
```xml
<pluginRepositories>
	<pluginRepository>
		<id>micropaas-plugin</id>
		<url>http://www.treeptik.fr/repository/</url>
	</pluginRepository>
</pluginRepositories>
``` 

## Configuration
Le plugin Maven micropaas, permet, à l'aide de goal Maven de créer des container Docker et de déployer les applications Java et Java EE en fonction du type de serveur choisi. 

Pour pouvoir l'utiliser, il suffit d'ajouter la configuration du plugin dans le `pom.xml` de votre projet et d'adapter les informations de la base de données. 

Exemple de fichier *pom.xml*
```xml
<plugin>
	<groupId>fr.treeptik.micropaas.maven.plugins</groupId>
	<artifactId>micropaas-maven-plugin</artifactId>
	<version>0.0.1</version>
	<configuration>
		<urlDockerManager>http://192.168.1.103:4243</urlDockerManager>
		<containerImage>micropaas/tomcat-mysql</containerImage>
		<containerName>superProjetTest</containerName>
		<databaseName>super-database</databaseName>
	</configuration>
</plugin>
``` 

## Exemple 

Nous allons déployer l'application d'exemple [Pet clinic de Spring](https://github.com/spring-projects/spring-petclinic)

Dans un premier temps, nous allons cloner le projet

```bash
git clone https://github.com/spring-projects/spring-petclinic
cd spring-petclinic
```
 
### Configuration 
Puis ajouter la configuration du plugin Maven 

```xml
<plugin>
	<groupId>fr.treeptik.micropaas.maven.plugins</groupId>
	<artifactId>micropaas-maven-plugin</artifactId>
	<version>0.0.1</version>
	<configuration>
		<urlDockerManager>http://192.168.0.109:4243</urlDockerManager>
		<containerImage>micropaas/tomcat-mysql</containerImage>
		<containerName>petclinic-app</containerName>
	</configuration>
</plugin>
```
*Nb : Il faut noter que pour l'instant il n'y a pas de configuration de la base de données du fait que l'application embarque une base HsqlDB. Une base MySql sera ajoutée dans une autre étape*

###Déploiement
Il est maintenant possible de déployer l'application avec une simple commande Maven

```xml
mvn package -DskipTests micropaas:deploy
```

La commande compile, package puis déploie l'application sur le serveur. Si tout se passe bien vous devriez voir dans le terminal le résultat suivant : 

```bash
…
…
******************************************
******************************************
********** APPLICATION ACCESS ************
******************************************
******************************************

URL : http://192.168.0.109:49158/petclinic

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 11.238s
[INFO] Finished at: Wed Apr 09 14:55:52 CEST 2014
[INFO] Final Memory: 12M/183M
[INFO] ------------------------------------------------------------------------
                                                                                   
```
L'application est maintenant accessible via l'URL précisée dans le résultat.  

###Base de données

Nous allons maintenant modifier l'application pour utiliser la base de données Mysql disponible dans le MicroPaas et non pas la base embarquée HsqlDB fournie dans l'application.

Premièrement, il faut ajouter la dépendance MySQL au projet. Pour cela il suffit de dé-commenter la dépendance du connecteur dans le `pom.xml`

Ensuite il faut ajouter le nom de la base de données à la configuration du plugin. La base doit s'appeler *petclinic* et se déclare ainsi : `<databaseName>petclinic</databaseName>`

Exemple de fichier `pom.xml` modifié
```xml
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>${mysql.version}</version>
</dependency>
<!-- .... -->
<plugin>
	<groupId>fr.treeptik.micropaas.maven.plugins</groupId>
	<artifactId>micropaas-maven-plugin</artifactId>
	<version>0.0.1</version>
	<configuration>
		<urlDockerManager>http://192.168.0.109:4243</urlDockerManager>
		<containerImage>micropaas/tomcat-mysql</containerImage>
		<containerName>superProjetTest</containerName>
		<databaseName>petclinic</databaseName>
	</configuration>
</plugin>
```   
Une fois la nouvelle configuration faite on peut réinitialiser le container pour qu'il crée la base de données. 

```bash
mvn  micropaas:reInitContainer 
```
Ensuite, il faut modifier la configuration spring de l'application d'exemple pour qu'elle accède à la base Mysql. Pour cela il faut ouvrir le fichier  src/main/resources/spring/data-access.properties, commenter tout ce qui concerne la base HSQLDB et dé-commenter les informations de la base Mysql. 

Exemple de fichier `data-access.properties`
```properties
#-------------------------------------------------------------------------------
# MySQL Settings

jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/petclinic
jdbc.username=root
jdbc.password=root

# Properties that control the population of schema and data for a new data source
jdbc.initLocation=classpath:db/mysql/initDB.sql
jdbc.dataLocation=classpath:db/mysql/populateDB.sql

# Property that determines which Hibernate dialect to use
# (only applied with "applicationContext-hibernate.xml")
hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Property that determines which database to use with an AbstractJpaVendorAdapter
jpa.database=MYSQL
jpa.showSql=true
``` 

Il ne reste plus qu'à compiler l'application et la déployer à nouveau. 

```bash
mvn clean package -DskipTests micropaas:deploy
```

That's it!
