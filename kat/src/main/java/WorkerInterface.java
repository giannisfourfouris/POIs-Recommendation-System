import org.apache.commons.math3.linear.RealMatrix;

public interface WorkerInterface {
    void initialize();

    void calculateCMatrix(int k, RealMatrix realm) ;


    void calculateCuMatrix(int u, RealMatrix realc) ;


    void calculateCiMatrix(int i, RealMatrix realc) ;

    RealMatrix preCalculateYY(RealMatrix realx) ;

    RealMatrix preCalculateXX(RealMatrix realy);

    RealMatrix calculate_x_u(int u, RealMatrix realy, RealMatrix realp) ;

    RealMatrix calculate_y_i(int i, RealMatrix realx, RealMatrix realp);


}

