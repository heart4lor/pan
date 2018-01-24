# 多版本云盘管理程序设计报告

## 一、需求和说明

### 1.1 设计背景

如今用户电脑上的文件越来越多，同一文件往往还有多个不同的版本，用户查找、管理文件很不方便且占用大量硬盘空间。在此背景下，一个方便的多版本云盘管理系统就显得十分必要，能一定程度上解决多版本文件管理的问题，并能节约本地磁盘空间。

### 1.2 设计内容

编写一个多版本云盘管理程序，分服务端和客户端两部分，能够在网络上集中管理每个用户的文档，并保存多个版本。

服务端能接受客户端发来的指令，从而实现对文件的上传下载和多版本管理。

客户端能够上传、下载和删除文件，且支持同一文件的不同版本，以时间戳区分。

### 1.3 任务和要求

该程序软件可以在任何一个标准配置的主机上运行，软件分为两部分：服务端小程序和客户端小程序。

1. 客户端：程序启动后输入用户名，然后可以查看本人上传的所有文档，包括文档的多个版本。可以上传、下载、删除和重新上传文档，上传是将本地的某个文件上传到服务端，每次重新上传都产生一个新的版本，原来的已上传仍然保存文件；
2. 服务端：接受并保存客户端上传的所有文件，并维护同一文件多次上传的版本；

要求：客户端和服务端采用 socket 通信，服务端采用文本文件保存文档的信息和版本信息，每个文档都归属于一个用户。用户查看文档的多个版本时，按时间逆序排列。

## 二、设计

### 2.1 设计思想

#### 2.1.1 程序结构

Pan
├── Client.java

├── MainUI.java

├── Regist.java

├── Server.java

└── ServerThread.java

#### 2.1.2 数据结构

本程序使用 MySQL 数据库记录用户和文件信息。

* 数据表 files

  | 字段        | 类型           | 含义   |
  | --------- | ------------ | ---- |
  | Id        | int(11)      | 序号   |
  | filename  | varchar(255) | 文件名  |
  | owner     | varchar(255) | 所有者  |
  | timestamp | timestamp    | 时间戳  |

* 数据表 users

  | 字段       | 类型           | 含义   |
  | -------- | ------------ | ---- |
  | Id       | int(11)      | 序号   |
  | username | varchar(255) | 用户名  |
  | password | varchar(255) | 密码   |


文件存储方式：

在当前目录下创建以用户名命名的文件夹，用户上传的同一文件的多个不同版本用时间戳分割，例如`readme_201801091523.txt`，其中`readme.txt`为原文件名，`201801091523`为格式化之后的时间戳，为保证文件仍可被正确识别，将时间戳添加在文件名之后，后缀名之前，中间以下划线`_`隔开。

#### 2.1.3 主要算法思想

* 服务端运行流程：

  启动 Server 类会开启一个 ServerSocket 并监听14301端口 ，程序进入阻塞状态开始接收客户端连接。客户端连接之后接收客户端发送来的指令，实现注册和登陆功能。登陆成功后会为该客户端开启一个 ServerThread 线程，来处理接下来客户端发来的请求。

* 客户端运行流程：

  启动 Client 类，弹出登陆/注册界面，用户点击按钮后把相应的信息发送到服务端处理，如果登陆成功服务端则返回字符串"ok"。登陆成功后客户端会实例化一个 MainUI 窗体，窗体中包含操作按钮和文件列表。点击`上传`按钮可以上传文件，选中文件后可以点击`下载选中文件`或`删除`按钮，客户端会将对应的指令和信息发送到服务端处理。

### 2.2 设计表示

#### 2.2.1 类名及作用

- Server 类实现注册和登陆部分的通信，如果登陆成功则实例化一个 ServerThread 线程继续和该客户端通信。
- ServerThread 类实现能接受客户端发送来的处理文件的指令进行操作，和客户端通信。
- Client 类展示一个登陆对话框，把用户名和密码发送给服务端。
- Regust 类展示一个注册窗体，把用户名和密码发送给服务端。
- MainUI 类展示云盘的主界面，用户可以选择文件、点击按钮进行操作，和服务端进行通信。

#### 2.2.2 类中数据成员

* Server 类

  | 数据成员     | 含义      |
  | -------- | ------- |
  | port     | 服务端监听端口 |
  | cmd      | 用户发来的命令 |
  | username | 用户名     |
  | password | 密码      |

* ServerThread 类

  | 数据成员   | 含义         |
  | ------ | ---------- |
  | client | 客户端 socket |
  | in     | 客户端发送来的数据流 |
  | out    | 向客户端发送的数据流 |
  | fin    | 文件输入流      |
  | fout   | 文件输出流      |

* Client 类

  | 数据成员 | 含义    |
  | ---- | ----- |
  | host | 服务器地址 |
  | port | 服务端端口 |

* Regist 类

  | 数据成员     | 含义   |
  | -------- | ---- |
  | username | 用户名  |
  | password | 密码   |

* MainUI 类

  | 数据成员     | 含义         |
  | -------- | ---------- |
  | server   | 服务端 socket |
  | username | 用户名        |
  | in       | 服务端发送来的数据流 |
  | out      | 向服务端发送的数据流 |
  | fin      | 文件输入流      |
  | fout     | 文件输出流      |

#### 2.2.3 类中函数原型 

* Server 类

  | 方法                                       | 说明       |
  | ---------------------------------------- | :------- |
  | String login(String username, String password, Socket client) | 登陆函数     |
  | String regist(String username, String password) | 注册函数     |
  | public static void main (String[] args)  | 主函数，运行入口 |

* ServerThread 类

  | 方法                                       | 说明              |
  | ---------------------------------------- | --------------- |
  | ServerThread(Socket client)              | 构造方法            |
  | void sendFile(File f)                    | 发送文件            |
  | void updatesql(String filename, String owner, Timestamp time) | 更新数据库           |
  | void run()                               | 实现的 Runnable 方法 |

* Client 类

  | 方法                        | 说明       |
  | ------------------------- | -------- |
  | Client()                  | 构造方法     |
  | void main (String[] args) | 主函数，运行入口 |

* Regist 类

  | 方法       | 说明   |
  | -------- | ---- |
  | Regist() | 构造函数 |

* MainUI 类

  | 方法                                      | 说明                 |
  | --------------------------------------- | ------------------ |
  | MainUI(Socket client, String username)  | 构造方法               |
  | void sendFile(File f, String time)      | 上传文件               |
  | void refreshFileList(JTable filelist)   | 刷新文件列表             |
  | String[][] getFileList(String username) | 根据 username 获取文件列表 |
  | void main(String[] args)                | 主函数，运行入口           |

### 2.3 实现注释

要求：客户端和服务端采用socket通信，服务端采用文本文件保存文档的信息和版本信息，每个文档都归属于一个用户。用户查看文档的多个版本时，按时间逆序排列。

上述要求功能均已实现，在此基础上，本程序还具有用户密码登陆、注册功能，使用数据库来保存用户信息和文件信息。此外，本程序还能对文件名和修改时间进行排序，实现精确查找。

### 2.4 详细表示

- 服务端运行流程：

  启动 Server 类会开启一个 ServerSocket 并监听14301端口 ，程序进入阻塞状态开始接收客户端连接。客户端连接之后接收客户端发送来的指令，实现注册和登陆功能。登陆时调用 login 函数，注册时调用 regist 函数。登陆成功后会为该客户端开启一个 ServerThread 线程，来处理接下来客户端发来的请求。ServerThread线程中接收用户请求，updatesql 函数用来更新数据库，sendFile 函数用来向客户端发送要下载的文件。

- 客户端运行流程：

  启动 Client 类，弹出登陆/注册界面，用户点击按钮后把相应的信息发送到服务端处理，如果登陆成功服务端则返回字符串"ok"。注册窗体初始化一个 Regist 类对象。登陆成功后客户端会实例化一个 MainUI 窗体，窗体中包含操作按钮和文件列表。点击`上传`按钮可以上传文件，选中文件后可以点击`下载选中文件`或`删除`按钮，客户端会将对应的指令和信息发送到服务端处理。sendFile 函数向客户端发送要上传的文件，refreshFilelist 函数从数据库更新文件列表，getFilelist 函数获取文件列表信息。

## 三、调试及测试

### 3.1 调试中遇到的问题

本程序编写过程中遇到过以下问题：

* 文件接收无法结束

  问题原因：参照教程写的代码结束标志为-1，表示EOF文件结束，但是这样表示需要断掉数据流，导致 socket 连接断开。

  解决办法：首先发送方获取文件长度，把长度信息发送给接收方，然后接收方统计已经接收的长度，如果大于或等于标准长度就停止接收。

* 文件接收后损坏，无法打开

  问题原因：二进制文件例如exe，doc下载后提示损坏，文本文件下载后通过编辑器发现文件开头多了几个乱码字符，初步判定是服务端数据流多发送了一个信息，而客户端没有接收，导致这部分信息存到了文件的开头。

  解决办法：检查了代码逻辑之后发现果然是多发送了数据，服务端收到客户端发来的下载指令后向客户端发送了一次文件长度信息，然后进入 sendFile 方法内又发送了一遍文件长度信息，而客户端只接受了一次，导致每个文件接收时都会在开头多8个字节。删除一个多余的发送即可。

* 其他小问题，例如不会sql语句，以及一些 swing 控件的使用方法，都通过百度自行学习。

### 3.2 对设计和编码的回顾分析

设计方面，其实还有很大可以改进的地方，有些方面实现的还是不够优美，例如读写数据库时重复的连接，其实可以封装到一个静态类中实现。再比如文件发送有 sendFile 函数，而文件接收却是写在主代码中没有封装成函数，不太规整，可读性也不太好。

编码方面，一个是文件的命名方式，或许可以用哈希后缀，来实现文件名的缩短，没有仔细想可行性。还有一个就是字符集编码问题，为了统一编码我把所有环境都设置为了 UTF-8 编码，避免了一些乱码问题。

### 3.3 程序运行的时空效率分析

本程序没有用到什么算法优化，所以时间效率和空间效率应该都是$O(n)$的。

### 3.4 运行实例

![](mypan.png)

### 3.5 改进设想

有些方面实现的还是不够优美，例如读写数据库时重复的连接，其实可以封装到一个静态类中实现。再比如文件发送有 sendFile 函数，而文件接收却是写在主代码中没有封装成函数，不太规整，可读性也不太好。

### 3.6 经验和体会

要想写出好的程序，用户体验是一方面，自己的代码规范也是一方面。这次时间还是比较仓促，代码感觉还有可以完善优美的地方，为了便于以后维护更新，代码一定要规整。

## 四、使用说明

服务器端：

1. 保证MySQL运行在3306端口，建立数据库`pan`，建立数据表`files`和`users`。
2. 表中需要建立的字段见本报告 **2.1.2 数据结构** 部分
3. 运行 Server 类即可。

客户端

1. 运行 Client 类即可。

### Q&A

Q：如何上传？
A：点击文件列表窗体中的`上传`按钮，选择文件即可。

Q：如何下载？
A：先选中要下载的文件，再点击`下载选中文件`按钮，选择要保存到的目录即可。

Q：如何删除？
A：先选中要删除的文件，再点击`删除`按钮即可。
