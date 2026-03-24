import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AddStock {

    // Stores stock + shares together
    static class StockHolding {
        String symbol;
        int shares;

        public StockHolding(String symbol, int shares) {
            this.symbol = symbol;
            this.shares = shares;
        }
    }

    private static List<StockHolding> portfolio = new ArrayList<>();

    // =========================
    // FETCH DATA FROM API
    // =========================
    public static String fetchData() {
        try {
            String apiKey = config.getApiKey();

            URL url = new URL(
                "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=" + apiKey
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            in.close();
            return response.toString();

        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // PARSE SYMBOLS
    // =========================
    public static List<String> parseSymbols(String csv) {
        List<String> symbols = new ArrayList<>();
        String[] lines = csv.split("\n");

        for (int i = 1; i < lines.length; i++) { // skip header
            String[] parts = lines[i].split(",");

            if (parts.length > 0) {
                symbols.add(parts[0].trim());
            }
        }

        return symbols;
    }

    // =========================
    // SEARCH SYMBOLS
    // =========================
    public static List<String> searchSymbols(List<String> symbols, String query) {
        List<String> results = new ArrayList<>();

        for (String symbol : symbols) {
            if (symbol.toUpperCase().contains(query.toUpperCase())) {
                results.add(symbol);
            }
        }

        return results;
    }

    // =========================
    // ADD TO PORTFOLIO (WITH SHARES)
    // =========================
    public static void addToPortfolio(String symbol, int shares) {
        portfolio.add(new StockHolding(symbol, shares));

        System.out.println("\n🎉 Congratulations!");
        System.out.println("You just bought " + shares + " shares of " + symbol);
    }

    // =========================
    // MAIN PROGRAM
    // =========================
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Fetching stock data...");
        String data = fetchData();

        if (data == null) {
            System.out.println("Failed to load stock data.");
            return;
        }

        List<String> symbols = parseSymbols(data);

        while (true) {

            System.out.print("\nSearch stock symbol (or type 'exit'): ");
            String query = scanner.nextLine();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            List<String> results = searchSymbols(symbols, query);

            if (results.isEmpty()) {
                System.out.println("No matching symbols found.");
                continue;
            }

            // Show results (limit 10)
            System.out.println("\nMatches:");
            int limit = Math.min(results.size(), 10);

            for (int i = 0; i < limit; i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }

            // Select stock
            System.out.print("\nSelect number (Enter = first result): ");
            String input = scanner.nextLine();

            String selectedSymbol;

            try {
                if (input.isBlank()) {
                    selectedSymbol = results.get(0);
                } else {
                    int index = Integer.parseInt(input) - 1;

                    if (index < 0 || index >= results.size()) {
                        System.out.println("Invalid selection.");
                        continue;
                    }

                    selectedSymbol = results.get(index);
                }

                // Get shares
                System.out.print("Enter number of shares to buy: ");
                int shares = Integer.parseInt(scanner.nextLine());

                if (shares <= 0) {
                    System.out.println("Shares must be greater than 0.");
                    continue;
                }

                addToPortfolio(selectedSymbol, shares);

            } catch (Exception e) {
                System.out.println("Invalid input. Try again.");
            }
        }

        // =========================
        // SHOW PORTFOLIO
        // =========================
        System.out.println("\n===== YOUR PORTFOLIO =====");

        for (StockHolding s : portfolio) {
            System.out.println(s.symbol + " - " + s.shares + " shares");
        }

        scanner.close();
    }
}