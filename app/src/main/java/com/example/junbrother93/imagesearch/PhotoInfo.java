package com.example.junbrother93.imagesearch;

public class PhotoInfo {
    String farm_id="";
    String server_id="";
    String id="";
    String secret="";

    public String getFarm_id(){
        return farm_id;
    }
    public String getServer_id(){
        return server_id;
    }
    public String getId(){
        return id;
    }
    public String getSecret(){
        return secret;
    }

    public void setFarm_id(String f)
    {
        farm_id = f;
    }
    public void setServer_id(String s)
    {
        server_id = s;
    }
    public void setId(String i)
    {
        id = i;
    }
    public void setSecret(String sec)
    {
        secret = sec;
    }



}
