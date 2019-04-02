package kat.recsystem;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class AndroidClient extends Thread implements Serializable{

    int id, k;
    double latitude, longitude;
    String cat;
    static List <Poi> pois=new ArrayList<>();
     int i;
    static Poi poi;

    public AndroidClient(int id, double latitude, double longitude, int k, String cat){
        this.id=id;
        this.latitude=latitude;
        this.longitude=longitude;
        this.k=k;
        this.cat=cat;
    }

    public void initializeAndroidClient() {
        Socket requestSocket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {

            /* Create socket for contacting the server on port 4321*/

            requestSocket = new Socket("192.168.43.179",4200);
            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());



            out.reset();
            out.writeInt(id);
            out.flush();
            out.reset();
            out.writeDouble(latitude);
            out.flush();
            out.reset();
            out.writeDouble(longitude);
            out.flush();
            out.reset();
            out.writeInt(k);
            out.flush();
            out.reset();
            out.writeObject(cat);
            out.flush();
            out.reset();


            i = in.readInt();




            for(int k=0; k<i; k++){
                poi=new Poi(in.readInt(),(String) in.readObject(),in.readDouble(),in.readDouble(),(String) in.readObject(),(String) in.readObject(),(String) in.readObject());
                pois.add(k,poi);
            }




        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();	out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}
