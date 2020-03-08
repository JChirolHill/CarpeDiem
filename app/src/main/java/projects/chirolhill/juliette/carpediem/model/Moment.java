package projects.chirolhill.juliette.carpediem.model;

import android.graphics.Bitmap;

public class Moment {
    private String title;
    private String date;
    private Bitmap img;

    public Moment(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
