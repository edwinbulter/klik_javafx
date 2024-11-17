package eb.javafx.klik.model;

public record UserCounter (String userId, int clickCount, String updatedAt) {

    public String getUserId() {
        return this.userId;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

}
