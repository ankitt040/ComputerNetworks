/*
 * ankit 4441, akshay 4451, alok 4461, anish 4471
 * anna 4481
 * 
 */
package lab3;

import java.net.Socket;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 *
 * @author ANKIT
 */
public class Server {
    private int running;
    private final int port;
    private SQLiteJDBC db;
    private HashMap<String,String> clientsIP;
    private String gchat_leader;
    
    public Server(int port){this.port=port;db=new SQLiteJDBC(); clientsIP=new HashMap<>(); }
    public void listen(){
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server waiting for clients at port: "+port);
            while(true){
                Socket client_socket= ss.accept();
                System.out.println("Connected to a client: "+client_socket.getRemoteSocketAddress());
                new ClientListener(client_socket).start();
                //System.out.println("got a client listener/");
            }
        } catch (IOException ex) {
            
        }
        System.out.println("listen --/");
    }
    
    class ClientListener extends Thread{
        private String un;
        private boolean keeprunning;
        Socket s;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        public ClientListener(Socket s){this.s=s;keeprunning=true;}
        
        public void init_input_streams(){
            //System.out.println("init_input_streams _0 --/");
            try {
                oos = new ObjectOutputStream(s.getOutputStream());
                ois = new ObjectInputStream(s.getInputStream());
            } catch (Exception e) {
                //System.out.println("init_input_streams excp --/");
                keeprunning=false;
            }
            //System.out.println("init_input_streams --/");
        }
        int get_port(String s){
            int port_listening=0;
            if( s.equalsIgnoreCase("ankit") ){ port_listening=4441; }
            else if( s.equalsIgnoreCase("akshay") ){ port_listening=4451; }
            else if( s.equalsIgnoreCase("alok") ){ port_listening=4461; }
            else if( s.equalsIgnoreCase("anish") ){ port_listening=4471; }
            else if( s.equalsIgnoreCase("anna") ){ port_listening=4481; }
            return port_listening;
        }
        @Override
        public void run() {
            System.out.println("ClientListener run _0 --/");
            init_input_streams();
            Message msg;
            while(keeprunning){
                try {
                    //System.out.println("ClientListener run _0 msg_0--/");
                    msg = (Message) ois.readObject();
                    //System.out.println("ClientListener run _1 --/");
                    System.out.println("MESSAGE:"+msg.getType()+":"+msg.getUN()+":"+msg.getPass()+":"+msg.getMsg());
                    switch(msg.getType()){
                        case Message.DISCONNECT:
                            send( new Message(Message.DISCONNECT) );
                            stopListening();
                            if(true) return ;
                            break;
                        case Message.REGISTER:
                            if( !db.con() ){ System.out.println("could not connect to db"); break; }
                            if( db.add_user(msg.getUN(), msg.getPass()) ){
                                send( new Message(Message.T_REGISTER) );
                            }else{ send( new Message(Message.F_REGISTER) ); }
                            db.close();
                            break;
                        case Message.LOGIN:
                            if( !db.con() ){ System.out.println("could not connect to db"); break; }
                            if( db.check_login(msg.getUN(), msg.getPass()) ){
                                this.un = msg.getUN();
                                clientsIP.put(msg.getUN(),s.getInetAddress().getHostAddress() );
                                send( new Message(Message.T_LOGIN,msg.getUN(),"",""+get_port(msg.getUN())) );
                                send( new Message(Message.FRIENDS_LIST, db.getFriends(msg.getUN())  ) );
                                send( new Message(Message.USERS_LIST, db.getUsers() ) );
                            }else{ send( new Message(Message.F_LOGIN) ); }
                            db.close();
                            break;
                        case Message.LOGOUT:
                            if(this.un != null)
                                try {clientsIP.remove(this.un); this.un=null;} catch (Exception e) {}
                            send( new Message(Message.LOGOUT) );
                            break;
                        case Message.ADD_TO_LIST:
                            if( !db.con() ){ System.out.println("could not connect to db"); break; }
                            if( db.add_friend(this.un, msg.getMsg()) ){
                                send( new Message(Message.T_ADD_TO_LIST,msg.getMsg()) );
                            }else{ send( new Message(Message.F_ADD_TO_LIST,msg.getMsg()) ); }
                            db.close();
                            break;
                        case Message.GET_IP:
                            if( clientsIP.containsKey(msg.getMsg()) ){
                                send( new Message(Message.T_GET_IP, clientsIP.get(msg.getMsg())+":" + get_port(msg.getMsg()) ) );
                            }else{ send( new Message(Message.F_GET_IP,msg.getMsg()) ); }
                            break;
                        case Message.CHAT_MSG:
                            if( msg.getMsg().trim().equalsIgnoreCase("hi") ){
                                send( new Message(Message.CHAT_MSG,"Hello") );
                            }else{ send( new Message(Message.CHAT_MSG,"Bye") ); }
                            break;
                        case Message.GET_IP_gchat:
                            if( clientsIP.containsKey(msg.getMsg()) ){
                                send( new Message(Message.T_GET_IP, clientsIP.get(msg.getMsg())+":" +  get_port(msg.getMsg()) ) );
                            }else{ send( new Message(Message.F_GET_IP_gchat,msg.getMsg()) ); }
                            break;
                        case Message.LEADER_gchat:
                            if( gchat_leader == null ){
                                gchat_leader=msg.getUN();
                            }
                            send( new Message(Message.LEADER_gchat,gchat_leader,""+get_port(gchat_leader),clientsIP.get(gchat_leader)  )); 
                            
                            break;
                            
                        case Message.GET_IP_p2pchat:
                            if( clientsIP.containsKey(msg.getMsg()) ){
                                send( new Message(Message.T_GET_IP_p2pchat,msg.getMsg(), ""+get_port(msg.getMsg()), clientsIP.get(msg.getMsg()) ) );
                            }else{ send( new Message(Message.F_GET_IP_p2pchat,msg.getMsg()) ); }
                            break;
                        default:
                            send( new Message(Message.CHAT_MSG,"Could not parse !") );
                            break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("ClientListener run excp --/");
                    stopListening();
                }
            }
            System.out.println("ClientListener run  --/");
        }
        public void stopListening(){
            keeprunning=false;
            try { ois.close();} catch (Exception e) { }
            try { oos.close();} catch (Exception e) { }
            try { s.close();} catch (Exception e) { }
            System.out.println("stopListening --/");
        }
        public void send(Message m){
            try {
                oos.writeObject(m);
                //System.out.println("send msg --/");
            } catch (Exception e) {
                System.out.println("send msg excp --/");
            }
        }
    }  
    
    public static void main(String args[] ){
        new Server(1500).listen();
    }
}
