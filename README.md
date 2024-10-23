# DatabaseEngineORM
Simple MySQL ORM for Java, Easy to use, suggestion are welcome. (May not perfect for production)

# How to use
1. create package called 'database' or 'databaseengine' inside of your project.
2. copy and paste all files in 'src' of this repo.
3. add this to 'dependencies' of pom.xml of your project.
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```
5. done! you can now use it. please do not forget to change package name of each java files.

# Docs
## Connecting & check connection
```java
DatabaseEngine engine = new DatabaseEngine("localhost:3306",
                                           "username", "p@ssword",
                                           "my_database");

if ( engine.isConnected() ) {
    System.out.println("Connected");
} else {
    System.out.println("Not Connected");
}
```
## Create table
```java
engine.createTable("users")
    .addColumn("username","TEXT")
    .addColumn("password","TEXT")
    .addColumn("email", "TEXT", "bob@email.com");
```
## Add Column
```java
engine.getTable("users")
    .addColumn("phone","TEXT");
```
## Create record 
```java
Record record = engine.getTable("users").create()
    .set("username","I'm Bob")
    .set("password","passwd")
    .set("phone","phonenumber")
    .save();
```
## Find Record and Update it
```java
Record record = engine.getTable("users").find()
    .ifCondition("username","I'm Bob").first();
if ( record == null ) return;
record.set("username", "I'm not Bob"); 
record.save(); 
```
## Shutdown the connection
```java
engine.shutdown();
```
