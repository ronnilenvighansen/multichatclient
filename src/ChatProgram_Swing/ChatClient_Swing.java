package ChatProgram_Swing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dave on 21/02/2017.
 */
public class ChatClient_Swing {

    // Textfields and Frames
    private static BufferedReader read;
    private static PrintWriter write;
    private static JFrame frame = new JFrame("ChatWindow");
    private static JTextField txtField = new JTextField(40);
    private static JTextField txtAreaInvis = new JTextField(40);
    private static JTextArea txtArea = new JTextArea(8, 40);


    /** Main method - Runs the client end system */
    public static void main(String[] args) throws IOException {

        // Timer used by ALIVE heartbeat protocol
        Timer timer = new Timer();
        timer.schedule(new Alive(), 60000, 60000);

        try {
            ChatClient_Swing client = new ChatClient_Swing();
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setVisible(true);
            client.frame.setSize(1200, 900);
            client.serverConn();
        }
        catch (NullPointerException nullex) {
            System.out.println("Closed application.");
            System.exit(0);
        }
    }

    /** Client Constructor */
    public ChatClient_Swing() {

    // Graphical User Interface - frames / textfields
        txtField.setEditable(false);
        txtAreaInvis.setVisible(false);
        txtAreaInvis.setEditable(true);
        txtArea.setEditable(false);
        frame.getContentPane().add(txtField, "North");
        frame.getContentPane().add(new JScrollPane(txtArea), "Center");
        frame.pack();

    // Action Listeners
        txtField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                write.println(txtField.getText());
                txtField.setText("");
            }
        });
    }

    /** Prompts the user for the servers IP address */
    private String promptForServerIP() {
        return JOptionPane.showInputDialog(frame,
                "Enter IP Address of the server:",
                "Welcome to the Chat Client",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /** Prompts the user for a temporary username */
    private String getClientName() {
        return JOptionPane.showInputDialog(frame,
                "Choose a chat name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /** Connects to the ChatServer end system */
    private void serverConn() throws IOException {

        // Initializing connection - constructing socket using TCP Protocol, using IP address and Port number
        String serverIP = promptForServerIP();
        Socket socket = new Socket(serverIP, 6660);

        // calling BufferedReader and PrintWriter with Input/OutputStreams to read/write data through sockets
        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        write = new PrintWriter(socket.getOutputStream(), true);

        // while loop - processing protocol-messages from the server
        while (true) {
            String line = read.readLine();

            // receive JOIN protocol-message from ChatServer, creating client as thread at ChatServer
            if (line.startsWith("JOIN")) {
                write.println(getClientName());
            }

            // receive J_OK protocol-message from ChatServer, acknowledges client as thread at ChatServer
            else if (line.startsWith("J_OK")) {
                txtField.setEditable(true);
            }

            // receive DATA or LIST protocol-message from ChatServer, enables writing a message
            else if (line.startsWith("DATA") || line.startsWith("LIST")) {
                txtArea.append(line.substring(8) + "\n");
            }

            // receive J_ERROR_USERNAME protocol-message from ChatServer, prompts user for different username
            else if (line.startsWith("J_ERROR_USERNAME")) {
                txtArea.append("\n !! Error in Username, please try again.. \n");
            }

            // receive J_ERROR_LENGTH protocol-message from ChatServer, prompts user for shorter message
            else if (line.startsWith("J_ERROR_LENGTH")) {
                txtArea.append("\n !! Message is too long, maximum 250 chars.. \n");
            }
        }
    }

    /** Static class containing the run method - what the ALIVE protocol does */
    private static class Alive extends TimerTask {

        public void run() {
            System.out.println("ALIVE");
            write.println(txtAreaInvis.getText());
            txtAreaInvis.setText("*");


        }
    }
}
