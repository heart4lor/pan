package Pan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

class Regist extends JFrame {

	Regist() {
		setLayout(new FlowLayout());
		setBounds(650, 350, 300, 180);
		setTitle("Login");
		String lookAndFeel =UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JTextField username = new JTextField(18);
		JPasswordField password1 = new JPasswordField(18);
		JPasswordField password2 = new JPasswordField(18);
		JButton regist = new JButton("注册");

		add(new JLabel("请输入用户名:"));
		add(username);
		add(new JLabel("请输入密码:  "));
		add(password1);
		add(new JLabel("请确认密码:  "));
		add(password2);
		add(regist);

		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		regist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String passwd1 = new String(password1.getPassword());
				String passwd2 = new String(password2.getPassword());
				if(!passwd1.equals(passwd2)) {
					JOptionPane.showMessageDialog(null, "密码输入不一致!", "错误", JOptionPane.ERROR_MESSAGE);
					password1.setText("");
					password2.setText("");
				}
				else {
					try {
						Socket client = new Socket(Client.host, Client.port);
						PrintStream out = new PrintStream(client.getOutputStream());
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						out.println("regist");
						out.println(username.getText());
						out.println(passwd1);
						String result = in.readLine();
						if(result.equals("ok"))
							JOptionPane.showMessageDialog(null, "注册成功!", "got it",JOptionPane.INFORMATION_MESSAGE);
						else
							JOptionPane.showMessageDialog(null, "注册失败!\n" + result, "emmmmm",JOptionPane.ERROR_MESSAGE);
						dispose();
					} catch (Exception err) {
						err.printStackTrace();
					}
				}
			}
		});
	}
}
