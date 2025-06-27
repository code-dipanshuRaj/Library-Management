package library_management.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in)
        ) {
            System.out.println("📚 Welcome to the Library Management System");

            while (true) {
                System.out.println("\nSelect your role:");
                System.out.println("1. Member");
                System.out.println("2. Vendor");
                System.out.println("3. Admin");
                System.out.println("4. Author");
                System.out.println("5. Publisher");
                System.out.println("6. Search");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");
                int choice = sc.nextInt();
                sc.nextLine();

                if (choice == 7) {
                    out.println("EXIT");
                    System.out.println("Goodbye!");
                    break;
                }

                switch (choice) {
                    case 1 -> memberMenu(sc, out, in);
                    case 2 -> vendorMenu(sc, out, in);
                    case 3 -> adminMenu(sc, out, in);
                    case 4 -> authorMenu(sc, out, in);
                    case 5 -> publisherMenu(sc, out, in);
                    case 6 -> searchMenu(sc, out, in);
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static void memberMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        while (true) {
            System.out.println("\n--- Member Menu ---");
            System.out.println("1. View Book Status");
            System.out.println("2. Request Book");
            System.out.println("3. Return Book");
            System.out.println("4. View All Books");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Book ID: ");
                    int bookId = sc.nextInt();
                    sc.nextLine();
                    out.println("MEMBER_GET_BOOK " + bookId);
                    System.out.println("Server: " + in.readLine());
                }
                case 2 -> {
                    System.out.print("Enter Book ID to request: ");
                    int reqBookId = sc.nextInt();
                    System.out.print("Enter Member ID: ");
                    int memId = sc.nextInt();
                    System.out.print("Enter Employee ID handling the request: ");
                    int empId = sc.nextInt();
                    sc.nextLine(); // consume newline
                    out.println("MEMBER_REQUEST_BOOK " + reqBookId + " " + memId + " " + empId);
                    System.out.println("Server: " + in.readLine());
                }
                
                case 3 -> {
                    System.out.print("Enter Book ID to return: ");
                    int retBookId = sc.nextInt();
                    sc.nextLine();
                    out.println("MEMBER_RETURN_BOOK " + retBookId);
                    System.out.println("Server: " + in.readLine());
                }
                case 4 -> {
                    out.println("MEMBER_VIEW_BOOKS");
                    System.out.println("--- List of All Books ---");
                    String line;
                    while (!(line = in.readLine()).equals("END")) {
                        System.out.println("Server: " + line);
                    }
                }
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void vendorMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        while (true) {
            System.out.println("\n--- Vendor Menu ---");
            System.out.println("1. View Books");
            System.out.println("2. Add Sold Book Info");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> {
                    out.println("VENDOR_VIEW_BOOKS");
                    System.out.println("--- List of Books ---");
                    String response;
                    while (!(response = in.readLine()).equals("END")) {
                        System.out.println("Server: " + response);
                    }
                }
                case 2 -> {
                    System.out.print("Enter Book ID: ");
                    int bookId = sc.nextInt();
                    System.out.print("Enter Vendor ID: ");
                    int vendorId = sc.nextInt();
                    sc.nextLine();
                    out.println("VENDOR_ADD_SALE " + bookId + " " + vendorId);
                    System.out.println("Server: " + in.readLine());
                }
                case 3 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void adminMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add New Book");
            System.out.println("2. Delete Book");
            System.out.println("3. View Members");
            System.out.println("4. Hire Employee");
            System.out.println("5. Add Member");
            System.out.println("6. view all Books");
            System.out.println("7. Back");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Book ID, Price, Status (e.g., 101 299.99 Available): ");
                    String[] parts = sc.nextLine().split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("Invalid input. Please enter Book ID, Price, and Status.");
                        break;
                    }
                    int bookId = Integer.parseInt(parts[0]);
                    float price = Float.parseFloat(parts[1]);
                    String status = parts[2];
                    out.println("ADMIN_ADD_BOOK " + bookId + " " + price + " " + status);
                    System.out.println("Server: " + in.readLine());
                }
                case 2 -> {
                    System.out.print("Enter Book ID to delete: ");
                    int delId = sc.nextInt();
                    sc.nextLine();
                    out.println("ADMIN_DELETE_BOOK " + delId);
                    System.out.println("Server: " + in.readLine());
                }
                case 3 -> {
                    out.println("ADMIN_VIEW_MEMBERS");
                    System.out.println("--- List of Members ---");
                    String response;
                    while (!(response = in.readLine()).equals("END")) {
                        System.out.println("Server: " + response);
                    }
                }
                case 4 -> {
                    System.out.print("Enter Admin ID, Employee ID, and Employee Name: ");
                    String fullInput = sc.nextLine();
                    String[] partss = fullInput.split(" ", 3);
                    if (partss.length < 3) {
                        System.out.println("Invalid input. Please enter all values.");
                        break;
                    }
                    int adminId = Integer.parseInt(partss[0]);
                    int empId = Integer.parseInt(partss[1]);
                    String empName = partss[2];
                    out.println("ADMIN_HIRE_EMPLOYEE " + adminId + " " + empId + " " + empName);
                    System.out.println("Server: " + in.readLine());
                }
                case 5 -> {
                    System.out.print("Enter New Member ID and Name: ");
                    String[] addMem = sc.nextLine().split(" ", 2);
                    if (addMem.length < 2) {
                        System.out.println("Invalid input.");
                        break;
                    }
                    int newMemId = Integer.parseInt(addMem[0]);
                    String memName = addMem[1];
                    out.println("ADMIN_ADD_MEMBER " + newMemId + " " + memName);
                    System.out.println("Server: " + in.readLine());
                }
                case 6 -> {
                    out.println("ADMIN_VIEW_BOOKS"); 
                    System.out.println("--- List of All Books ---");
                    String line;
                    while (!(line = in.readLine()).equals("END")) {
                        System.out.println("Server: " + line);
                    }
                }
                case 7 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void authorMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter Author ID: ");
        int authorId = sc.nextInt();
        sc.nextLine();
        out.println("AUTHOR_VIEW_BOOKS " + authorId);
        System.out.println("--- Books by Author ---");
        String line;
        while (!(line = in.readLine()).equals("END")) {
            System.out.println("Server: " + line);
        }
    }

    private static void publisherMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        System.out.print("Enter Publisher ID: ");
        int pubId = sc.nextInt();
        sc.nextLine();
        out.println("PUBLISHER_VIEW_BOOKS " + pubId);
        System.out.println("--- Books by Publisher ---");
        String line;
        while (!(line = in.readLine()).equals("END")) {
            System.out.println("Server: " + line);
        }
    }

    private static void searchMenu(Scanner sc, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Search by : Book ID\n");

        System.out.print("Enter ID to search: ");
        int value = sc.nextInt();
        sc.nextLine();
        out.println("SEARCH_BOOK " + value);
        System.out.println("--- Search Results ---");
        String line;
        while (!(line = in.readLine()).equals("END")) {
            System.out.println("Server: " + line);
        }
    }
    
}
