//Edward Davidson
//S1604249

package com.example.mobilecw;

import android.os.Parcel;
import android.os.Parcelable;

public class RoadData implements Parcelable {

    private String title;
    private String description;
    private String link;
    private String georssPoint;
    private String author;
    private String comments;
    private String pubDate;

    public RoadData()
    {

    }

    public RoadData(String title, String description, String link, String georssPoint, String author, String comments, String pubDate)
    {
        this.title = title;
        this.description = description;
        this.link = link;
        this.georssPoint = georssPoint;
        this.author = author;
        this.comments = comments;
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getGeorssPoint() {
        return georssPoint;
    }

    public String getLink() {
        return link;
    }

    public String getAuthor() {
        return author;
    }

    public String getComments() {
        return comments;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGeorssPoint(String georssPoint) {
        this.georssPoint = georssPoint;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setAuthor(String author) { this.author = author; }

    public void setComments(String comments) { this.comments = comments; }

    private RoadData(Parcel in) {
        // This order must match the order in writeToParcel()
        title = in.readString();
        description = in.readString();
        link = in.readString();
        georssPoint = in.readString();
        author = in.readString();
        comments = in.readString();
        pubDate = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(description);
        out.writeString(link);
        out.writeString(georssPoint);
        out.writeString(author);
        out.writeString(comments);
        out.writeString(pubDate);

    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RoadData> CREATOR = new Parcelable.Creator<RoadData>() {
        public RoadData createFromParcel(Parcel in) {
            return new RoadData(in);
        }

        public RoadData[] newArray(int size) {
            return new RoadData[size];
        }
    };
}

