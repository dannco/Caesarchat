
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends JFrame {
    private ArrayList<ServerIn> in;
    private ArrayList<ServerOut> out;
    private int port = 2000;
    private String pass, passKey;
    
    static int users = 0;
    static final boolean ENCRYPT = true;
    static final boolean DECRYPT = false;
    static final int ENCRYPTION_KEY_LEN = 12;
    static final int INVALID_KEY = 0;
    
    private DefaultListModel<String> sockList;
    private JList<String> jlist;
    private JTextArea textField;

    public Server(String[] args) {
        super("");
        if (args.length>0) port = Integer.parseInt(args[0]);

        setResizable(false);
        sockList = new DefaultListModel<String>();
        jlist = new JList<String>(sockList);
        jlist.setVisibleRowCount(18);
        jlist.setLayoutOrientation(JList.VERTICAL);
        jlist.setFixedCellWidth(300);
        JPanel vpanel = new JPanel();
        vpanel.setLayout(new BoxLayout(vpanel,BoxLayout.PAGE_AXIS));
        vpanel.add(new JLabel("Connected Users"));
        vpanel.add(new JScrollPane(jlist));
        add(vpanel, BorderLayout.WEST);
        textField = new JTextArea(18,75);
        textField.setLineWrap(true);
        textField.setEditable(false);
        JPanel tpan = new JPanel();
        tpan.setLayout(new BoxLayout(tpan,BoxLayout.PAGE_AXIS));
        tpan.add(new JLabel("Log"));
        tpan.add(new JScrollPane(textField));
        add(tpan, BorderLayout.EAST);

        System.out.println("Server running.");
        in = new ArrayList<ServerIn>();
        out = new ArrayList<ServerOut>();
        if (args.length>0) {
            port = Integer.parseInt(args[0]);
        }
        new Janitor(this);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        if (setPort()==true) run();
        else System.exit(0);
    }

    public void broadCast(String s) {
        textField.append(s+"\n");
        for (int i=out.size()-1; i>=0; i--) {
            if (out.get(i).isAlive()) {
                out.get(i).send(s);
            }
            else out.remove(i);
        }
    }
    public boolean setPort() {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("Enter port number and password for new server");
        p1.add(l1);
        JPanel p2 = new JPanel();
        JLabel l2 = new JLabel("Port:");
        JPanel p3 = new JPanel();
        JLabel l3 = new JLabel("Password:");
        JTextField input = new JTextField(6);
        input.setText(""+port);
        JTextField input2 = new JTextField(6);
        p2.add(l2);
        p2.add(input);
        p1.add(p2);
        p3.add(l3);
        p3.add(input2);
        p1.add(p3);
        int ok = JOptionPane.showConfirmDialog(null, p1, "",JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                int p = Integer.parseInt(input.getText());
                port = p;
                pass = input2.getText();
                passKey = "";
                for (int i = 0 ; i < pass.length(); i++) {
                    passKey += pass.charAt(pass.length()-(1+i));
                }
                return true;
            } catch (NumberFormatException ex) {}
        }
        return false;
    }

    public void newCon(Socket s) {
        String key = Cipher.newKey(ENCRYPTION_KEY_LEN);
        System.out.println("Key generated for new user: "+key);
        ServerOut newOut = new ServerOut(s,key);
        out.add(newOut);
        in.add(new ServerIn(s,key,newOut));
        System.out.println("Attempted connection from "+s.toString());
        sockList.addElement(s.toString());
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            setTitle(Inet4Address.getLocalHost().toString()+" : "+port+" | Password: \""+pass+"\"");
            while (true) {
                Socket con = server.accept();
                newCon(con);
            }

        } catch (IOException e){
            textField.append(e.toString());
            textField.append("Server already on port "+port);

        } catch (IllegalArgumentException e) {
            textField.append(e.toString());
        }
        System.out.println(port+" server closed.");
    }

    private class ServerOut {
        Socket s;
        OutputStreamWriter out;
        
        String key;
        
        public ServerOut(Socket s,String key) {
            this.s = s;
            this.key = key;
            try {
                out = new OutputStreamWriter(s.getOutputStream());
                out.write("HELLO!\0");
                out.flush();
            } catch (IOException e) {
                textField.append(e.toString());
            }
                
        }
        public boolean isAlive() {
            return (!s.isClosed());
        }
        public void send(String s) {
            try {
                out.write(Cipher.run(s,key,ENCRYPT)+"\0");
                out.flush();
            } catch (IOException e) {
                textField.append(e.toString());
            }
        }
        public void kick(int reason) {
            try {
                if (reason == Server.INVALID_KEY) {
                    out.write("$NOAUTH$\0");
                    out.flush();
                }
                s.close();
            } catch (IOException e) {
                textField.append(e.toString());
            }
        }
        
        public void authorize() {
            try {
                String newKey = Cipher.run(key,Server.this.passKey,Server.ENCRYPT);
                out.write("$KEY$:"+newKey+"\0");
                out.flush();
                System.out.println("Connection established with " + s.toString());
                broadCast("["+s.getInetAddress().toString()+" : "+s.getPort()+"] has connected.\0");
                out.flush();
            } catch (IOException e) {
                textField.append(e.toString());
            }
        };
    }

    private class ServerIn extends Thread {
        
        Socket sock;
        String key, nick;
        ServerOut out;
        
        boolean auth = false;
        public ServerIn(Socket s2, String key, ServerOut out) {
            sock = s2;
            this.key = key;
            this.out = out;
            nick = "User"+Server.this.users++;
            start();
        }
        public void run() {
            StringBuffer inc = new StringBuffer();
            try {
                InputStream in = sock.getInputStream();
                int c;
                while ((c = in.read()) != -1) {
                    if (c==0) {
                        if (!auth && inc.toString().trim().matches("\\$AUTH\\$:.*")) {
                            String userPass = inc.toString().replaceFirst("\\$AUTH\\$:","");
                            userPass = Cipher.run(userPass,passKey,DECRYPT);
                            if (Server.this.pass.equals(userPass)) {
                                auth = true;
                                out.authorize();
                            } else {
                                System.out.println("Invalid key");
                                out.kick(Server.INVALID_KEY);
                            }
                        } else if (auth) {
                        String msg = Cipher.run(inc.toString(),key,Server.DECRYPT);
                        broadCast("["+nick+"] : "+msg);
                        }
                        inc.delete(0,inc.length());
                    }
                    else {
                        inc.append((char)c);
                    }
                }
            } catch (IOException e) {}
            try {
                String closedown = sock.toString();
                sock.close();
                sockList.removeElement(closedown);
                broadCast(closedown + " has disconnected.\n");
            } catch (IOException e) {
                textField.append(e.toString());
            }
        }
    }

    private class Janitor extends Thread{
        public Janitor(Server s) {
            start();
        }
        public void run() {
            while (true) {
                for (int i=out.size()-1; i>=0; i--) {
                    if (!out.get(i).isAlive()) out.remove(i);
                }
                for (int i=in.size()-1; i>=0; i--) {
                    if (!in.get(i).isAlive()) in.remove(i);
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    textField.append(e.toString());
                }
            }
        }
    }
    public static void main(String args[]) {
        new Server(args);
    }
}
