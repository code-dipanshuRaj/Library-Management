package library_management.utils;

import java.sql.*;

public class QueryHandler {
    private Connection connection;

    public QueryHandler(Connection connection) {
        this.connection = connection;
    }

    // MEMBER: Get the status of a book
    public String getBookStatus(int bookId) {
        String result;
        String query = "SELECT Book_status FROM BOOKS WHERE Book_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = "Book Status: " + rs.getString("Book_status");
            } else {
                result = "Book not found.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // MEMBER: Request a book
    public String requestBook(int memId, int reqBookId, int empId) {
        String result;
        int requestId = (int) (Math.random() * 10000);
        String bookStatus = null;

        String checkBookStatusQuery = "SELECT Book_status FROM BOOKS WHERE Book_id = ?";
        String insertRequestQuery = "INSERT INTO BOOK_REQUEST (Request_id, Mem_id, Book_id, Request_date, Status) VALUES (?, ?, ?, CURDATE(), ?)";
        String insertIssueQuery = "INSERT INTO BOOK_ISSUE (Issue_id, Emp_id, Request_id, Issue_date, Return_date) VALUES (?, ?, ?, CURDATE(),  CURDATE() + INTERVAL 7 DAY)";
        String updateBookStatusQuery = "UPDATE BOOKS SET Book_status = 'Issued' WHERE Book_id = ?";

        try (
            PreparedStatement checkStmt = connection.prepareStatement(checkBookStatusQuery);
            PreparedStatement requestStmt = connection.prepareStatement(insertRequestQuery);
            PreparedStatement issueStmt = connection.prepareStatement(insertIssueQuery);
            PreparedStatement updateStmt = connection.prepareStatement(updateBookStatusQuery)
        ) {
            // Step 1: Check book status
            checkStmt.setInt(1, reqBookId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                bookStatus = rs.getString("Book_status");
            } else {
                return "Book not found.";
            }

            // Step 2: Insert into BOOK_REQUEST
            String status = bookStatus.equalsIgnoreCase("Available") ? "Approved" : "Pending";
            requestStmt.setInt(1, requestId);
            requestStmt.setInt(2, memId);
            requestStmt.setInt(3, reqBookId);
            requestStmt.setString(4, status);
            int reqResult = requestStmt.executeUpdate();

            if (reqResult > 0) {
                // Step 3: If approved, issue the book
                if (status.equals("Approved")) {
                    int issueId = (int) (Math.random() * 10000); // You can use AUTO_INCREMENT in real DB
                    issueStmt.setInt(1, issueId);
                    issueStmt.setInt(2, empId);
                    issueStmt.setInt(3, requestId);
                    issueStmt.executeUpdate();

                    // Step 4: Update book status
                    updateStmt.setInt(1, reqBookId);
                    updateStmt.executeUpdate();

                    result = "Book request approved and book issued. Request ID: " + requestId;
                } else {
                    result = "Book is currently not available. Your request is added to the waitlist. Request ID: " + requestId;
                }
            } else {
                result = "Failed to create book request.";
            }

        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    // MEMBER: Return a book
    public String returnBook(int bookId) {
        String result;
        String query = "UPDATE BOOKS SET Book_status = 'Available' WHERE Book_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                result = "Book returned successfully and marked as Available.";
            } else {
                result = "Failed to return book. Book might not exist.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // VENDOR: View all books
    public String viewAllBooks() {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM BOOKS";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("Book_id");
                float price = rs.getFloat("Book_price");
                String status = rs.getString("Book_status");
                result.append("ID: ").append(id)
                      .append(", Price: ").append(price)
                      .append(", Status: ").append(status).append("\n");
            }
            if (result.length() == 0) {
                result.append("No books found.");
            }
        } catch (SQLException e) {
            result.append("Error: " + e.getMessage());
        }
        return result.toString();
    }

    // VENDOR: Add sale
    public String addSale(int soldBookId, int vendorId) {
        String result;
        String query = "UPDATE BOOKS SET Book_status = 'Sold' WHERE Book_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, soldBookId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                result = "Sale recorded for Book ID " + soldBookId + " by Vendor " + vendorId;
            } else {
                result = "Failed to record sale. Book not found.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // ADMIN: Add a new book
    public String addBook(int newBookId, float price, String status) {
        String result;
        String query = "INSERT INTO BOOKS (Book_id, Book_price, Book_status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newBookId);
            ps.setFloat(2, price);
            ps.setString(3, status);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                result = "Book added successfully.";
            } else {
                result = "Failed to add book.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // ADMIN: Delete a book
    public String deleteBook(int delId) {
        String result;
        String query = "DELETE FROM BOOKS WHERE Book_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, delId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                result = "Book deleted successfully.";
            } else {
                result = "Book not found or failed to delete.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // ADMIN: View all members
    public String viewAllMembers() {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM MEMBER";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int memId = rs.getInt("Mem_id");
                String name = rs.getString("Name");
                result.append("Member ID: ").append(memId)
                      .append(", Name: ").append(name).append("\n");
            }
            if (result.length() == 0) {
                result.append("No members found.");
            }
        } catch (SQLException e) {
            result.append("Error: " + e.getMessage());
        }
        return result.toString();
    }

    // ADMIN: Hire employee
    public String hireEmployee(int adminId, int empId, String empName) {
        String result;
        try {
            PreparedStatement adminCheck = connection.prepareStatement("SELECT * FROM ADMIN WHERE Admin_id = ?");
            adminCheck.setInt(1, adminId);
            ResultSet rs1 = adminCheck.executeQuery();
            if (!rs1.next()) return "Admin ID " + adminId + " does not exist.";

            PreparedStatement empUpdate = connection.prepareStatement("INSERT INTO EMPLOYEE (Emp_id, Emp_name) VALUES (?, ?)");
            empUpdate.setInt(1, empId);
            empUpdate.setString(2, empName);
            int rows = empUpdate.executeUpdate();
            if (rows <= 0) {
                return "Failed to hire employee.";
            }

            PreparedStatement stmt = connection.prepareStatement("INSERT INTO HIRES (Admin_id, Emp_id, Hire_date) VALUES (?, ?, CURDATE())");
            stmt.setInt(1, adminId);
            stmt.setInt(2, empId);
            stmt.executeUpdate();

            result = "Employee " + empId + " hired successfully by Admin " + adminId;
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // ADMIN: Add a new member
    public String addMember(int memId, String name) {
        String result;
        String query = "INSERT INTO MEMBER (Mem_id, Name) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, memId);
            ps.setString(2, name);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                result = "Member added successfully.";
            } else {
                result = "Failed to add member.";
            }
        } catch (SQLException e) {
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    // AUTHOR: View books written by them
    public String viewBooksByAuthor(int authorId) {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM BOOKS WHERE Book_id in (SELECT Book_id FROM book_author WHERE Author_code = ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, authorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.append("Book ID: ").append(rs.getInt("Book_id"))
                      .append(", Price: ").append(rs.getFloat("Book_price"))
                      .append(", Status: ").append(rs.getString("Book_status"))
                      .append("\n");
            }
            if (result.length() == 0) result.append("No books found for given Author ID: ").append(authorId);
        } catch (SQLException e) {
            result.append("Error: ").append(e.getMessage());
        }
        return result.toString();
    }

    // PUBLISHER: View books published by them
    public String viewBooksByPublisher(int publisherId) {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM BOOKS WHERE Book_id in (SELECT Book_id FROM book_publishing WHERE Publisher_code = ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, publisherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.append("Book ID: ").append(rs.getInt("Book_id"))
                      .append(", Price: ").append(rs.getFloat("Book_price"))
                      .append(", Status: ").append(rs.getString("Book_status"))
                      .append("\n");
            }
            if (result.length() == 0) result.append("No books found for given Publisher ID: ").append(publisherId);
        } catch (SQLException e) {
            result.append("Error: ").append(e.getMessage());
        }
        return result.toString();
    }

    // GENERAL: Search book by BookID, AuthorID, or PublisherID
    public String searchBook(int value) {
        StringBuilder result = new StringBuilder();
        String query = "SELECT * FROM BOOKS WHERE Book_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, value);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.append("Book ID: ").append(rs.getInt("Book_id"))
                      .append(", Price: ").append(rs.getFloat("Book_price"))
                      .append(", Status: ").append(rs.getString("Book_status"))
                      .append("\n");
            }
            if (result.length() == 0) result.append("No books found.");
        } catch (SQLException e) {
            result.append("Error: ").append(e.getMessage());
        }
        return result.toString();
    }
}
