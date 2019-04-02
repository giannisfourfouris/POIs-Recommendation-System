import java.io.*;
import java.lang.reflect.Array;
import java.net.*;

public class ActionsForWorkers extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    String ip;
    int idnum;
    int k=1;
    double p=0.0001;
     double v=0.0001;


    public ActionsForWorkers(Socket connection, int id) {
        try {
            idnum = id;
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public synchronized void run() {

        try {

            Properties a = null;
            try {
                if (Master.counterFor == 0) {

                    a = (Properties) in.readObject();
                    Master.proparray[idnum] = new Properties(a.getRam(), a.getProc());

                    Master.numofstarted++;
                    out.writeLong(a.getRam());
                    out.flush();
                    out.reset();
                    out.writeInt(a.getProc());
                    out.flush();
                    out.reset();
                    out.writeInt(idnum);
                    out.flush();


                    while (Master.percentages[Master.MaxNumOfWorkers - 1] == 0) {
                        Thread.sleep(200);
                    }


                } else {

                    idnum = in.readInt();


                    Master.calcDone[idnum] = false;
                    for (int j = 0; j < Master.MaxNumOfWorkers; j++) {
                        Master.counter[j]++;
                    }

                    while (Master.counter[idnum] < Master.MaxNumOfWorkers) {
                        Thread.sleep(200);
                    }
                    k++;
                    Master.sTables[idnum].setCurrentI(k);
                    SentTables a1;
                    if(Master.calcerror>=Master.lamda){


                    out.reset();
                    out.writeObject(Master.distributeXMatrixToWorkers(idnum));
                    out.flush();
                    a1 = (SentTables) in.readObject();

                    receiveresultsX(a1);

                    }
                    else{
                        SentTables send= Master.distributeXMatrixToWorkers(idnum);
                        send.setCurrentI(-1);
                        out.reset();
                        out.writeObject(send);
                        out.flush();
                    }
                    for (int j = 0; j < Master.MaxNumOfWorkers; j++) {
                        Master.counter[j]++;
                    }

                    while (Master.counter[idnum] < Master.MaxNumOfWorkers) {
                        Thread.sleep(500);
                    }

                    if(Master.calcerror>=Master.lamda) {
                        Master.sTablesY[idnum].setCurrentI(k);
                        out.reset();
                        out.writeObject(Master.distributeYMatrixToWorkers(idnum));
                        out.flush();
                        a1 = (SentTables) in.readObject();
                        receiveresultsY(a1);

                    }
                    else{
                        SentTables send= Master.distributeYMatrixToWorkers(idnum);
                        send.setCurrentI(-1);
                        out.reset();
                        out.writeObject(send);
                        out.flush();
                    }
                    for (int j = 0; j < Master.MaxNumOfWorkers; j++) {
                        Master.counter[j]++;
                    }

                    while (Master.counter[idnum] < Master.MaxNumOfWorkers) {
                        Thread.sleep(200);
                    }



                  k ++;

                }

            } catch (ClassNotFoundException e) {
                System.err.println("Error in read or write");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }




        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    public synchronized void receiveresultsX(SentTables a) {

        for (int i = a.start; i <= a.end; i++) {
            for (int j = 0; j < Master.xmatrix.getColumnDimension(); j++)

                Master.xmatrix.setEntry(i, j, a.getX().getEntry(i, j));
        }

        Master.counter[idnum] = 0;


        System.out.println("o x tou master enhmerwthhke epituxws");

    }

    public synchronized void receiveresultsY(SentTables a) {


        for (int i = a.start; i < a.end; i++) {
            for (int j = 0; j < Master.ymatrix.getColumnDimension(); j++)
                Master.ymatrix.setEntry(i, j, a.getY().getEntry(i, j));
        }

        Master.counter[idnum] = 0;
        Master.replay++;
        System.out.println("o y tou master enhmerwthhke epituxws");
    }

}
