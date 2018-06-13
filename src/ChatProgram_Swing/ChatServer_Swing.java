package ChatProgram_Swing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dave on 21/02/2017.
 */
public class ChatServer_Swing {


    // Sets and Variables
    private static final int PORT = 6660;
    private static int usernameMaxLength = 12;
    private static int msgMaxLength = 250;
    // HashSet of usernames of all the clients
    private static HashSet<String> clientNames = new HashSet<>();
    // HashSet of printwriters used by all clients
    private static HashSet<PrintWriter> printWriters = new HashSet<>();


    /** Main method - creates ClientHandler threads on client joins */
    public static void main(String[] args) throws IOException {

        System.out.println("Server is operational..");
        ServerSocket serverSocket = new ServerSocket(PORT);

        // open a socket
        try {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
        // close a socket
        finally {
            serverSocket.close();
        }
    }


    /** ClientHandler class extended from Thread Class to handle each customer paralleled */
    private static class ClientHandler extends Thread {

        // Variables
        private String name;
        private PrintWriter write;
        private BufferedReader read;
        private Socket socket;

        /** Thread Constructor */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }


        /** How each ClientHandler thread processes each client */
        public void run() {

            // creates new BufferedReader and PrintWriter, to be used as streaming channels pr client
            try {
                read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                write = new PrintWriter(socket.getOutputStream(), true);

                // while loop - handles when ChatClient tries to create username and join ChatServer
                while (true) {
                    // send JOIN protocol-message to client, prompting for a username
                    write.println("JOIN");
                    name = read.readLine();

                    // if name is empty, do nothing
                    if (name == null) {
                        return;
                    }
                    else {
                        // using synchronized keyword on the non-final HashSet because threads need to
                        synchronized (clientNames) {
                            // add client's username to HashSet if statement is true, and stops while loop
                            if (!clientNames.contains(name) && name.matches("[0-9a-zA-Z_-]+") && name.length() <= usernameMaxLength) {
                                clientNames.add(name);
                                break;
                            }
                            // checks if client's username is only alphanumeric and containing: '-' and '_', using a regular expression
                            // send J_ERR protocol-message to client, trigger error message on client side
                            else {
                                write.println("J_ERROR_USERNAME");
                            }
                        }
                    }
                }
                // sends message to ChatClient end system that writing to server is now OK
                write.println("J_OK");

                // add printWriter to set of PrintWriters
                printWriters.add(write);

                // Stores how long the system has been running currently
                long startHBTime = System.currentTimeMillis();
                long startHBTimeSec = TimeUnit.MILLISECONDS.toSeconds(startHBTime);
                // While loop - handles messages from client
                while (true) {
                    String input = read.readLine();

                    // if client message is QUIT, close socket
                    if (input.startsWith("QUIT")) {
                        socket.close();
                    }
                    // if client message is LIST, print a list of all usernames to client
                    if (input.startsWith("LIST")) {
                        write.println("LIST    " + clientNames);
                        System.out.println(clientNames);
                    }

                    if (input.startsWith("*")) {
                        long currentHBTime = System.currentTimeMillis();
                        long currentHBTimeSec = TimeUnit.MILLISECONDS.toSeconds(currentHBTime);
                        System.out.println("User" + name + "has been connected for: "+ (currentHBTimeSec - startHBTimeSec) + " seconds.");

                    }
                    // prints one client's message to all clients, checks for max length on message string
                    if (input.length() < msgMaxLength) {
                        for (PrintWriter writer : printWriters) {
                            // Client
                            writer.println("DATA    " + name + ": " + input);
                            // Server
                            System.out.println("User: " + name + " writes: " + input);
                        }
                    }
                    // sends error protocol message to client
                    else {
                        write.println("J_ERROR_LENGTH");
                    }
                }
            }
            // catches SocketException
            catch (IOException ioException) {
                System.out.println("User: " + name + " closed their connection.");
            }

            // Flushing ChatClient - removing the objects from the respective HashSets
            finally {
                // when flushing, clients username is removed from clientName HashSet
                if (name != null) {
                    clientNames.remove(name);
                }
                // when flushing, printWriter is removed from printWriters HashSet
                if (write != null) {
                    printWriters.remove(write);
                }
                // closing socket
                try {
                    socket.close();
                }
                catch (IOException ioException) {
                    System.out.println(ioException);
                }
            }
        }

    }


}
