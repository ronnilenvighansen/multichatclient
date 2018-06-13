package ChatProgram_Graba;


import java.io.IOException;
import java.net.*;

/**
 * Created by Dave on 22/02/2017.
 */
public class ChatServer_Graba {

    private static final int PORT = 1234;
    private static DatagramSocket datagramSocket;

    public static void main(String[] args) throws SocketException {

        System.out.println("Opening port..");
        datagramSocket = new DatagramSocket(PORT);
        try {
            while (true) {
                new ClientHandler(datagramSocket).start();
            }
        } finally {
            datagramSocket.close();
        }
    }

    private static class ClientHandler extends Thread {

        private static DatagramSocket datagramSocket;
        private static DatagramPacket inPckt;
        private static DatagramPacket outPckt;
        private static byte[] buffer;


        public ClientHandler(DatagramSocket datagramSocket) {
            this.datagramSocket = datagramSocket;
        }

        public void run() {

            try {
                String msgIn;
                String msgOut;
                int numOfMsg = 0;
                InetAddress clientAddress = null;
                int clientPort;
               // DatagramSocket socket;

                do {

                    buffer = new byte[256];
                    inPckt = new DatagramPacket(buffer, buffer.length);

                    datagramSocket.receive(inPckt);

                    clientAddress = inPckt.getAddress();
                    clientPort = inPckt.getPort();

                    msgIn = new String(inPckt.getData(), 0, inPckt.getLength());

                    System.out.println("Message received.");
                    numOfMsg++;
                    msgOut = "Message " + numOfMsg + ": " + msgIn;

                    outPckt = new DatagramPacket(msgOut.getBytes(), msgOut.length(), clientAddress, clientPort);
                    datagramSocket.send(outPckt);

                }
                while (true);

            }
            catch (IOException ioex) {
                ioex.printStackTrace();
            }
            finally {

                System.out.println("Closing connection..");
                datagramSocket.close();
            }

        }

    }


}



