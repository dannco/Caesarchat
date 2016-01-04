
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends JFrame
        implements KeyListener {
            
    static final boolean ENCRYPT = true;
    static final boolean DECRYPT = false;
            
            
    private String host = "localhost";
    private String pass, passKey;
    private String encryptionKey;
    private int port = 2000;
    
    private Socket s;
    private ClientIn in;
    private OutputStreamWriter output;

    JTextArea textField;
    JTextField inputField;

    public Client(String[] args) {
        super("");
        if (args.length>0) host = args[0];
        if (args.length>1) port = Integer.parseInt(args[1]);

        textField = new JTextArea(18,75);
        textField.setLineWrap(true);
        textField.setEditable(false);
        JPanel tpan = new JPanel();
        tpan.setLayout(new BoxLayout(tpan,BoxLayout.PAGE_AXIS));
        tpan.add(new JLabel("Log"));
        tpan.add(new JScrollPane(textField));
        inputField = new JTextField(75);
        inputField.addKeyListener(this);
        tpan.add(inputField);
        JButton send = new JButton("Send");
        send.addActionListener(new SendListener());
        tpan.add(send);
        add(tpan);

        inputField.requestFocus();

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        if (setClient()==true)run();
        else System.exit(0);
    }

    public boolean setClient() {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("Set host and port of desired server.");
        p1.add(l1);
        JPanel p2 = new JPanel();
        JLabel l2 = new JLabel("Port:");
        JPanel p3 = new JPanel();
        JLabel l3 = new JLabel("Address:");
        JPanel p4 = new JPanel();
        JLabel l4 = new JLabel("Password:");
        JTextField input1 = new JTextField(6);
        JTextField input2 = new JTextField(6);
        JTextField input3 = new JTextField(6);
        input1.setText(""+port);
        input2.setText(""+host);
        p2.add(l2);
        p2.add(input1);
        p1.add(p2);
        p3.add(l3);
        p3.add(input2);
        p1.add(p3);
        p4.add(l4);
        p4.add(input3);
        p1.add(p4);
        int ok = JOptionPane.showConfirmDialog(null, p1, "",JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                int p = Integer.parseInt(input1.getText());
                port=p;
                host = input2.getText();
                pass = input3.getText();
                passKey = "";
				
                for (int i = 0 ; i < pass.length(); i++) {
                    passKey += pass.charAt(pass.length()-(1+i));
                }
                return true;
            } catch (NumberFormatException ex) {ex.printStackTrace();}
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==10) send();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }


    public void send() {
        String line = inputField.getText().trim();
        inputField.setText("");
        send(line);
    }
    public void send(String line) {
        if (!line.equals("")) {
            try {
                if (line.equals(".")) {
                    s.close();
                    textField.append("You have disconnected from the server");
                }

                if (encryptionKey==null) {
                    output.write(line.trim()+"\n\0");
                    output.flush();
                } else {
                    output.write(Cipher.run(line.trim(),encryptionKey,ENCRYPT)+"\n\0");
                    output.flush();
                }
            } catch (IOException ex) {}
        }
    }
    
    protected void receive(String msg) {
        if (encryptionKey==null && msg.matches("HELLO!")) {
            System.out.println("Server has acknowledged you. Sending authorization.");
            send("$AUTH$:"+Cipher.run(pass,passKey,ENCRYPT));

        } else if (encryptionKey==null && msg.matches("\\$KEY\\$:.+")) {
            String serverAuth = msg.replaceFirst("\\$KEY\\$:","");
            encryptionKey = Cipher.run(serverAuth,passKey,DECRYPT);
            textField.append("Connected to server " + port + ".\nClose this window or send . to end session.\n");
        } else if (encryptionKey==null && msg.matches("\\$NOAUTH\\$")){
            textField.append("Connection failed; invalid password.");
        } else {
            textField.append(Cipher.run(msg,encryptionKey,DECRYPT)+"\n");
        }
    }

    class SendListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            send();
        }
    }

    public void run() {
        try {
            s = new Socket(host, port);
            setTitle(host+" : "+port);
            in = new ClientIn();
            output = new OutputStreamWriter(s.getOutputStream());
        }
        catch (UnknownHostException ex) {
            textField.append(ex.toString());
        }
        catch (IOException ex) {
            textField.append(ex.toString());
        }
    }

    private class ClientIn extends Thread{
        public ClientIn() {
            start();
        }
        public void run() {
            StringBuffer resp = new StringBuffer();
            try {
                InputStream respStream = s.getInputStream();
                int c;

                while ((c = respStream.read()) != -1) {
                    if (c==0) {
                        Client.this.receive(resp.toString());
                        resp.delete(0,resp.length());
                    }
                    else {
                        resp.append((char)c);
                    }
                }
            } catch (IOException e) {}
            try {
                s.close();
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args){
        new Client(args);
    }
}
