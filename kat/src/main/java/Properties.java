import java.io.Serializable;
import java.io.SerializablePermission;
import java.net.Inet4Address;

public class Properties implements Serializable {
    long Ram;
    int Proc;
    String localip;

    public String getLocalip() {
        return localip;
    }

    public void setLocalip(String localip) {
        this.localip = localip;
    }

    public Properties(long ram, int proc){
        Ram=ram;
        Proc=proc;


    }

    public long getRam() {
        return Ram;
    }

    public void setRam(long ram) {
        Ram = ram;
    }

    public int getProc() {
        return Proc;
    }

    public void setProc(int proc) {
        Proc = proc;
    }
    public String toString(){
        return (getRam()+" ram"+getProc());
    }

}
