package kat.recsystem;

public interface CreateQuery {


    public void sendQueryToServer(int id, double latitude, double longitude, int k, String cat);

}
