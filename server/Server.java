package library_management.server;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;

import library_management.db.DBConnector;
import library_management.utils.QueryHandler;

public class Server {
    public static void main(String[] args) {
        int port = 5000;
        Connection dbConnection = null;  // Declare outside to avoid try-with-resources issues
        ServerSocket serverSocket = null;
        
        try {
            // Start the server socket
            serverSocket = new ServerSocket(port);
            System.out.println("🚀 Server started on port " + port);
            
            // Obtain the DB connection outside try-with-resources
            dbConnection = DBConnector.getConnection();
            
            // Continuously accept new clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("👤 New client connected: " + clientSocket.getInetAddress());
                // Create a new thread to handle the client
                new Thread(new ClientHandler(clientSocket, dbConnection)).start();
            }
        } catch (IOException | SQLException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            // Close the server socket if it's not null
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
            // Close the database connection explicitly
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing DB connection: " + e.getMessage());
                }
            }
        }
    }
}

// Runnable ClientHandler class to manage each client's communication
class ClientHandler implements Runnable {
    private Socket socket;
    private Connection dbConnection;

    public ClientHandler(Socket socket, Connection dbConnection) {
        this.socket = socket;
        this.dbConnection = dbConnection;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            QueryHandler handler = new QueryHandler(dbConnection);
            String input;
            while ((input = in.readLine()) != null) {
                if (input.equals("EXIT"))
                    break;

                String[] tokens = input.split(" ");
                String command = tokens[0];

                switch (command) {
                    // MEMBER Commands
                    case "MEMBER_GET_BOOK":
                        int bookId = Integer.parseInt(tokens[1]);
                        out.println(handler.getBookStatus(bookId));
                        break;
                    case "MEMBER_REQUEST_BOOK":
                        if (tokens.length < 4) {
                            out.println("Usage: MEMBER_REQUEST_BOOK <BookID> <MemberID> <EmpID>");
                        } else {
                            try {
                                int reqBookId = Integer.parseInt(tokens[1]);
                                int memId = Integer.parseInt(tokens[2]);
                                int empId = Integer.parseInt(tokens[3]);
                                String result = handler.requestBook(memId, reqBookId, empId);
                                out.println(result);
                            } catch (NumberFormatException e) {
                                out.println("Invalid input. Book ID, Member ID, and Emp ID must be integers.");
                            }
                        }
                        break;
                    case "MEMBER_RETURN_BOOK":
                        int returnBookId = Integer.parseInt(tokens[1]);
                        out.println(handler.returnBook(returnBookId));
                        break;
                    case "MEMBER_VIEW_BOOKS":
                        String memberBooks = handler.viewAllBooks();
                        for (String line : memberBooks.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;
                    // VENDOR Commands
                    case "VENDOR_VIEW_BOOKS":
                        String bookList = handler.viewAllBooks();
                        for (String line : bookList.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;
                    case "VENDOR_ADD_SALE":
                        int soldBookId = Integer.parseInt(tokens[1]);
                        int vendorId = Integer.parseInt(tokens[2]);
                        out.println(handler.addSale(soldBookId, vendorId));
                        break;
                    // ADMIN Commands
                    case "ADMIN_ADD_BOOK":
                        int newBookId = Integer.parseInt(tokens[1]);
                        float price = Float.parseFloat(tokens[2]);
                        String status = tokens[3];
                        out.println(handler.addBook(newBookId, price, status));
                        break;
                    case "ADMIN_DELETE_BOOK":
                        int delId = Integer.parseInt(tokens[1]);
                        out.println(handler.deleteBook(delId));
                        break;
                    case "ADMIN_VIEW_MEMBERS":
                        String response = handler.viewAllMembers();
                        for (String line : response.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;
                    case "ADMIN_HIRE_EMPLOYEE":
                        int adminId = Integer.parseInt(tokens[1]);
                        int empId = Integer.parseInt(tokens[2]);
                        StringBuilder empNameBuilder = new StringBuilder();
                        for (int i = 3; i < tokens.length; i++) {
                            empNameBuilder.append(tokens[i]).append(" ");
                        }
                        String empName = empNameBuilder.toString().trim();
                        out.println(handler.hireEmployee(adminId, empId, empName));
                        break;
                    case "ADMIN_ADD_MEMBER":
                        int newMemId = Integer.parseInt(tokens[1]);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            nameBuilder.append(tokens[i]).append(" ");
                        }
                        String name = nameBuilder.toString().trim();
                        out.println(handler.addMember(newMemId, name));
                        break;
                    case "ADMIN_VIEW_BOOKS":
                        String allBooks = handler.viewAllBooks();
                        for (String line : allBooks.split("\n")) {
                        out.println(line);
                        }
                        out.println("END");
                        break;
                    // AUTHOR Commands
                    case "AUTHOR_VIEW_BOOKS":
                        int authorId = Integer.parseInt(tokens[1]);
                        String authorBooks = handler.viewBooksByAuthor(authorId);
                        for (String line : authorBooks.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;

                    // PUBLISHER Commands
                    case "PUBLISHER_VIEW_BOOKS":
                        int publisherId = Integer.parseInt(tokens[1]);
                        String pubBooks = handler.viewBooksByPublisher(publisherId);
                        for (String line : pubBooks.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;
                    // GENERAL Search
                    case "SEARCH_BOOK":
                        // String criteria = tokens[1]; // should be "Book_id", "Author_id", or "Publisher_id"
                        int value = Integer.parseInt(tokens[1]);
                        String searchResult = handler.searchBook(value);
                        for (String line : searchResult.split("\n")) {
                            out.println(line);
                        }
                        out.println("END");
                        break;

                    default:
                        out.println("Unknown command: " + command);
                }
            }
            socket.close();
            System.out.println("❌ Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
