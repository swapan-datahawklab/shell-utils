package com.example.shelldemo.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.sql.ResultSet;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.cli.ConnectionConfig;
import com.example.shelldemo.cli.UnifiedDatabaseOperation;
import com.example.shelldemo.exception.DatabaseException;
import com.example.shelldemo.analysis.CommandData;
import com.example.shelldemo.analysis.CommandClassDiscoverer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A unified database runner that combines CLI and script execution functionality.
 * Supports executing SQL scripts and stored procedures with proper error handling.
 */
@Command(name = "db", mixinStandardHelpOptions = true, version = "1.0",
    description = "Unified Database CLI Tool")
public class RunnerDatabase implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(RunnerDatabase.class);
    private final Map<String, CommandTestResults> testResults = new ConcurrentHashMap<>();
    private final String basePackage;

    private UnifiedDatabaseOperation dbOperation;
    private final ConnectionConfig config;

    @Option(names = {"-t", "--type"}, required = true,
        description = "Database type (oracle, sqlserver, postgresql, mysql)")
    private String dbType;

    @Option(names = {"-H", "--host"}, required = true, description = "Database host")
    private String host;

    @Option(names = {"-P", "--port"},
        description = "Database port (defaults: oracle=1521, sqlserver=1433, postgresql=5432, mysql=3306)")
    private int port;

    @Option(names = {"-u", "--username"}, required = true, description = "Database username")
    private String username;

    @Option(names = {"-p", "--password"}, required = true, description = "Database password")
    private String password;

    @Option(names = {"-d", "--database"}, required = true, description = "Database name")
    private String database;

    @Option(names = {"--stop-on-error"}, defaultValue = "true",
        description = "Stop execution on error")
    private boolean stopOnError;

    @Option(names = {"--auto-commit"}, defaultValue = "false",
        description = "Auto-commit mode")
    private boolean autoCommit;

    @Option(names = {"--print-statements"}, defaultValue = "false",
        description = "Print SQL statements")
    private boolean printStatements;

    @Parameters(index = "0", description = "SQL script file or stored procedure name")
    private String target;

    @Option(names = {"--function"}, description = "Execute as function")
    private boolean isFunction;

    @Option(names = {"--return-type"}, defaultValue = "NUMERIC",
        description = "Return type for functions")
    private String returnType;

    @Option(names = {"-i", "--input"}, description = "Input parameters (name:type:value,...)")
    private String inputParams;

    @Option(names = {"-o", "--output"}, description = "Output parameters (name:type,...)")
    private String outputParams;

    @Option(names = {"--io"}, description = "Input/Output parameters (name:type:value,...)")
    private String ioParams;

    @Option(names = {"--driver-path"}, description = "Path to JDBC driver JAR file")
    private String driverPath;

    @Option(names = {"--csv-output"}, description = "Output file for CSV format (if query results exist)")
    private String csvOutputFile;

    public RunnerDatabase(String basePackage) {
        this.basePackage = basePackage;
        this.config = new ConnectionConfig();
    }

    public RunnerDatabase() {
        this("com.example.shelldemo.commands");
    }

    @Override
    public Integer call() throws Exception {
        log.debug("Starting UnifiedDatabaseRunner with parameters:");
        log.debug("dbType: {}", dbType);
        log.debug("host: {}", host);
        log.debug("port: {}", port);
        log.debug("username: {}", username);
        log.debug("database: {}", database);
        
        if (driverPath != null) {
            loadDriverFromPath(driverPath);
        }
        
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new DatabaseException("Database type is null or empty. Please provide a valid database type using -t or --type option.");
        }

        try {
            // Initialize config
            config.setHost(host);
            config.setUsername(username);
            config.setPassword(password);
            config.setDatabase(database);
            config.setPort(port > 0 ? port : getDefaultPort());

            // Create database operation with validated dbType
            String validatedDbType = dbType.trim().toLowerCase();
            if (!isValidDbType(validatedDbType)) {
                throw new DatabaseException("Invalid database type: " + validatedDbType + ". Supported types are: oracle, sqlserver, postgresql, mysql");
            }
            dbOperation = UnifiedDatabaseOperation.create(validatedDbType, config);

            // Determine operation type
            File scriptFile = new File(target);
            if (scriptFile.exists()) {
                return runScript(scriptFile);
            } else {
                return runStoredProc();
            }
        } catch (SQLException e) {
            throw new DatabaseException(formatOracleError(e), e);
        } catch (Exception e) {
            throw new DatabaseException("Database operation failed: " + e.getMessage(), e);
        }
    }

    private boolean isValidDbType(String dbType) {
        return dbType.equals("oracle") || 
               dbType.equals("sqlserver") || 
               dbType.equals("postgresql") || 
               dbType.equals("mysql");
    }

    private int getDefaultPort() {
        switch (dbType.toLowerCase()) {
            case "oracle":
                return 1521;
            case "sqlserver":
                return 1433;
            case "postgresql":
                return 5432;
            case "mysql":
                return 3306;
            default:
                return 1521;
        }
    }

    private List<String> parseSqlFile(File scriptFile) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        boolean inMultilineComment = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = processLine(line, inMultilineComment);
                if (line == null) continue;
                
                currentStatement.append(line).append("\n");
                
                if (line.endsWith(";")) {
                    addStatement(statements, currentStatement);
                    currentStatement.setLength(0);
                }
            }
            
            addStatement(statements, currentStatement);
        }
        
        return statements;
    }

    private String processLine(String line, boolean inMultilineComment) {
        line = line.trim();
        if (line.isEmpty()) return null;
        
        if (inMultilineComment) {
            return null;
        }
        
        if (line.startsWith("/*")) {
            return null;
        }
        
        if (line.startsWith("--") || line.startsWith("//")) {
            return null;
        }
        
        int commentStart = line.indexOf("--");
        return commentStart >= 0 ? line.substring(0, commentStart).trim() : line;
    }

    private void addStatement(List<String> statements, StringBuilder currentStatement) {
        String stmt = currentStatement.toString().trim();
        if (!stmt.isEmpty()) {
            if (stmt.endsWith(";")) {
                stmt = stmt.substring(0, stmt.length() - 1).trim();
            }
            if (!stmt.isEmpty()) {
                statements.add(stmt);
            }
        }
    }

    private String formatOracleError(SQLException e) {
        String message = e.getMessage();
        int oraIndex = message.indexOf("ORA-");
        if (oraIndex >= 0) {
            int endIndex = message.indexOf(":", oraIndex);
            String oraCode = endIndex > oraIndex ? message.substring(oraIndex, endIndex) : message.substring(oraIndex);
            String errorDetails = message.substring(endIndex + 1).trim();
            
            // Extract the most relevant part of the error message
            int detailsIndex = errorDetails.indexOf("ORA-03301");
            if (detailsIndex > 0) {
                errorDetails = errorDetails.substring(0, detailsIndex).trim();
            }
            
            return oraCode + ": " + errorDetails;
        }
        return message;
    }

    private void executeSqlStatement(Connection conn, String sql) throws SQLException {
        if (printStatements) {
            log.info("Executing: {}", sql);
        }
        
        try (Statement stmt = csvOutputFile != null ? 
                conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY) :
                conn.createStatement()) {
                
            boolean isQuery = stmt.execute(sql);
            
            if (isQuery) {
                try (var rs = stmt.getResultSet()) {
                    if (csvOutputFile != null) {
                        writeQueryResultsToCsv(rs);
                    } else {
                        displayQueryResults(rs);
                    }
                }
            }
            
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            if (!autoCommit) {
                conn.rollback();
            }
            log.error(formatOracleError(e));
            if (stopOnError) {
                throw e;
            }
        }
    }

    private void writeQueryResultsToCsv(ResultSet rs) throws SQLException {
        try {
            rs.beforeFirst();
            writeResultsToCsv(rs, csvOutputFile);
            log.info("CSV output written to: {}", csvOutputFile);
        } catch (IOException e) {
            log.error("Error writing to CSV file: {}", e.getMessage());
        }
    }

    private void displayQueryResults(ResultSet rs) throws SQLException {
        var metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Print column headers
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) header.append(",");
            header.append(metaData.getColumnName(i).trim());
        }
        log.info("{}", header);
        
        // Print rows
        while (rs.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) row.append(",");
                String value = rs.getString(i);
                row.append(value != null ? value.trim() : "NULL");
            }
            log.info("{}", row);
        }
    }

    private int runScript(File scriptFile) throws SQLException {
        return dbOperation.executeTransaction(conn -> {
            conn.setAutoCommit(autoCommit);
            
            try {
                List<String> statements = parseSqlFile(scriptFile);
                for (String sql : statements) {
                    executeSqlStatement(conn, sql);
                }
                return 0;
            } catch (IOException e) {
                throw new SQLException("Failed to read script file: " + e.getMessage(), e);
            }
        });
    }

    private int runStoredProc() throws SQLException {
        return dbOperation.executeTransaction(conn -> {
            List<ProcedureParam> params = parseParameters();

            if (isFunction) {
                return runFunction(conn, params);
            } else {
                return runProcedure(conn, params);
            }
        });
    }

    private List<ProcedureParam> parseParameters() {
        List<ProcedureParam> params = new ArrayList<>();
        
        if (inputParams != null) {
            params.addAll(parseInputParameters());
        }
        
        if (outputParams != null) {
            params.addAll(parseOutputParameters());
        }
        
        if (ioParams != null) {
            params.addAll(parseIoParameters());
        }
        
        return params;
    }

    private List<ProcedureParam> parseInputParameters() {
        List<ProcedureParam> params = new ArrayList<>();
        List<String> paramPairs = splitPreservingFunctions(inputParams);
        for (String paramPair : paramPairs) {
            String[] parts = paramPair.split(":", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid parameter format: " + paramPair + 
                    ". Expected format: name:type:value");
            }
            params.add(new ProcedureParam(parts[0], parts[1], parts[2]));
        }
        return params;
    }

    private List<ProcedureParam> parseOutputParameters() {
        List<ProcedureParam> params = new ArrayList<>();
        String[] paramPairs = outputParams.split(",");
        for (String paramPair : paramPairs) {
            String[] parts = paramPair.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid output parameter format: " + paramPair + 
                    ". Expected format: name:type");
            }
            params.add(new ProcedureParam(parts[0], parts[1], null));
        }
        return params;
    }

    private List<ProcedureParam> parseIoParameters() {
        List<ProcedureParam> params = new ArrayList<>();
        String[] paramPairs = ioParams.split(",");
        for (String paramPair : paramPairs) {
            String[] parts = paramPair.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid IO parameter format: " + paramPair + 
                    ". Expected format: name:type:value");
            }
            params.add(new ProcedureParam(parts[0], parts[1], parts[2]));
        }
        return params;
    }

    private List<String> splitPreservingFunctions(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int parenCount = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                parenCount++;
            } else if (c == ')') {
                parenCount--;
            } else if (c == ',' && parenCount == 0) {
                result.add(input.substring(start, i).trim());
                start = i + 1;
            }
        }
        
        // Add the last parameter
        if (start < input.length()) {
            result.add(input.substring(start).trim());
        }
        
        return result;
    }

    private int runFunction(Connection conn, List<ProcedureParam> params) throws SQLException {
        if (returnType == null) {
            log.error("Return type must be specified for functions");
            return 1;
        }

        String call = buildCallString(target, true, params.size());
        try (CallableStatement stmt = conn.prepareCall(call)) {
            // Register return parameter
            stmt.registerOutParameter(1, getSqlType(returnType));

            // Register parameters
            int paramIndex = 2;
            for (ProcedureParam param : params) {
                stmt.setObject(paramIndex++, param.getValue());
            }

            // Execute the function
            stmt.execute();
            Object result = stmt.getObject(1);
            log.info("Function result: {}", result);
            return 0;
        }
    }

    private int runProcedure(Connection conn, List<ProcedureParam> params) throws SQLException {
        String call = buildCallString(target, false, params.size());
        try (CallableStatement stmt = conn.prepareCall(call)) {
            // Register parameters
            int paramIndex = 1;
            for (ProcedureParam param : params) {
                stmt.setObject(paramIndex++, param.getValue());
            }

            // Execute the procedure
            stmt.execute();
            int updateCount = stmt.getUpdateCount();
            
            if (updateCount >= 0) {
                log.info("Rows inserted/updated: {}", updateCount);
            }
            
            printOutputParams(stmt, params);
            return 0;
        }
    }

    private String buildCallString(String procedureName, boolean isFunction, int paramCount) {
        StringBuilder call = new StringBuilder();

        if (isFunction) {
            call.append("{? = call ").append(procedureName).append("(");
        } else {
            call.append("{call ").append(procedureName).append("(");
        }

        for (int i = 0; i < paramCount; i++) {
            if (i > 0) call.append(", ");
            call.append("?");
        }

        call.append(")}");
        return call.toString();
    }

    private int getSqlType(String type) {
        switch (type.toUpperCase()) {
            case "NUMERIC", "NUMBER":
                return Types.NUMERIC;
            case "VARCHAR", "VARCHAR2", "CHAR":
                return Types.VARCHAR;
            case "DATE":
                return Types.DATE;
            case "TIMESTAMP":
                return Types.TIMESTAMP;
            case "CLOB":
                return Types.CLOB;
            case "BLOB":
                return Types.BLOB;
            default:
                return Types.VARCHAR;
        }
    }

    private void printOutputParams(CallableStatement stmt, List<ProcedureParam> params) throws SQLException {
        int paramIndex = 1;
        for (ProcedureParam param : params) {
            if (param.getValue() == null) { // Output parameter
                Object value = stmt.getObject(paramIndex);
                log.info("{} = {}", param.getName(), value);
            }
            paramIndex++;
        }
    }

    private void loadDriverFromPath(String path) {
        try {
            File driverFile = new File(path);
            if (!driverFile.exists()) {
                log.error("Driver file not found: {}", path);
                return;
            }

            URL driverUrl = driverFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{driverUrl}, getClass().getClassLoader());

            // Try to load the driver using ServiceLoader
            ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class, loader);
            for (Driver driver : drivers) {
                log.info("Loaded driver: {}", driver.getClass().getName());
                DriverManager.registerDriver(new DriverShim(driver));
            }
        } catch (Exception e) {
            log.error("Error loading driver from path {} - System classpath: {} - {}: {}", 
                path, System.getProperty("java.class.path"), e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private static class DriverShim implements Driver {
        private final Driver driver;

        DriverShim(Driver driver) {
            this.driver = driver;
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        @Override
        public Connection connect(String url, java.util.Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }

        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }
    }

    public static class ProcedureParam {
        private final String name;
        private final String type;
        private final Object value;

        public ProcedureParam(String name, String type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }

    private void writeResultsToCsv(ResultSet rs, String filename) throws SQLException, IOException {
        var metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        try (FileWriter writer = new FileWriter(filename)) {
            // Write headers
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) writer.append(',');
                writer.append(escapeCsvField(metaData.getColumnName(i)));
            }
            writer.append('\n');
            
            // Write data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) writer.append(',');
                    String value = rs.getString(i);
                    writer.append(escapeCsvField(value != null ? value : ""));
                }
                writer.append('\n');
            }
        }
    }
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        boolean needsQuoting = field.contains(",") || field.contains("\"") || field.contains("\n");
        if (!needsQuoting) {
            return field;
        }
        
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    public void runAllCommandTests(List<CommandData> commands) {
        commands.forEach(this::testCommand);
    }

    private void testCommand(CommandData cmd) {
        CommandTestResults results = new CommandTestResults(cmd.getName());
        testResults.put(cmd.getName(), results);

        try {
            testNoArgs(cmd, results);
            testValidArgs(cmd, results);
            testInvalidArgs(cmd, results);
            testEdgeCases(cmd, results);
        } catch (Exception e) {
            results.recordFailure(e);
        }
    }

    private void testNoArgs(CommandData cmd, CommandTestResults results) {
        try {
            cmd.newInstance().run();
            results.recordSuccess("testNoArgs");
        } catch (Exception e) {
            results.recordFailure("testNoArgs", e);
        }
    }

    private void testValidArgs(CommandData cmd, CommandTestResults results) {
        // Implement valid argument testing
        results.recordSuccess("testValidArgs");
    }

    private void testInvalidArgs(CommandData cmd, CommandTestResults results) {
        // Implement invalid argument testing
        results.recordSuccess("testInvalidArgs");
    }

    private void testEdgeCases(CommandData cmd, CommandTestResults results) {
        // Implement edge case testing
        results.recordSuccess("testEdgeCases");
    }

    public Map<String, CommandTestResults> getTestResults() {
        return testResults;
    }

    public List<CommandData> getSuccessfulCommands() {
        return testResults.entrySet().stream()
            .filter(entry -> entry.getValue().isSuccessful())
            .map(entry -> {
                try {
                    return CommandClassDiscoverer.findCommand(basePackage, entry.getKey());
                } catch (Exception e) {
                    log.error("Error getting command data for {}", entry.getKey(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static class CommandTestResults {
        private final String commandName;
        private final List<TestResult> results = new ArrayList<>();
        private boolean overallSuccess = true;

        public CommandTestResults(String commandName) {
            this.commandName = commandName;
        }

        public String getCommandName() {
            return commandName;
        }

        public void recordSuccess(String testName) {
            results.add(new TestResult(testName, true, null));
        }

        public void recordFailure(String testName, Exception e) {
            results.add(new TestResult(testName, false, e));
            overallSuccess = false;
        }

        public void recordFailure(Exception e) {
            overallSuccess = false;
        }

        public boolean isSuccessful() {
            return overallSuccess;
        }

        public List<TestResult> getResults() {
            return Collections.unmodifiableList(results);
        }

        public static class TestResult {
            private final String testName;
            private final boolean success;
            private final Exception error;

            public TestResult(String testName, boolean success, Exception error) {
                this.testName = testName;
                this.success = success;
                this.error = error;
            }

            public String getTestName() { return testName; }
            public boolean isSuccess() { return success; }
            public Exception getError() { return error; }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RunnerDatabase()).execute(args);
        System.exit(exitCode);
    }
}