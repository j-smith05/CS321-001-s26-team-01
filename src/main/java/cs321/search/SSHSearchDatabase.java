/**
 * @author @tk-44
 * @date 04/08/2026
 * The driver class for searching a Database of a B-Tree.
 */

package cs321.search;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;



public class SSHSearchDatabase
{
	
    public static void main(String[] args) throws Exception
    {
        String treeType = null;
        String dbPath = null;
        int topFrequency = 0;

        for (String arg : args)
        {
            if (arg.startsWith("--type=")) {
                treeType = arg.substring(7);
            } else if (arg.startsWith("--database=")) {
                dbPath = arg.substring(11);
            } else if (arg.startsWith("--top-frequency=")) {
                try {
                    topFrequency = Integer.parseInt(arg.substring(16));
                } catch (NumberFormatException e) {
                    System.err.println("Error: --top-frequency must be an integer.");
                    return;
                }
            }
        }
        // Validate arguments
        if (treeType == null || dbPath == null) {
            System.err.println("Usage: java -jar SSHSearchDatabase.jar --type=<tree-type> --database=<path> --top-frequency=<N>");
            return;
        }
        if (!treeType.equalsIgnoreCase("test") && topFrequency <= 0) {
            System.err.println("Error: --top-frequency must be greater than 0 when searching.");
            return;
        }

        //Route to the appropriate mode
        if (treeType.equalsIgnoreCase("test")) {
            System.out.println("Test mode detected. Generating test database...");
            createTestDatabase(dbPath);
        } else {
            // Format the table name (remove hyphens to prevent SQL syntax errors)
            String tableName = treeType.replace("-", "");
            searchAndPrintTopFrequencies(dbPath, tableName, topFrequency);
        }
    }

    /**
     * TEST MODE: Creates the acceptedip table and inserts the 25 test entries.
     */
    private static void createTestDatabase(String dbPath) {
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
    
        Object[][] testData = {
            {"Accepted-111.222.107.90", 25}, {"Accepted-112.96.173.55", 3},
            {"Accepted-112.96.33.40", 3},    {"Accepted-113.116.236.34", 6},
            {"Accepted-113.118.187.34", 2},  {"Accepted-113.99.127.215", 2},
            {"Accepted-119.137.60.156", 1},  {"Accepted-119.137.62.123", 9},
            {"Accepted-119.137.62.142", 1},  {"Accepted-119.137.63.195", 14},
            {"Accepted-123.255.103.142", 5}, {"Accepted-123.255.103.215", 5},
            {"Accepted-137.189.204.138", 1}, {"Accepted-137.189.204.155", 1},
            {"Accepted-137.189.204.220", 1}, {"Accepted-137.189.204.236", 1},
            {"Accepted-137.189.204.246", 1}, {"Accepted-137.189.204.253", 3},
            {"Accepted-137.189.205.44", 2},  {"Accepted-137.189.206.152", 1},
            {"Accepted-137.189.206.243", 1}, {"Accepted-137.189.207.18", 1},
            {"Accepted-137.189.207.28", 1},  {"Accepted-137.189.240.159", 1},
            {"Accepted-137.189.241.19", 2}
        };

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DROP TABLE IF EXISTS acceptedip");
            
            statement.executeUpdate("CREATE TABLE acceptedip (key_value TEXT, frequency INTEGER)");
            
            // Insert the test data using a PreparedStatement for efficiency and security
            String insertSql = "INSERT INTO acceptedip (key_value, frequency) VALUES (?, ?)";
            try (PreparedStatement test = connection.prepareStatement(insertSql)) {
                
                // Add all entries to a batch to execute them efficiently
                for (Object[] row : testData) {
                    test.setString(1, (String) row[0]);
                    test.setInt(2, (Integer) row[1]);
                    test.addBatch(); 
                }
                
                // Execute the batch insertion
                test.executeBatch();
                System.out.println("Successfully created table acceptedip and inserted 25 test records.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to create test database: " + e.getMessage());
        }
    }

    /**
     * SEARCH MODE: Queries the specified table for the top frequencies.
     */
    private static void searchAndPrintTopFrequencies(String dbPath, String tableName, int limit) {
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        String query = "SELECT key_value, frequency FROM " + tableName + 
                       " ORDER BY frequency DESC LIMIT ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement search = connection.prepareStatement(query)) {
             
            search.setInt(1, limit);

            try (ResultSet rs = search.executeQuery()) {
                System.out.println("Top " + limit + " frequencies for " + tableName + ":");
                System.out.println("----------------------------------------");
                
                int count = 1;
                while (rs.next()) {
                    String key = rs.getString("key_value");
                    int frequency = rs.getInt("frequency");
                    System.out.printf("%d. Key: %s | Frequency: %d%n", count, key, frequency);
                    count++;
                }
                
                if (count == 1) {
                    System.out.println("No records found in table '" + tableName + "'.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.err.println("Make sure the table '" + tableName + "' exists and the database path is correct.");
        }
    }
}