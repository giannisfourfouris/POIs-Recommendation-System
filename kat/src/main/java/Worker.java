import java.io.*;
import java.net.*;

import org.apache.commons.math3.linear.*;

public class Worker extends Thread implements WorkerInterface {
    //epanalhpseis kai lamda
    double lamda = 0.1;
    int threshold = 2;

    //alles arxikopoiseis
    int counterFor = 0;
    int idnum;
    RealMatrix cmatrix, pmatrix, xmatrix, ymatrix, Cu, Ci;
    Properties p;
    SentTables tab;

    Worker(Properties prop) {
        p = new Properties(prop.getRam(), prop.getProc());

    }

    public static void main(String args[]) throws UnknownHostException {
        String local = Inet4Address.getLocalHost().getHostAddress();
        long maxMemory = Runtime.getRuntime().maxMemory();
        int processors = Runtime.getRuntime().availableProcessors();
        Properties p = new Properties(maxMemory, processors);

        new Worker(p).start();

    }

    public void run() {
        initialize();

    }

    public void initialize() {
        ///to run to client
        Socket requestSocket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        for (int k = 0; k < threshold + 1; k++) {

            try {

                requestSocket = new Socket("localhost", 4200);
                /* Create the streams to send and receive data from server */
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                /* Write the two integers */
                if (counterFor == 0) {
                    out.writeObject(p);
                    out.flush();


                    /* Print the received result from server */
                    System.out.println("Worker: > " + in.readLong() + " RAM " + in.readInt() + " CPU nodes");
                    System.out.println("Eimai sthn epanalhpsh i: "+k+" Exw steilei epityxws ram kai cores sto master");

                    idnum = in.readInt();
                    counterFor += 1;
                } else {
                    try {
                        System.out.println("Eimai sthn epanalhpsh i: "+k);

                        out.writeInt(idnum);
                        out.flush();
                        tab = (SentTables) in.readObject();
                        if (tab.getCurrentI()==-1){
                            break;

                        }
                        System.out.println("Dimensions: "+tab.start + " " + tab.end);
                        xmatrix = tab.getX();
                        cmatrix = tab.getC();
                        pmatrix = tab.getP();
                        ymatrix = tab.getY();


                        for (int i = tab.start; i <= tab.end; i++) {
                            calculate_x_u(i, ymatrix, pmatrix);
                        }
                        tab.setX(xmatrix);
                        out.writeObject(tab);
                        out.flush();



                        xmatrix = null;
                        cmatrix = null;
                        pmatrix = null;
                        ymatrix = null;


                        tab = (SentTables) in.readObject();
                        if (tab.getCurrentI()==-1){
                            break;

                        }
                        System.out.println("Dimensions: "+tab.start + " " + tab.end);
                        xmatrix = tab.getX();
                        cmatrix = tab.getC();
                        pmatrix = tab.getP();
                        ymatrix = tab.getY();

                        for (int i = tab.start; i <= tab.end; i++) {
                            calculate_y_i(i, xmatrix, pmatrix);
                        }

                        tab.setY(ymatrix);
                        out.writeObject(tab);
                        out.flush();
                        xmatrix = null;
                        cmatrix = null;
                        pmatrix = null;
                        ymatrix = null;


                        counterFor += 1;

                    } catch (ClassNotFoundException e) {
                        System.out.println("Class not found");
                    }
                }


            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            finally {
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
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


    public void calculateCuMatrix(int u, RealMatrix realc) {
        Cu = MatrixUtils.createRealMatrix(ymatrix.getRowDimension(), ymatrix.getRowDimension());
        for (int i = 0; i < ymatrix.getRowDimension(); i++) {
            Cu.setEntry(i, i, realc.getEntry(u, i));
        }
        this.Cu = Cu;

    }


    public void calculateCiMatrix(int i, RealMatrix realc) {
        Ci = MatrixUtils.createRealMatrix(xmatrix.getRowDimension(), xmatrix.getRowDimension());
        for (int u = 0; u < xmatrix.getRowDimension(); u++) {
            Ci.setEntry(u, u, realc.getEntry(u, i));
        }
        this.Ci = Ci;
    }

    public RealMatrix preCalculateXX(RealMatrix realx) {
        return realx.transpose().multiply(realx);
    }

    public RealMatrix preCalculateYY(RealMatrix realy) {
        return realy.transpose().multiply(realy);
    }

    public RealMatrix calculate_x_u(int u, RealMatrix realy, RealMatrix realp) {
        calculateCuMatrix(u, cmatrix);
        RealMatrix result, part1, identitymat, substraction;
        identitymat = MatrixUtils.createRealIdentityMatrix(Cu.getColumnDimension()); //monadiaios

        substraction = Cu.subtract(identitymat);

        part1 = preCalculateYY(realy).add(realy.transpose().multiply(substraction).multiply(realy));
        identitymat = MatrixUtils.createRealIdentityMatrix(realy.getColumnDimension());
        result = (MatrixUtils.inverse(part1.add(identitymat.scalarMultiply(lamda))).multiply(realy.transpose()).multiply(Cu).multiply(realp.getRowMatrix(u).transpose()));
        //pernoume to result ston X orizontia kai oxi ka8eta
        for (int i = 0; i < xmatrix.getColumnDimension(); i++) {
            xmatrix.setEntry(u, i, result.getEntry(i, 0));
        }
        ///einai enas tupos pou to exei sto cf gia to Xu
        return result;


    }

    public RealMatrix calculate_y_i(int i, RealMatrix realx, RealMatrix realp) {

        calculateCiMatrix(i, cmatrix);
        RealMatrix result, part1, identitymat, substraction;
        identitymat = MatrixUtils.createRealIdentityMatrix(Ci.getColumnDimension()); //monadiaios
        substraction = Ci.subtract(identitymat);

        part1 = preCalculateXX(realx).add(realx.transpose().multiply(substraction).multiply(realx));
        identitymat = MatrixUtils.createRealIdentityMatrix(realx.getColumnDimension());
        result = (MatrixUtils.inverse(part1.add(identitymat.scalarMultiply(lamda))).multiply(realx.transpose()).multiply(Ci).multiply(realp.getColumnMatrix(i)));
        //pernoume to result ston Y orizontia kai oxi ka8eta
        for (int j = 0; j < ymatrix.getColumnDimension(); j++) {
            ymatrix.setEntry(i, j, result.getEntry(j, 0));
        }
        ///einai enas tupos pou to exei sto cf gia to Yi
        return result;

    }



}
