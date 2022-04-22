import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{
    ServerSocket theServer;
    static int num_threads = 10;

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket ss = new ServerSocket(5050);
            System.out.println("Server Socket Start!! on 5050");
            for (int i = 0; i < num_threads; i++) {
                System.out.println("Create num_threads " + i + " Port: 5050.");
                Server server = new Server(ss);
                server.start();
            }
        } catch (IOException e) { System.err.println(e); }
    }

    public Server(ServerSocket ss) { theServer = ss; }

    public void run() {
        while (true) {
            try {
                Socket connection = theServer.accept();
                DataOutputStream output = new
                        DataOutputStream(connection.getOutputStream());
                DataInputStream input = new DataInputStream(connection.getInputStream());
                System.out.println("Client Connected and Start get I/O!!");
                System.out.println("==> Input from Client: " + input.readUTF());
                System.out.println("Output to Client ==> \"Connection successful\"");
                output.writeUTF( "Connection successful" );
                output.flush();
                input.close();
                connection.close();
            } catch (IOException e) { }
        }
    }
}