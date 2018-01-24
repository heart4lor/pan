package Pan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
	public static String host = "localhost";
	public static int port = 14301;

	Client() {
		setLayout(new FlowLayout());
		setBounds(600, 300, 300, 150);
		setTitle("Login");
		String lookAndFeel =UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JTextField username = new JTextField(20);
		JPasswordField password = new JPasswordField(20);
		JButton login = new JButton("登陆");
		JButton regist = new JButton("注册");

		add(new JLabel("用户名: "));
		add(username);
		add(new JLabel(" 密码:  "));
		add(password);
		add(login);
		add(regist);

		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		login.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Socket client = new Socket(host, port);
					DataOutputStream out = new DataOutputStream(client.getOutputStream());
					DataInputStream in = new DataInputStream(client.getInputStream());
					out.writeUTF("login");
					out.writeUTF(username.getText());
					out.writeUTF(String.valueOf(password.getPassword()));
					String result = in.readUTF();
					if(result.equals("ok")) {
						JOptionPane.showMessageDialog(null, "登陆成功!", "got it", JOptionPane.INFORMATION_MESSAGE);
						new MainUI(client, username.getText());
						dispose();
					}
					else if(result.equals("no such user"))
						JOptionPane.showMessageDialog(null, "登陆失败!\n数据库中没有这个用户,你可能还没有注册", "emmmmm",JOptionPane.ERROR_MESSAGE);
					else if(result.equals("passwd err"))
						JOptionPane.showMessageDialog(null, "登陆失败!\n用户名或密码不正确", "emmmmm",JOptionPane.ERROR_MESSAGE);

//					dispose();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		regist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Regist();
			}
		});
	}

	public static void main(String[] args) throws Exception {
		new Client();
	}
}
