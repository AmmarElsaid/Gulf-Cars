## 1. Set up the database

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed.sql   # optional sample data
```

This creates a database named **`gulfcars`** with all tables and constraints from spec.

## 2. Configure the connection

Edit `src/main/resources/db.properties`:

```
db.url=jdbc:mysql://localhost:3306/gulfcars?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=YOUR_PASSWORD
```

## 3. Build & run

### Option A — Maven (recommended)

```bash
mvn clean package
java -jar target/gulfcars-1.0.0-jar-with-dependencies.jar
```

### Option B — Plain javac

Download `mysql-connector-j-8.4.0.jar` into `lib/`, then:

```bash
# compile
javac -d out -cp "lib/*" $(find src/main/java -name "*.java")
# copy resources
mkdir -p out && cp src/main/resources/db.properties out/
# run
java -cp "out:lib/*" com.gulfcars.app.Main      # macOS / Linux
java -cp "out;lib/*" com.gulfcars.app.Main      # Windows
``
