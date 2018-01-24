package Pan;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerThread extends Thread {
	private static Socket client;
	private static DataInputStream in;
	private static DataOutputStream out;
	private static FileInputStream fin;
	private static FileOutputStream fout;

	public ServerThread(Socket client) {
		this.client = client;
	}
	private static void sendFile(File f) throws Exception {
		try {
			fin = new FileInputStream(f);
			out = new DataOutputStream(client.getOutputStream());

			System.out.println(f.length());
			out.writeLong(f.length());

			byte[] bytes = new byte[1024];
			int length = 0;
			while((length = fin.read(bytes)) != -1) {
				out.write(bytes, 0, length);
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updatesql(String filename, String owner, Timestamp time) {
		Connection con;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/pan?useSSL=true";
		String sqluser = "root";
		String sqlpasswd = "4e6d";
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, sqluser, sqlpasswd);
			Statement statement = con.createStatement();
			String insertsql = "INSERT INTO `files` (`filename`, `owner`, `timestamp`) VALUES ('%s', '%s', '%s')";
			String sql = String.format(insertsql, filename, owner, time);
			statement.executeUpdate(sql);
			con.close();
			System.out.println("sql updated");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (true) {
				in = new DataInputStream(client.getInputStream());
				out = new DataOutputStream(client.getOutputStream());
				Thread.sleep(100);
				String str = in.readUTF();
				if (str.equals("exit"))
					break;
				else if (str.equals("upload")) {
					String filename = in.readUTF();
					String username = in.readUTF();
					String time = in.readUTF();
					long fileLength = in.readLong();

					StringBuilder sb = new StringBuilder(filename);
					sb.insert(filename.lastIndexOf("."), "_" + time);
					String filename_time = sb.toString();

					File f = new File(username + "\\" + filename_time);
					File dir = f.getParentFile();
					System.out.println(f);
					if (!dir.exists())
						dir.mkdir();
					fout = new FileOutputStream(f);
					byte[] bytes = new byte[1024];
					int length = 0;
					long curLength = 0;
					while ((length = in.read(bytes)) != -1) {
						fout.write(bytes, 0, length);
						fout.flush();
						curLength += length;
						System.out.println(curLength);
						if(curLength >= fileLength)
							break;
					}
					System.out.println("success");
					fout.close();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					SimpleDateFormat tssdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date ts = sdf.parse(time);
					System.out.println(tssdf.format(ts));
					updatesql(filename, username, Timestamp.valueOf(tssdf.format(ts)));
				} else if (str.equals("delete")) {
					System.out.println("send delete");
					String username = in.readUTF();
					int n = in.readInt();
					Connection con;
					String driver = "com.mysql.jdbc.Driver";
					String url = "jdbc:mysql://localhost:3306/pan?useSSL=true";
					String sqluser = "root";
					String sqlpasswd = "4e6d";
					try {
						Class.forName(driver);
						con = DriverManager.getConnection(url, sqluser, sqlpasswd);
						Statement statement = con.createStatement();
						String delsql = "DELETE FROM `files` WHERE (`filename`, `owner`, `timestamp`) = ('%s', '%s', '%s')";
						for(int i = 0; i < n; i++) {
							String filename = in.readUTF();
							String ts = in.readUTF();
							String sql = String.format(delsql, filename, username, ts);
							statement.executeUpdate(sql);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							SimpleDateFormat tssdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							StringBuilder sb = new StringBuilder(filename);
							sb.insert(filename.lastIndexOf("."), "_" + sdf.format(tssdf.parse(ts)));
							String filename_time = sb.toString();
							File f = new File(username + "//" + filename_time);
							System.out.println(f.getName());
							f.delete();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else if(str.equals("download")) {
					String username = in.readUTF();
					int n = in.readInt();
					while(n-- > 0) {
						System.out.println(n);
						String filename = in.readUTF();
						File f = new File(username + "\\" + filename);
						System.out.println(f.getName());
						sendFile(f);
					}
				}
			}
		} catch (EOFException eof) {

		} catch (Exception e) {
			e.printStackTrace();
			try {
				join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
//			e.printStackTrace();
		}
	}
}
