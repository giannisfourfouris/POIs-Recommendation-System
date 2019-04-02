import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;

public interface MasterInterface {
    void initialize();

    void calculateCMatrix(int k, RealMatrix realm);

    //void distributeXMatrixToWorkers(int M, int K, RealMatrix realm);

    //void distributeYMatrixToWorkers(int K, int N, RealMatrix realn);

    void calculatePMatrix(RealMatrix realm);

    //double calculateError();

    double calculateScore(int a, int b);

    List<Poi> calculateBestLocalPoisForUser(int user, double latitude, double longitude, int numofpois, String Category);


}
