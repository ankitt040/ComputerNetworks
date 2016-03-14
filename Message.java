/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab3;

import java.io.Serializable;
//import javax.swing.JOptionPane;

/**
 *
 * @author ANKIT
 */
public class Message implements Serializable{
    static final int CONNECT=3,DISCONNECT=4,REGISTER=5,LOGIN=6,LOGOUT=7,ADD_TO_LIST=8,GET_IP=9,CHAT_MSG=0;
    static final int T_REGISTER=15,T_LOGIN=16,T_LOGOUT=17,T_ADD_TO_LIST=18,T_GET_IP=19,T_CHAT_MSG=10;
    static final int F_REGISTER=25,F_LOGIN=26,F_LOGOUT=27,F_ADD_TO_LIST=28,F_GET_IP=29,F_CHAT_MSG=20;
    static final int FRIENDS_LIST=61,USERS_LIST=62;
    static final int GET_IP_gchat=91,T_GET_IP_gchat=191,F_GET_IP_gchat=291,LEADER_gchat=92,ADD_TO_GCHAT_GROUP=93;
    static final int GET_IP_p2pchat=95,T_GET_IP_p2pchat=195,F_GET_IP_p2pchat=295;
    private int type;
    private String msg,un,pass;
    public Message(int t){type=t;}
    public Message(int t,String m){type=t;msg=m;}
    public Message(int t,String u,String p){type=t;un=u;pass=p;}
    public Message(int t,String u,String p,String m){type=t;un=u;pass=p;msg=m;}
    int getType(){return this.type;}
    String getMsg(){return this.msg;}
    String getUN(){return this.un;}
    String getPass(){return this.pass;}
//    public static void main(String[] args) {
//        JOptionPane.showMessageDialog(null, "My Goodness, this is so concise");
//    }
}
