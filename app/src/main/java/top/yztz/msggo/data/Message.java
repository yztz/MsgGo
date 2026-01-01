package top.yztz.msggo.data;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // 用于版本控制

    private String phone;
    private String content;

    public Message(String phone, String content) {
        this.phone = phone;
        this.content = content;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{phone='" + phone + "', content='" + content + "'}";
    }
}
