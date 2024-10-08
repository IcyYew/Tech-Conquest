package Websockets;

import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import org.apache.logging.log4j.message.Message;

import java.util.Date;

@Entity
@Table(name = "clan-messages")
public class ClanChatMessage {
    @Id
    @GeneratedValue
    @Column(name ="clan-message-id")
    private long id;

    @Column
    private String userName;

    @Lob
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time-sent")
    private Date sent = new Date();

    public ClanChatMessage() {
    }

    public ClanChatMessage(String userName, String content) {

        this.userName = userName;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }
}
