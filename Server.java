package Pan;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class Server {
	private static int port = 14301;
	private static ServerSocket server;

	private static String login(String username, String password, Socket client) {
		Connection con;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/pan?useSSL=true";
		String sqluser = "root";
		String sqlpasswd = "4e6d";
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, sqluser, sqlpasswd);
			Statement statement = con.createStatement();
			String querysql = "SELECT `password` FROM `users` WHERE `username` = '%s'";
			String sql = String.format(querysql, username);
//			System.out.println(sql);
			ResultSet sqlresult = statement.executeQuery(sql);
			if(!sqlresult.next())
				return "no such user";
			String passwd0 = sqlresult.getString("password");
			if(!passwd0.equals(password))
				return "passwd err";
			con.close();
			new ServerThread(client).start();
			return "ok";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static String regist(String username, String password) {
		Connection con;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/pan?useSSL=true";
		String sqluser = "root";
		String sqlpasswd = "4e6d";
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, sqluser, sqlpasswd);
			Statement statement = con.createStatement();
			String insertsql = "INSERT INTO `users` (`username`, `password`) VALUES ('%s', '%s')";
			String sql = String.format(insertsql, username, password);
//			System.out.println(sql);
			statement.executeUpdate(sql);
			con.close();
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public static void main (String[] args) throws Exception {
		server = new ServerSocket(port);
		System.out.println("服务端已开启");
		while(true) {
			Thread.sleep(100);
			Socket client = server.accept();
			System.out.println("连接成功");
			DataInputStream in = new DataInputStream(client.getInputStream());
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			String cmd = in.readUTF();
			String name = in.readUTF();
			String password = in.readUTF();
			if(cmd.equals("regist"))
				out.writeUTF(regist(name, password));
			else
				out.writeUTF(login(name, password, client));
		}
	}

}
