import com.sun.imageio.plugins.jpeg.JPEG;

import java.io.*;

public class Poi implements Serializable{

    private  int columnnumber;
    private String name, category,id;
    private double latitude, longitude;
    private String image;

    public Poi(){

    }


    public Poi(int column, String id, double latitude, double longitude, String image, String category, String name) {
        this.columnnumber=column;

        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.image=image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {

        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImage() {
        return image;
    }

    public int getColumnnumber() {
        return columnnumber;
    }

    public void setColumnnumber(int columnnumber) {
        this.columnnumber = columnnumber;
    }



    @Override
    public String toString() {
        return "Poi{" +
                "column_number"+columnnumber+
                ", id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", photos="+image+
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                '}';
    }


}
