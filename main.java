import java.util.Scanner;


public class main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("");
        System.out.println("Welcome to Nzilla Stock Tracker!");
        
        try {
            Thread.sleep(2000); // 2 second delay before display
             System.out.println("1. Add Stock");
             System.out.println("2. View Portfolio");
             System.out.println("3. Exit");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       

        // // Here you would add logic to handle user input and navigate to the appropriate functionality
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                // Call AddStock functionality (runs the AddStock app flow)
                AddStock.main(new String[0]);
                break;
            case 2:
                // TODO: Call ViewPortfolio functionality
                System.out.println("View portfolio not implemented yet.");
                break;
            case 3:
                System.out.println("Exiting...");
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }

        scanner.close();
    }
}
