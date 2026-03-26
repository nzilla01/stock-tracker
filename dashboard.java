import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class dashboard {

    // =========================
    // PORTFOLIO ENTRY CLASS
    // =========================
    static class PortfolioEntry {
        String asset;
        double currentPrice;
        int sharesBought;
        double purchasePrice;
        double totalInvested;
        double currentValue;
        double gainLoss;
        double gainLossPercent;

        public PortfolioEntry(String asset, double currentPrice, int sharesBought, double purchasePrice) {
            this.asset = asset;
            this.currentPrice = currentPrice;
            this.sharesBought = sharesBought;
            this.purchasePrice = purchasePrice;
            this.totalInvested = sharesBought * purchasePrice;
            this.currentValue = sharesBought * currentPrice;
            this.gainLoss = currentValue - totalInvested;
            this.gainLossPercent = (gainLoss / totalInvested) * 100;
        }
    }

    // =========================
    // FETCH CURRENT PRICE
    // =========================
    public static double fetchCurrentPrice(String symbol) {
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

            // extract price from JSON
            if (data.contains("\"05. price\":")) {
                String price = data.split("\"05. price\": \"")[1].split("\"")[0];
                return Double.parseDouble(price);
            } else {
                return 0.0;
            }

        } catch (Exception e) {
            System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
            return 0.0;
        }
    }

    // =========================
    // DISPLAY DASHBOARD
    // =========================
    public static void displayDashboard(List<AddStock.StockHolding> portfolio) {
        if (portfolio.isEmpty()) {
            System.out.println("\n Your portfolio is empty. Add stocks to get started!");
            return;
        }

        List<PortfolioEntry> entries = new ArrayList<>();
        double totalInvested = 0;
        double totalCurrentValue = 0;
        double totalGainLoss = 0;

        System.out.println("\n⏳ Loading current prices...");

        // Fetch current prices and calculate metrics
        for (AddStock.StockHolding holding : portfolio) {
            double currentPrice = fetchCurrentPrice(holding.symbol);
            PortfolioEntry entry = new PortfolioEntry(
                holding.symbol,
                currentPrice,
                holding.shares,
                holding.purchasePrice
            );
            entries.add(entry);

            totalInvested += entry.totalInvested;
            totalCurrentValue += entry.currentValue;
            totalGainLoss += entry.gainLoss;
        }

        // Display table header
        System.out.println("\n" + "=".repeat(95));
        System.out.println(" STOCK PORTFOLIO DASHBOARD");
        System.out.println("=".repeat(95));

        System.out.printf(
            "%-10s | %-15s | %-15s | %-12s | %-15s | %-12s%n",
            "Asset",
            "Current Price",
            "Shares Bought",
            "Total Invested",
            "Current Value",
            "P&L (Outcome)"
        );
        System.out.println("-".repeat(95));

        // Display each stock
        for (PortfolioEntry entry : entries) {
            String pnlIndicator = entry.gainLoss >= 0 ? "📈 +" : "📉 ";
            String pnlColor = entry.gainLoss >= 0 ? "[GAIN]" : "[LOSS]";

            System.out.printf(
                "%-10s | $%-14.2f | %-15d | $%-14.2f | $%-14.2f | %s$%.2f (%+.2f%%) %s%n",
                entry.asset,
                entry.currentPrice,
                entry.sharesBought,
                entry.totalInvested,
                entry.currentValue,
                pnlIndicator,
                entry.gainLoss,
                entry.gainLossPercent,
                pnlColor
            );
        }

        System.out.println("-".repeat(95));

        // Display portfolio summary
        double totalGainLossPercent = (totalGainLoss / totalInvested) * 100;
        String summaryIndicator = totalGainLoss >= 0 ? "📈 +" : "📉 ";
        String summaryColor = totalGainLoss >= 0 ? "[GAIN]" : "[LOSS]";

        System.out.printf(
            "%-10s | %-15s | %-15s | $%-14.2f | $%-14.2f | %s$%.2f (%+.2f%%) %s%n",
            "TOTAL",
            "",
            "",
            totalInvested,
            totalCurrentValue,
            summaryIndicator,
            totalGainLoss,
            totalGainLossPercent,
            summaryColor
        );

        System.out.println("=".repeat(95));

        // Display portfolio insights
        System.out.println("\n PORTFOLIO INSIGHTS:");
        System.out.println("   • Total Invested: $" + String.format("%.2f", totalInvested));
        System.out.println("   • Current Portfolio Value: $" + String.format("%.2f", totalCurrentValue));
        System.out.println("   • Total Gain/Loss: $" + String.format("%.2f", totalGainLoss) + " (" + String.format("%+.2f", totalGainLossPercent) + "%)");
        System.out.println("   • Number of Holdings: " + portfolio.size());

        if (totalGainLoss >= 0) {
            System.out.println("\n Your portfolio is in PROFIT!");
        } else {
            System.out.println("\n  Your portfolio is currently in LOSS. Keep investing wisely!");
        }
    }

    // =========================
    // MAIN PROGRAM
    // =========================
    public static void main(String[] args) {
        System.out.println("Welcome to your Portfolio Dashboard!");
        System.out.println("=====================================\n");

        // Get portfolio from AddStock
        List<AddStock.StockHolding> portfolio = AddStock.getPortfolio();

        // Display the dashboard
        displayDashboard(portfolio);
    }
}