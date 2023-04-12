package sendfile.client;



import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hThao
 */
public class ReceivingFileThread implements Runnable {
    
    protected Socket socket;//kết nối đến máy chủ
    protected DataInputStream dis;// đối tượng đọc dữ liệu từ socket
    protected DataOutputStream dos;// đối tượng ghi vào dữ liệu từ socket
    protected MainForm main;//hàm main
    protected StringTokenizer st;//phân tích cú pháp của dữ liệu đọc được từ socket 
    protected DecimalFormat df = new DecimalFormat("##,#00");//định dạng đối tượng 
    private final int BUFFER_SIZE = 100;//kích thước mảng byte xử lý dữ liệu gửi từ socket
    //Khởi tạo các đối tượng DataInput và DataOutput để gửi và nhận dữ liệu và gửi dữ liệu từ client và server
    public ReceivingFileThread(Socket soc, MainForm m){ //nhận 2 đối tượng là socket và main form
        this.socket = soc;
        this.main = m;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " +e.getMessage());
        }
    }

    @Override
    public void run() {
        try {//tạo 1 vòng lặp vô hạn để đọc xử lý dữ liệu từ socket
            while(!Thread.currentThread().isInterrupted()){
                String data = dis.readUTF();//sử dụng dis để đọc dữ liệu từ socket
                st = new StringTokenizer(data);//phân tích cú pháp của dữ liệu đọc được từ socket
                String CMD = st.nextToken();
                
                switch(CMD){//xác định loại dữ liệu nhận được từ socket
                    
                    //   hàm này sẽ xử lý việc nhận một file trong một tiến trình nền xử lý từ một user khác
                    case "CMD_SENDFILE"://nếu nhận được dữ liệu là gửi file thì thực hiện các bước nhận file và lưu vào máy tính
                        String consignee = null;
                            try {
                                String filename = st.nextToken();
                                int filesize = Integer.parseInt(st.nextToken());
                                consignee = st.nextToken(); // lấy tên của người gửi kích thước của file 
                                main.setMyTitle("Đang tải File....");
                                System.out.println("Đang tải File....");
                                System.out.println("From: "+ consignee);
                                String path = main.getMyDownloadFolder() + filename;                                
                                /*  tạo 1 luồng mới bắt đầu quá trình tải file    */
                                FileOutputStream fos = new FileOutputStream(path);
                                InputStream input = socket.getInputStream();                                
                                /*  theo dõi tiến độ tải file và hiển thị nó trên main form   */
                                ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(main, "Downloading file please wait...", input);
                                /*  Buffer   */
                                BufferedInputStream bis = new BufferedInputStream(pmis);
                                /*  tạo 1 tệp tin tạm thời để chứa nó */
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count, percent = 0;
                                while((count = bis.read(buffer)) != -1){
                                    percent = percent + count;
                                    int p = (percent / filesize);
                                    main.setMyTitle("Downloading File  "+ p +"%");
                                    fos.write(buffer, 0, count);
                                }
                                fos.flush();
                                fos.close();
                                main.setMyTitle("you are logged in as: " + main.getMyUsername());
                                JOptionPane.showMessageDialog(null, "File đã được tải đến \n'"+ path +"'");
                                System.out.println("File đã được lưu: "+ path);
                            } catch (IOException e) {
                                /*
                                Gửi lại thông báo lỗi đến sender
                                Định dạng: CMD_SENDFILERESPONSE [username] [Message]
                                */
                                DataOutputStream eDos = new DataOutputStream(socket.getOutputStream());
                                eDos.writeUTF("CMD_SENDFILERESPONSE "+ consignee + " Kết nối bị mất, vui lòng thử lại lần nữa.!");
                                
                                System.out.println(e.getMessage());
                                main.setMyTitle("bạn đã được đăng nhập với tên: " + main.getMyUsername());
                                JOptionPane.showMessageDialog(main, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                                socket.close();
                            }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " +e.getMessage());
        }
    }
}

