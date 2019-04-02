import com.sun.imageio.plugins.jpeg.JPEG;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.awt.peer.SystemTrayPeer;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;


import java.io.*;
import java.net.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


//na kanoume reed auta p 8a mas steiloun autoi
//na kanoume write tin lista pois
//


//na rwthsoume gia thn calculateError an prepei na epistrefei timh h pinaka me vash to paper
//se poio apo to 2 Xu h Yi 8elei transpose?
//sthn calculateScore an prepei na polla/soume 2 pinakes h 2 vectors kai an polla/soume 2 vectors se poio 8elei transpose

public class Master extends Thread implements MasterInterface, Serializable {
    //Gia BestLocalPois
    static int range = 5000;
    static int user = 1;
    static double latitude = 40.75458413923;
    static double longitude = -74.214440350544;
    static String category = "Food";

    //Gia posous workers
    static int MaxNumOfWorkers = 3;

    //gia lamda
    static double lamda = 0.1;
    static int numofTopPois = 5;

    //poses epanalhpseis
    public static int threashold = 2;

    //Alles arxikopoihseis
    static int numofstarted = 0;
    static int numofworkers = 0;
    public static int[] counter = new int[MaxNumOfWorkers];
    public static int replay = 0;
    public static boolean[] calcDone = new boolean[MaxNumOfWorkers];
    public static RealMatrix original;
    public static RealMatrix cmatrix;
    public static RealMatrix pmatrix;
    public static RealMatrix xmatrix;
    public static RealMatrix ymatrix;
    public static double result_of_last_costfunction = 0;
    public static double calcerror = 1;
    public static double[] percentages;
    public static Properties[] proparray;
    static Thread[] workers;
    static SentTables[] sTables, sTablesY;
    public static ArrayList<Poi> pois = new ArrayList<Poi>();
    static Master ms;
    static int counterFor = 0;

    public static void main(String args[]) {
        ms = new Master();
        ms.poireader();


        sTables = new SentTables[MaxNumOfWorkers];
        sTablesY = new SentTables[MaxNumOfWorkers];
        proparray = new Properties[Master.MaxNumOfWorkers];
        percentages = new double[Master.MaxNumOfWorkers];
        readZeroM();
        int K = 20;
        ms.calculateCMatrix(40, original);
        ms.calculatePMatrix(original);
        ms.createXMatrix(original.getRowDimension(), K, original);
        ms.createYMatrix(original.getColumnDimension(), K, original);
        //  System.out.println(ms.calculateScore(1, 10)); //hthela na testarw na dw an trexei swsta

        ms.initialize();
        System.out.println("Waiting for Android Connections");
        ms.initializeAndoidClient();

/*
        List<Poi> PoiRecomendationResult = ms.calculateBestLocalPoisForUser(user, latitude, longitude, numofTopPois, category);
        for (int i = 0; i < PoiRecomendationResult.size(); i++) {
            System.out.println(PoiRecomendationResult.get(i).toString());

        }*/


        // System.exit(0); //mporei na mhn xreiastei

    }


    ServerSocket providerSocket;
    Socket connection = null;

    ///objects used for sockets
    public synchronized void initialize() {
        //to run tou Server
        workers = new Thread[MaxNumOfWorkers];


        try {
            providerSocket = new ServerSocket(4200, 10);//to 10 o megistos ari8mos connections

            while (numofworkers < MaxNumOfWorkers) {

                connection = providerSocket.accept();
                System.out.println("Got a new connection...");
                workers[numofworkers] = new ActionsForWorkers(connection, numofworkers);
                numofworkers++;

            }


            for (int i = 0; i < MaxNumOfWorkers; i++) {
                workers[i].start();

            }


            while (numofstarted < MaxNumOfWorkers) {
                Thread.sleep(200);
            }


            int totalProc = 0;
            long totalMem = 0;
            for (int i = 0; i < numofworkers; i++) {

                totalMem += proparray[i].getRam();
                totalProc += proparray[i].getProc();

            }

            long x;
            int y;
            for (int i = 0; i < numofworkers; i++) {

                percentages[i] = (((double) proparray[i].getRam() / (double) totalMem) + ((double) proparray[i].getProc() / (double) totalProc)) / 2;


            }
            System.out.println("Eimai sthn epanalhpsh i: " + (0) + "\n" + "Epituxeis ypologismos twn posostwn gia workers.");

            counterFor++;
            int i = 0;

            while (i < threashold && calcerror >= lamda) {
                System.out.println("Eimai sthn epanalhpsh i: " + (i + 1));

                for (int k = 0; k < MaxNumOfWorkers; k++) {
                    connection = providerSocket.accept();
                    System.out.println("Got a new connection...");
                    workers[k] = new ActionsForWorkers(connection, numofworkers);

                }
                for (int j = 0; j < MaxNumOfWorkers; j++) {
                    workers[j].start();

                }
                while (replay < MaxNumOfWorkers) {
                    Thread.sleep(200);

                }

                replay = 0;
                calcerror = calculateError();
                System.out.println("Calculate Error: " + calcerror);

                Thread.sleep(200);


                counterFor++;
                i++;
            }


        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException e) {

        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }

    }


    public void initializeAndoidClient() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            providerSocket = new ServerSocket(4200, 10);//to 10 o megistos ari8mos connections

            while (true) {
                connection = providerSocket.accept();
                System.out.println("Got a new connection from Android...");

                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());


                int user_id = in.readInt();
                double user_latitude = in.readDouble();
                double user_longitude = in.readDouble();
                int user_numofpois = in.readInt();

                String user_categoty = (String) in.readObject();
                System.out.println("Id " + user_id + " Lat " + user_latitude + " Long " + user_longitude + " Num " + user_numofpois + " cat " + user_categoty);
                List<Poi> p = calculateBestLocalPoisForUser(user_id, user_latitude, user_longitude, user_numofpois, user_categoty);
                System.out.println(p.size());
                out.reset();
                out.writeInt(p.size());
                out.flush();
                //Poi poiz = new Poi(4, "user", 40.01, 70.04, "image", "Food", "Poi name");
                // System.out.println(poiz.toString()+ poiz.getClass());
                //out.reset();
                //out.writeObject(poiz);
                // out.flush();
                // poiz=(Poi) in.readObject();


                for (int j = 0; j < p.size(); j++) {
                    System.out.println(p.get(j).toString());
                    out.reset();
                    out.writeInt(p.get(j).getColumnnumber());
                    out.flush();
                    out.reset();
                    out.writeObject(p.get(j).getId());
                    out.flush();
                    out.reset();
                    out.writeDouble(p.get(j).getLatitude());
                    out.flush();
                    out.reset();
                    out.writeDouble(p.get(j).getLongitude());
                    out.flush();
                    out.reset();
                    out.writeObject(p.get(j).getImage());
                    out.flush();
                    out.reset();
                    out.writeObject(p.get(j).getCategory());
                    out.flush();
                    out.reset();
                    out.writeObject(p.get(j).getName());
                    out.flush();
                }

                //out.writeObject(calculateBestLocalPoisForUser(user_id, user_latitude, user_longitude, user_numofpois, user_categoty));
                out.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }


        }
    }

    static private Scanner x;

    public static void readZeroM() {

        try {

            x = new Scanner(new File("src\\main\\java\\input_matrix_non_zeros.csv"));

            RealMatrix zeroMatrix = MatrixUtils.createRealMatrix(835, 1692);


            while (x.hasNext()) {
                String data = x.nextLine();
                String[] values = data.trim().split(", ");
                zeroMatrix.setEntry(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));

            }
            x.close();

            original = zeroMatrix;
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file");
        }


    }


    public void calculateCMatrix(int a, RealMatrix realm) {

        RealMatrix CMatrix = MatrixUtils.createRealMatrix(realm.getRowDimension(), realm.getColumnDimension());
        for (int i = 0; i < CMatrix.getRowDimension(); i++) {
            for (int j = 0; j < CMatrix.getColumnDimension(); j++) {

                CMatrix.setEntry(i, j, 1 + a * realm.getEntry(i, j));

            }
        }
        cmatrix = CMatrix;
    }


    public void calculatePMatrix(RealMatrix realm) {

        RealMatrix PMatrix = MatrixUtils.createRealMatrix(realm.getRowDimension(), realm.getColumnDimension());
        for (int i = 0; i < PMatrix.getRowDimension(); i++) {
            for (int j = 0; j < PMatrix.getColumnDimension(); j++) {
                if (realm.getEntry(i, j) > 0) {
                    PMatrix.setEntry(i, j, 1);
                }
            }

        }
        pmatrix = PMatrix;

    }

    public void createXMatrix(int M, int K, RealMatrix realm) {
        int ran = 0;
        RealMatrix XMatrix = MatrixUtils.createRealMatrix(M, K);
        RandomGenerator randomGenerator = new JDKRandomGenerator();
        randomGenerator.setSeed(1);
        for (int i = 0; i < XMatrix.getRowDimension(); i++) {
            for (int j = 0; j < XMatrix.getColumnDimension(); j++) {


                ran = randomGenerator.nextInt();
                if (ran > 0) {
                    ran = 1;
                } else {
                    ran = 0;
                }

                XMatrix.setEntry(i, j, ran);

            }

        }
        xmatrix = XMatrix;
        for (int i = 0; i < MaxNumOfWorkers; i++) {
            sTables[i] = new SentTables(null, null, null, null, 0, 0, 100);
        }
    }


    static public SentTables distributeXMatrixToWorkers(int M) {

        int start, end;
        start = 0;
        end = (int) ((double) percentages[0] * (double) xmatrix.getRowDimension());


        sTables[0].setP(pmatrix);
        sTables[0].setC(cmatrix);
        sTables[0].setX(xmatrix);
        sTables[0].setY(ymatrix);
        sTables[0].setStart(start);
        sTables[0].setEnd(end);

        for (int i = 1; i < numofworkers - 1; i++) {
            start = end + 1;
            end = end + (int) ((double) percentages[i] * (double) xmatrix.getRowDimension());
            sTables[i].setP(pmatrix);
            sTables[i].setC(cmatrix);
            sTables[i].setX(xmatrix);
            sTables[i].setY(ymatrix);
            sTables[i].setStart(start);
            sTables[i].setEnd(end);

        }
        start = end + 1;
        end = xmatrix.getRowDimension() - 1;
        sTables[numofworkers - 1].setP(pmatrix);
        sTables[numofworkers - 1].setC(cmatrix);
        sTables[numofworkers - 1].setX(xmatrix);
        sTables[numofworkers - 1].setY(ymatrix);
        sTables[numofworkers - 1].setStart(start);
        sTables[numofworkers - 1].setEnd(end);
        System.out.println("O distribute X trexei epityxws");
        return sTables[M];
    }


    public void createYMatrix(int N, int K, RealMatrix realm) {
        int ran = 0;
        RealMatrix YMatrix = MatrixUtils.createRealMatrix(N, K);
        RandomGenerator randomGenerator = new JDKRandomGenerator();
        randomGenerator.setSeed(1);
        for (int i = 0; i < YMatrix.getRowDimension(); i++) {
            for (int j = 0; j < YMatrix.getColumnDimension(); j++) {


                ran = randomGenerator.nextInt();
                if (ran > 0) {
                    ran = 1;
                } else {
                    ran = 0;
                }
                YMatrix.setEntry(i, j, ran);

            }

        }
        ymatrix = YMatrix;
        for (int i = 0; i < MaxNumOfWorkers; i++) {
            sTablesY[i] = new SentTables(null, null, null, null, 0, 0, 100);
        }
    }


    static public SentTables distributeYMatrixToWorkers(int K) {
        int start, end;

        start = 0;
        end = (int) ((double) percentages[0] * (double) ymatrix.getRowDimension());

        sTablesY[0].setP(pmatrix);
        sTablesY[0].setC(cmatrix);
        sTablesY[0].setX(xmatrix);
        sTablesY[0].setY(ymatrix);
        sTablesY[0].setStart(start);
        sTablesY[0].setEnd(end);
        for (int i = 1; i < numofworkers - 1; i++) {
            start = end + 1;
            end = end + (int) ((double) percentages[i] * (double) ymatrix.getRowDimension());
            sTablesY[i].setP(pmatrix);
            sTablesY[i].setC(cmatrix);
            sTablesY[i].setX(xmatrix);
            sTablesY[i].setY(ymatrix);
            sTablesY[i].setStart(start);
            sTablesY[i].setEnd(end);

        }
        start = end + 1;
        end = ymatrix.getRowDimension() - 1;
        sTablesY[numofworkers - 1].setP(pmatrix);
        sTablesY[numofworkers - 1].setC(cmatrix);
        sTablesY[numofworkers - 1].setX(xmatrix);
        sTablesY[numofworkers - 1].setY(ymatrix);
        sTablesY[numofworkers - 1].setStart(start);
        sTablesY[numofworkers - 1].setEnd(end);
        System.out.println("o distribute Y trexei epityxws");

        return sTablesY[K];
    }


    public static double calculateError() throws InterruptedException {

        double sum = 0.0;
        double sumx = 0.0;
        double sumy = 0.0;
        double result = 0.0;
        for (int u = 0; u < xmatrix.getRowDimension(); u++) {
            for (int i = 0; i < ymatrix.getRowDimension(); i++) {

                sum += cmatrix.getEntry(u, i) *
                        (pmatrix.getEntry(u, i) - (xmatrix.getRowVector(u).dotProduct(ymatrix.transpose().getColumnVector(i)))) *
                        (pmatrix.getEntry(u, i) - (xmatrix.getRowVector(u).dotProduct(ymatrix.transpose().getColumnVector(i))));


            }
        }


        for (int u = 0; u < xmatrix.getRowDimension(); u++) {
            for (int i = 0; i < xmatrix.getColumnDimension(); i++) {
                sumx += xmatrix.getEntry(u, i) * xmatrix.getEntry(u, i);
            }
        }
        for (int u = 0; u < ymatrix.getRowDimension(); u++) {
            for (int i = 0; i < ymatrix.getColumnDimension(); i++) {
                sumx += ymatrix.getEntry(u, i) * ymatrix.getEntry(u, i);
            }
        }
        result = sum + lamda * (sumx + sumy);


        if (result_of_last_costfunction == 0) {//an einai h prwth fora
            result_of_last_costfunction = result;
            return result;
        }

        sumx = result_of_last_costfunction;
        result_of_last_costfunction = result;


        return sumx - result;

    }


    public double calculateScore(int user, int poi) {


        return xmatrix.transpose().getColumnVector(user).dotProduct(ymatrix.getRowVector(poi));
    }


    public void poireader() {
        Poi p;
        try {

            FileReader reader = new FileReader("src\\main\\java\\POIs.json");
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(reader);

            JSONObject ele;
            for (int i = 0; (ele = (JSONObject) obj.get(Integer.toString(i))) != null; i++) {
                p = new Poi();
                p.setColumnnumber(i);

                p.setId((String) ele.get("POI"));

                p.setLatitude((Double) ele.get("latidude"));

                p.setLongitude((Double) ele.get("longitude"));

                p.setImage((String) ele.get("photos"));
                p.setCategory((String) ele.get("POI_category_id"));
                p.setName((String) ele.get("POI_name"));
                pois.add(p);


            }
            System.out.println("Pois was read succesfully!");


        } catch (FileNotFoundException e) {
            System.out.println("Could not find file");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public List<Poi> calculateBestLocalPoisForUser(int user, double latitude, double longitude, int numofpois, String category) {

        List<Poi> allp = new ArrayList<Poi>();
        RealMatrix predic = xmatrix.multiply(ymatrix.transpose());
        double[] poiPred = new double[predic.getColumnDimension()];
        // System.out.println(poiPred.length);

        for (int i = 0; i < poiPred.length; i++) {
            poiPred[i] = predic.getEntry(user, i);

        }
        //taksinomw me vash th vatmologia
        double temp = 0.0;
        for (int i = 0; i < predic.getColumnDimension(); i++) {
            for (int j = 1; j < (predic.getColumnDimension() - i); j++) {
                if (poiPred[j - 1] < poiPred[j]) {
                    temp = poiPred[j - 1];
                    poiPred[j - 1] = poiPred[j];
                    poiPred[j] = temp;

                }

            }
        }
        int k = 0;
        //bazw sth lista ta poio kontina
        for (int i = 0; i < poiPred.length; i++) {
            if (k < numofpois) {

                if (pmatrix.getEntry(user, i) == 1) {

                } else {
                    if (category.equals(("Everything"))) {
                        if (range > distanceBetweenLocations(latitude, longitude, pois.get(i).getLatitude(), pois.get(i).getLongitude())) {

                            allp.add(pois.get(i));
                            k++;
                        }
                    } else {
                        if (range > distanceBetweenLocations(latitude, longitude, pois.get(i).getLatitude(), pois.get(i).getLongitude()) && pois.get(i).getCategory().equals(category)) {

                            allp.add(pois.get(i));
                            k++;
                        }
                    }
                }


            } else {
                break;
            }

        }

        return allp;

    }

    public double distanceBetweenLocations(double latUser, double lonUser, double latPoi, double lonPoi) {

        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(latPoi - latUser);
        double lonDistance = Math.toRadians(lonPoi - lonUser);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latUser)) * Math.cos(Math.toRadians(latPoi))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.round(distance);

        return distance;
    }


}