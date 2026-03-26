import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class AddStock {

    // =========================
    // STOCK HOLDING CLASS
    // =========================
    static class StockHolding {
        String symbol;
        int shares;
        double purchasePrice;

        public StockHolding(String symbol, int shares, double purchasePrice) {
            this.symbol = symbol;
            this.shares = shares;
            this.purchasePrice = purchasePrice;
        }
    }

    private static List<StockHolding> portfolio = new ArrayList<>();

    // =========================
    // GET PORTFOLIO
    // =========================
    public static List<StockHolding> getPortfolio() {
        return portfolio;
    }

    // =========================
    // FETCH SYMBOL LIST (CSV)
    // =========================
    public static String fetchSymbolsData() {
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
            System.out.println("Error fetching symbols: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // PARSE SYMBOLS FROM CSV
    // =========================
    public static List<String> parseSymbols(String csv) {
        List<String> symbols = new ArrayList<>();

        String[] lines = csv.split("\n");

        for (int i = 1; i < lines.length; i++) { // skip header
            String[] parts = lines[i].split(",");

            if (parts.length > 1) {
                String symbol = parts[0].trim();
                String name = parts[1].trim();

                symbols.add(symbol + " - " + name);
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
    // FETCH PRICE (JSON)
    // =========================
    public static String fetchPrice(String symbol) {
        try {
            String apiKey = config.getApiKey();

            URL url = new URL(
                "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                        + symbol + "&apikey=" + apiKey
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            String data = response.toString();

            // extract price from JSON (simple method)
            if (data.contains("\"05. price\":")) {
                return data.split("\"05. price\": \"")[1].split("\"")[0];
            } else {
                return "N/A";
            }

        } catch (Exception e) {
            return "Error";
        }
    }

    // =========================
    // ADD TO PORTFOLIO
    // =========================
    public static void addToPortfolio(String symbol, int shares, double purchasePrice) {
        portfolio.add(new StockHolding(symbol, shares, purchasePrice));

        System.out.println("\n Bought " + shares + " shares of " + symbol + " at $" + purchasePrice);
    }

    // =========================
    // MAIN PROGRAM
    // =========================
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Fetching stock symbols...");
        String data = fetchSymbolsData();

        if (data == null) {
            System.out.println("Failed to load stock data.");
            return;
        }

        List<String> symbols = parseSymbols(data);

        while (true) {

            System.out.print("\nSearch stock (or type 'exit'): ");
            String query = scanner.nextLine();

            if (query.equalsIgnoreCase("exit")) break;

            List<String> results = searchSymbols(symbols, query);

            if (results.isEmpty()) {
                System.out.println("No matches found.");
                continue;
            }

            // show results
            System.out.println("\nMatches:");
            int limit = Math.min(results.size(), 10);

            for (int i = 0; i < limit; i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }

            // select
            System.out.print("\nSelect number: ");
            String input = scanner.nextLine();

            try {
                int index = input.isBlank() ? 0 : Integer.parseInt(input) - 1;

                if (index < 0 || index >= results.size()) {
                    System.out.println("Invalid selection.");
                    continue;
                }

                String selected = results.get(index);

                // extract symbol only
                String symbolOnly = selected.split(" - ")[0];

                // fetch price
                String price = fetchPrice(symbolOnly);

                System.out.println("Current Price of " + symbolOnly + ": $" + price);

                // shares
                System.out.print("Enter number of shares: ");
                int shares = Integer.parseInt(scanner.nextLine());

                if (shares <= 0) {
                    System.out.println("Invalid shares.");
                    continue;
                }

                double purchasePrice = Double.parseDouble(price);
                addToPortfolio(symbolOnly, shares, purchasePrice);

            } catch (Exception e) {
                System.out.println("Invalid input.");
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
