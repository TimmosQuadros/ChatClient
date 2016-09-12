package Client;

import presentation.Observer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JTextArea;
import shared.ProtocolStrings;

public class EchoClient extends Thread {

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;
    private List<Observer> observers = new ArrayList<>();
    private String[] clientList;
    private boolean isLoggedIn = false;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public Socket getSocket() {
        return socket;
    }

    public void notifyAllObservers(String msg) {
        for (Observer observer : observers) {
            observer.begin(msg);
        }
    }

    public void registerObserver(Observer o) {
        observers.add(o);
    }

    public void connect(String address, int port) throws UnknownHostException, IOException {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    }

    public void send(String msg) {
        output.println(msg);
    }

    public void stopConnection() throws IOException {
        output.println(ProtocolStrings.STOP);
    }

    public String receive() {
        String msg = input.nextLine();
        notifyAllObservers(msg);
        if (msg.equals(ProtocolStrings.STOP)) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Log.LOG_NAME).log(Level.SEVERE, null, ex);
            }
        }
        return msg;
    }

    public boolean isStopped() {
        return socket.isClosed();
    }

    public void parseMessage(String msg, JList<String> jList1, JTextArea jTextArea1) {
        String[] splitColon = msg.split(":");
        String splitComma[];
        if (splitColon[0].equalsIgnoreCase(ProtocolStrings.CLIENTLIST)) {
            splitComma = splitColon[1].split(",");
            if (splitComma.length == 0) {
                splitComma = new String[1];
                splitComma[0] = splitColon[1];
            }
            clientList = splitComma;
            jList1.setModel(new AbstractListModel<String>() {
                @Override
                public int getSize() {
                    return clientList.length;
                }

                @Override
                public String getElementAt(int index) {
                    return clientList[index];
                }
            });
        } else if (splitColon[0].equalsIgnoreCase(ProtocolStrings.MESSAGERESPONSE)) {
            jTextArea1.append(splitColon[1] + ": " + splitColon[2] + "\n");
        } else {

        }
    }

    @Override
    public synchronized void run() {
        while (isLoggedIn) {
            System.out.println("Run");
            this.receive(); //Starts to listen for response from server
            System.out.println("Receive");
            if(!isLoggedIn){
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        super.run(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public synchronized void wakeUp(){
        this.notify();
    }

//    public static void main(String[] args) {
//        int port = 7777;
//        String ip = "localhost";
//        if (args.length == 2) {
//            ip = args[0];
//            port = Integer.parseInt(args[1]);
//        }
//        try {
//            EchoClient tester = new EchoClient();
//            tester.connect(ip, port);
//            System.out.println("Sending 'Hello world'");
//            tester.send("Hello World");
//            System.out.println("Waiting for a reply");
//            System.out.println("Received: " + tester.receive()); //Important Blocking call         
//            tester.stop();
//            //System.in.read();      
//        } catch (UnknownHostException ex) {
//            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
