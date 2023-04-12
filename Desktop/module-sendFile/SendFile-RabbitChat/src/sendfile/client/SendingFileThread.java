package sendfile.client;


import sendfile.client.SendFile;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hThao
 */
public class SendingFileThread implements Runnable {
    
    protected Socket socket;//để kết nối tới server hoặc đối tác để gửi file
    private DataOutputStream dos;//đối tượng để ghi kết nối vào UOTPUT stream
    protected SendFile form;//cập nhật giao diện
    protected String file;//lưu đường dẫn
    protected String receiver;//tên nhận file
    protected String sender;//tên người gửi
    protected DecimalFormat df = new DecimalFormat("##,#00");//định dạng số liệu hiển thị trên giao diện
    private final int BUFFER_SIZE = 100;//kích thước sử dụng để gửi file là 100 byte
    //khởi tạo các biến
    public SendingFileThread(Socket soc, String file, String receiver, String sender, SendFile frm){
        this.socket = soc;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
        this.form = frm;
    }

    @Override
    public void run() {
        try {
            form.disableGUI(true);
            System.out.println("Gửi File..!");
            dos = new DataOutputStream(socket.getOutputStream());//tạo đối tượng dos để ghi dữ liệu vào uotputstream
            /** Write filename, recipient, username  **/
            File filename = new File(file);
            int len = (int) filename.length();//lấy kích thước của file
            int filesize = (int)Math.ceil(len / BUFFER_SIZE);//tính kích thước của file theo buffer đã được định sẵn
            String clean_filename = filename.getName();//lấy tên file
            dos.writeUTF("CMD_SENDFILE "+ clean_filename.replace(" ", "_") +" "+ filesize +" "+ receiver +" "+ sender);//ghi thông tin của file vào người nhận vào uotputstream
            System.out.println("Từ: "+ sender);
            System.out.println("Đến: "+ receiver);
            /** Create an stream **/
            InputStream input = new FileInputStream(filename);//tạo đối tượng input để đọc dữ liệu từ file
            OutputStream output = socket.getOutputStream();//tạo đối tượng uot put để đọc dữ liệu từ socket
 
            BufferedInputStream bis = new BufferedInputStream(input);//tạo buffer để đọc dữ liệu từ inputstream
            byte[] buffer = new byte[BUFFER_SIZE];
            int count, percent = 0;
            while((count = bis.read(buffer)) > 0){
                percent = percent + count;
                int p = (percent / filesize);
               
                form.updateProgress(p);
                output.write(buffer, 0, count);
            }
            /* Cập nhật AttachmentForm GUI */
            form.setMyTitle("File đã được gửi đi.!");
            form.updateAttachment(false); //  Cập nhật Attachment 
            JOptionPane.showMessageDialog(form, "File đã gửi thành công.!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            form.closeThis();
            /* Đóng gửi file */
            output.flush();
            output.close();
            System.out.println("File đã được gửi..!");
        } catch (IOException e) {
            form.updateAttachment(false); //  Cập nhật Attachment
            System.out.println("[SendFile]: "+ e.getMessage());
        }
    }
}