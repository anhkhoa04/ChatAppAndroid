package edu.csc.chatapp.Model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String name;
    private String imageURL;
    private String status;

    public User(String id, String name, String imageURL, String status) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.status = status;
    }

    // must have constructor no parameter
    public User(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id",this.id);
        map.put("name",this.name);
        map.put("image", this.imageURL);
        return map;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageURL='" + imageURL + '\'' +
                '}';
    }
}
