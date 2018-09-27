package domain;


public class Favourite {
    private String favouriteId;
    private String lodgingId;
    private String userId;

    public Favourite() {
    }

    public String getFavouriteId() {
        return favouriteId;
    }

    public void setFavouriteId(String favouriteId) {
        this.favouriteId = favouriteId;
    }

    public String getLodgingId() {
        return lodgingId;
    }

    public void setLodgingId(String lodgingId) {
        this.lodgingId = lodgingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
