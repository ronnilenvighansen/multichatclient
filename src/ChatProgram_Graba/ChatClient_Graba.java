package ChatProgram_Graba;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

/**
 * Created by Dave on 21/02/2017.
 */
public class ChatClient_Graba {

    private static InetAddress host;
    private static final int PORT = 1234;
    private static DatagramSocket datagramSocket;
    private static DatagramPacket inPckt;
    private static DatagramPacket outPckt;
    private static byte[] buffer;

    public static void main(String[] args) {

        try {
            host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException uex) {
            System.out.println("Unable to establish connection to host, try again.");
            System.exit(1);
        }
        serverConn();
    }

    private static void serverConn() {

        try {

            datagramSocket = new DatagramSocket();

            // set up stream for key entry
            Scanner userInput = new Scanner(System.in);

            String message;
            String response;

            do {
                System.out.println("Enter a message: ");
                message = userInput.nextLine();

                if (!message.equals("*CLOSE*")) {
                    outPckt = new DatagramPacket(message.getBytes(), 0, message.length(), host, PORT);

                    datagramSocket.send(outPckt);
                    buffer = new byte[256];

                    inPckt = new DatagramPacket(buffer, buffer.length);

                    datagramSocket.receive(inPckt);

                    response = new String(inPckt.getData(), 0, inPckt.getLength());

                    System.out.println("\nSERVER> " + response);


                }
            }
            while (!message.equals("*CLOSE*"));

        }
        catch (IOException ioex) {
            ioex.printStackTrace();
        }
        finally {
                System.out.println("\n* Closing connection... *");
                datagramSocket.close();

        }
    }
}
