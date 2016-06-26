package exo.mbariah.sharemylocation.Chat;


public class ChatMessage {
    private boolean isImage, isMine;
    private String content, date, link;

    public ChatMessage(String message, String url, String time, boolean mine, boolean image) {
        content = message;
        link = url;
        isMine = mine;
        isImage = image;
        date = time;

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }
}
