/*
 * 
 * 
 * 
 */
package lab3;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author ANKIT
 */
public class Client { 
    static String un_loggedin;
    private boolean connected,loggedin;
    // for I/O with server
    private ObjectInputStream ois;		// to read from the socket
    private ObjectOutputStream oos;		// to write on the socket
    private Socket socket;
    private Client_GUI cg;
    P2P p;
    P2P_Group p2p_group;
     HashMap<String,P2P_connectTo> nodes_p2p_connectedTo;
    public Client(Client_GUI cg){ this.cg=cg;  nodes_p2p_connectedTo = new HashMap<String,P2P_connectTo>();}
    
    public boolean connectToServer(String ip,int port){
        try {
            socket = new Socket(ip,port);
            System.out.println(socket.getLocalSocketAddress());
            System.out.println(socket.getRemoteSocketAddress());
           //System.out.println("connectToServer_1 --/");
        } catch (Exception e) {
           // e.printStackTrace();
            //System.out.println("connectToServer excp --/");
            return false;
        }
        try {
            //if( socket.isConnected() ){System.out.println("socket connected");}
            //if(socket.isInputShutdown()){System.out.println("isInputShutdown"); }
            //System.out.println("connectToServer_2 inputstream --/");
            
            oos= new ObjectOutputStream( socket.getOutputStream() );
           // System.out.println("connectToServer_2 oos --/");
            ois= new ObjectInputStream(socket.getInputStream()) ;
           // System.out.println("connectToServer_2 ois --/");
           // System.out.println("connectToServer_2 --/");
        } catch (Exception e) {
           // System.out.println("connectToServer excp2 --/");
            return false;
        }
        connected =true;
        //System.out.println("connectToServer_3 --/");
        new ServerListener().start();
        //System.out.println("connectToServer --/");
        return true;
    }
    public void disconnect(){
        connected=false;loggedin=false;
        try { ois.close(); } catch (Exception e) {}
        try { oos.close(); } catch (Exception e) {}
        try { socket.close(); } catch (Exception e) {}
        try { cg.notify_disconnect(); } catch (Exception e) {}
    }
    public void sendToServer(Message m){
        try {
            oos.writeObject(m);
            // System.out.println("sent");
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Exception_sendToServer");
        }
    }
    public void register(String u,String p){ sendToServer( new Message(Message.REGISTER,u,p ) ); }
    public void login(String u,String p){ sendToServer( new Message(Message.LOGIN,u,p ) ); }
    public void logout(){ sendToServer( new Message(Message.LOGOUT) ); }
    public void add_friend(String u,String f){ sendToServer( new Message(Message.ADD_TO_LIST,u,"",f ) ); }
    public void get_ip(String u,String f){ sendToServer( new Message(Message.GET_IP,u,"",f ) ); }
    public void get_ip_p2pchat(String u,String friend){
        if( nodes_p2p_connectedTo.containsKey(friend) ){
           cg.notify_getIP_p2pchat(friend); 
        }else{
            sendToServer( new Message(Message.GET_IP_p2pchat,u,"",friend ) );
        } 
    }
    public void p2pchat_msg_handle(String from, String msg){
        cg.set_panel_p2pchat(false, "", 3, from+"->"+msg, 0);
    }
    public void get_ip_gchat(String u,String f){ sendToServer( new Message(Message.GET_IP_gchat,u,"",f ) ); }
    public void send_P2P_connectTo(String from,String to,String m){
        try {
            nodes_p2p_connectedTo.get(to).send(from, m);
        } catch (Exception e) {
            System.out.println("Client send_P2P_connectTo excp --/");
        }
    }
    class ServerListener extends Thread{
        Message sm;//server message
        public void run(){
            while(connected)
            {
                try 
                {
                    sm = (Message) ois.readObject();
                    try {
                        System.out.println("MESSAGE:"+sm.getType()+":"+sm.getUN()+":"+sm.getPass()+":"+sm.getMsg());
                    } catch (Exception e) {
                        System.out.println("ServerListener run excp_1 --/");
                    }
                    switch(sm.getType()){
                        case Message.T_REGISTER:
                            cg.notify_registered();
                            break;
                        case Message.F_REGISTER:
                            cg.notify_reg_failed();
                            break;
                        case Message.T_LOGIN:
                            un_loggedin=sm.getUN();
                            cg.notify_login();
                            try {
                                p= new P2P( Integer.parseInt(sm.getMsg().trim()) );
                                System.out.println(" p2p listner ");
                            } catch (Exception e) {
                                System.out.println("ServerListener run excp_1");
                            }
                            p.start();
                            break;
                        case Message.F_LOGIN:
                            cg.notify_login_fail();
                            break;
                        case Message.LOGOUT:
                            cg.notify_logout();
                            break;
                        case Message.T_ADD_TO_LIST:
                            cg.notify_add_to_list(sm.getMsg());
                            break;
                        case Message.F_ADD_TO_LIST:
                            cg.notify_add_fail();
                            break;
                        case Message.T_GET_IP:
                            cg.notify_getIP(sm.getMsg());
                            break;
                        case Message.F_GET_IP:
                            cg.notify_getIP_fail();
                            break;
                        case Message.FRIENDS_LIST:
                            cg.notify_friendslist(sm.getMsg());
                            break;
                        case Message.USERS_LIST:
                            cg.notify_userslist(sm.getMsg());
                            break;
                        case Message.CHAT_MSG:
                            //cg.notify_chatmsg(sm.getMsg());
                            JOptionPane.showMessageDialog(null, "Chat Msg: "+sm.getMsg());
                            break;
                        
                        case Message.T_GET_IP_gchat:
                            cg.notify_getIP_gchat(sm.getMsg());
                            break;
                        case Message.F_GET_IP_gchat:
                            cg.set_panel_gchat_tab1(true, "unavailable", 0, "",0);
                            break;
                        case Message.T_GET_IP_p2pchat:
                           // cg.notify_getIP_p2pchat(sm.getMsg());
                            try {
                                nodes_p2p_connectedTo.put(sm.getUN(), new P2P_connectTo(sm.getMsg(),Integer.parseInt(sm.getPass()) ));
                                nodes_p2p_connectedTo.get(sm.getUN()).start();
                                nodes_p2p_connectedTo.get(sm.getUN()).send_add_req_gchat_group();
                                
                                cg.notify_getIP_p2pchat(sm.getUN());
                            } catch (Exception e) {
                                System.out.println("Client run T_GET_IP_p2pchat excp --/");
                            }
                            break;
                        case Message.F_GET_IP_p2pchat:
                            cg.notify_getIP_p2pchat_fail();
                            break;
                        case Message.LEADER_gchat:
                            if(sm.getUN().equalsIgnoreCase(Client.un_loggedin) ){
                                JOptionPane.showMessageDialog(null, "ServerListener LEADER_gchat leader->self");
                            } 
                            else if( nodes_p2p_connectedTo.containsKey(sm.getUN()) ){  
                                JOptionPane.showMessageDialog(null, "ServerListener LEADER_gchat leader->"+sm.getUN());
                                nodes_p2p_connectedTo.get(sm.getUN()).send_add_req_gchat_group();
                            }else{
                                try {
                                    nodes_p2p_connectedTo.put(sm.getUN(), new P2P_connectTo(sm.getMsg(),Integer.parseInt(sm.getPass()) ));
                                    nodes_p2p_connectedTo.get(sm.getUN()).start();
                                    nodes_p2p_connectedTo.get(sm.getUN()).send_add_req_gchat_group();
                                } catch (Exception e) {
                                    System.out.println("Client run LEADER_gchat excp --/");
                                }
                            }
                            if(p2p_group == null)
                            p2p_group = new P2P_Group(sm.getUN()); 
                            break;
                    }
                } catch (Exception e) {  System.out.println("Exception_run_server_listener");disconnect(); break;}
            
            }
            System.out.println("Exiting ServerListener");
        }
    }
    
    class P2P extends Thread{
        private int port;
        private boolean running;
        //private HashMap<String,P2PListener> nodes_connected;
        private ServerSocket ss;
        public P2P(int port){ 
            this.port=port;
            //nodes_connected=new HashMap<String,P2PListener>();
            try {
                ss= new ServerSocket(port);
                System.out.println("P2P waiting for nodes :");
            } catch (Exception e) {
                System.out.println("P2P excp --/");
                return ;
            }
        }
        public void run(){
            System.out.println("P2P run_0 ");
            while(true){
                try {
                    Socket s= ss.accept();
                    System.out.println("P2P run Connected to a P2PNode: "+s.getRemoteSocketAddress());
                    
                    P2P_connectTo l = new P2P_connectTo(s); // no need of P2PListener<REDUNDANT> == P2P_connectedTo
                    l.start();
                } catch (IOException ex) {
                    System.out.println("P2P run excp --/");
                    //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    return ;
                }
            }
        }
        public void send(String from,String to,String msg){
            try {
                nodes_p2p_connectedTo.get(to).send( new Message_P2P(Message_P2P.MSG,from,msg) );
            } catch (Exception e) {
                System.out.println("P2P send excp --/");
            }
        }
    }
   
    class P2P_connectTo extends Thread{
        private int port;
        private String ip,un_connectedTo;
        private Socket s;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        P2P_connectTo(Socket s){ this.s=s; }
        P2P_connectTo(String i,int p){ 
            ip=i;port=p;
            try { 
                s= new Socket(i,p);
            } catch (IOException ex) {
                System.out.println("P2P_connectTo excp --/");
               
            }
        }
        public void send_add_req_gchat_group(){
            send( new Message_P2P(Message_P2P.NODE_NAME,""+Client.un_loggedin,"") );
        }
        public void send_gchat(String u,String m){ send( new Message_P2P(Message_P2P.MSG_GROUP,u,m) ); }
        public void send(String u,String m){ send( new Message_P2P(Message_P2P.MSG,u,m) ); }
        //public void send(String m){ send( new Message_P2P(Message_P2P.MSG,"",m) ); }
        public void send(Message_P2P m){
            try {
                oos.writeObject(m);
                oos.flush();
                System.out.println("P2P_connectedTo send "+un_connectedTo+":"+m.getType()+":"+m.getUN()+":"+m.getMsg());
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("P2P_connectTo send excp --/");
            }
        }
        @Override
        public void run(){
            try {
                
                oos = new ObjectOutputStream(s.getOutputStream());
                ois = new ObjectInputStream(s.getInputStream());
                System.out.println("connected");
                //send( new Message_P2P(Message_P2P.NODE_NAME,""+Client.un_loggedin,"") );
                
            } catch (Exception e) {
                System.out.println("P2P_connectTo run excp --/");
                return ;
            }
            Message_P2P msg;
            while(true){
                try {
                    msg = (Message_P2P) ois.readObject();
                    System.out.println("P2P_connectTo run msg : "+msg.getType()+":"+msg.getUN()+":"+msg.getMsg());
                    switch(msg.getType()){
                        case Message_P2P.NODE_NAME:
                            this.un_connectedTo = msg.getUN();
                            nodes_p2p_connectedTo.put(msg.getUN(), this);
                            break;
                        case Message_P2P.MSG:
                            cg.set_panel_p2pchat(false, "", 3, ""+msg.getUN()+":"+msg.getMsg()+"\n", 0);
                            JOptionPane.showMessageDialog(null, "msg from"+msg.getUN());
                            break;
                        case Message_P2P.MSG_GROUP:
                                JOptionPane.showMessageDialog(null, msg.getUN()+" MSG_GROUP says "+msg.getMsg());
                                cg.set_panel_gchat_tab1(false, "", 3, msg.getUN()+":"+msg.getMsg()+"\n", 0);
                                if(p2p_group.if_group_leader)
                                    p2p_group.send(msg.getUN(), msg.getMsg());
                                break;
                        case Message_P2P.ADD_TO_GROUP:
                            JOptionPane.showMessageDialog(null, Client.un_loggedin+" (P2P_connectedTo)add regquest from "+msg.getUN());
                            p2p_group.add(msg.getUN());
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "-P2P_connectedTo-"+msg.getMsg());
                            
                    }
                    
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("P2P_connectTo run io excp --/");
                    break;
                }
            }
            System.out.println("P2P_connectTo run done--/");
        }
    }
   
    class P2P_Group{
        final boolean if_group_leader;
        final String leader;
        HashSet<String> group;
        public P2P_Group(String leader){
            this.leader = leader;
            if( this.leader.equalsIgnoreCase(Client.un_loggedin) ){
                if_group_leader=true;
                group = new HashSet<>();
            }else{ 
                if_group_leader = false;
                try {
                    nodes_p2p_connectedTo.get(leader).send(new Message_P2P(Message_P2P.ADD_TO_GROUP,Client.un_loggedin,"" ));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("P2P_Group excp --/");
                }
            }
        }
        public void add(String s){group.add(s);}
        public void send(String from,String msg){
            if(if_group_leader){
                try {
                    for(String s : group){
                        if(! s.equalsIgnoreCase(from) )
                            nodes_p2p_connectedTo.get(s).send_gchat(from, msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("P2P_Group send excp --/");
                }
            }else{
                try {
                    nodes_p2p_connectedTo.get(leader).send_gchat(Client.un_loggedin, msg);
                } catch (Exception e) {
                    System.out.println("P2P_Group send excp _2 --/");
                }
            }
        }
        
    }
}
 // p2p messages
    class Message_P2P implements Serializable {
        static final int NODE_NAME=3,MSG=4,MSG_GROUP=5,ADD_TO_GROUP=6;
        private final int type;
        private String un,msg;
        public Message_P2P(int t,String un,String m){
            type=t;
            this.un=un;
            msg=m;
        }
        String getUN(){return un;}
        String getMsg(){return msg;}
        int getType(){return type;}
    }