package Pan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainUI extends JFrame {
	private static Socket client = null;
	private static String username;
	private static DataInputStream in;
	private static DataOutputStream out;
	private static FileInputStream fin;
	private static FileOutputStream fout;

	public MainUI(Socket client, String username) {
		this.client = client;
		this.username = username;

		setLayout(null);
		setBounds(300, 200, 800, 600);
		setTitle("我的云盘");
		String lookAndFeel =UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JLabel l1 = new JLabel("欢迎回来," + username);
		l1.setBounds(20, 20, 800, 20);
		JButton download = new JButton("下载选中文件");
		download.setBounds(20, 50, 130, 30);
		JButton delete = new JButton("删除");
		delete.setBounds(680, 50, 70, 30);
		JButton upload = new JButton("上传");
		upload.setBounds(600, 50, 70, 30);
		JTable filelist = new JTable(10, 2);
//		filelist.setBounds(40, 100, 700, 400);
		filelist.setRowHeight(30);
		JScrollPane scr = new JScrollPane(filelist);
		scr.setBounds(15, 100, 750, 430);

		upload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.showOpenDialog(null);
					File f = fileChooser.getSelectedFile();
					Timestamp ts = new Timestamp(System.currentTimeMillis());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					String time = sdf.format(ts);
					sendFile(f, time);
					Thread.sleep(100);
					refreshFileList(filelist);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int[] rows = filelist.getSelectedRows();
					out = new DataOutputStream(client.getOutputStream());
					out.writeUTF("delete");
					out.writeUTF(username);
					out.writeInt(rows.length);
					for (int i : rows) {
						out.writeUTF((String) filelist.getValueAt(i, 0));
						out.writeUTF((String) filelist.getValueAt(i, 1));
					}
					Thread.sleep(100);
					refreshFileList(filelist);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		download.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] rows = filelist.getSelectedRows();
				JFileChooser jfc = new JFileChooser("选择要下载到的目录");
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showOpenDialog(null);
				String dir = jfc.getSelectedFile().getPath();
				try {
					in = new DataInputStream(client.getInputStream());
					out = new DataOutputStream(client.getOutputStream());
					out.writeUTF("download");
					out.writeUTF(username);
					out.writeInt(rows.length);
					for(int i :rows) {
						String filename = (String) filelist.getValueAt(i, 0);
						String time = (String) filelist.getValueAt(i, 1);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						SimpleDateFormat tssdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						StringBuilder sb = new StringBuilder(filename);
						sb.insert(filename.lastIndexOf("."), "_" + sdf.format(tssdf.parse(time)));
						String filename_time = sb.toString();
						out.writeUTF(filename_time);
						long fileLength = in.readLong();
						System.out.println(fileLength);

						File f = new File(dir + "\\" +filename_time);
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
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		add(l1);
		add(download);
		add(delete);
		add(upload);
		add(scr);

		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		refreshFileList(filelist);
	}

	private static void sendFile(File f, String time) throws Exception {
		try {
			fin = new FileInputStream(f);
			out = new DataOutputStream(client.getOutputStream());

			out.writeUTF("upload");
			out.writeUTF(f.getName());
			out.writeUTF(username);
			out.writeUTF(time);
			out.writeLong(f.length());
			System.out.println(f.length());

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

	private static void refreshFileList(JTable filelist) {
		String[][] data = getFileList(username);
		String[] columNames = {"名称", "日期版本"};
		DefaultTableModel model = new DefaultTableModel(data, columNames);
		RowSorter sorter = new TableRowSorter(model);
		filelist.setModel(model);
		filelist.setRowSorter(sorter);
	}

	private static String[][] getFileList(String username) {
		ArrayList<String[]> r = new ArrayList();
		Connection con;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/pan?useSSL=true";
		String sqluser = "root";
		String sqlpasswd = "4e6d";
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, sqluser, sqlpasswd);
			Statement statement = con.createStatement();
			String querysql = "SELECT `filename`, `timestamp` FROM `files` WHERE `owner` = '%s'";
			String sql = String.format(querysql, username);
			ResultSet sqlresult = statement.executeQuery(sql);
			while(sqlresult.next()) {
				String[] afile = new String[2];
				afile[0] = sqlresult.getString(1);
				afile[1] = sqlresult.getString(2);
				r.add(afile);
			}
			return r.toArray(new String[r.size()][2]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	public static void main(String[] args) {
		new MainUI(null, "test");
	}
}
