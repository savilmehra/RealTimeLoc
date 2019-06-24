package in.railyatri.rylocation.ResourcesAccessLayer.NetworkLocation;

/**
 * Created by Saldi on 4/5/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public interface NetworkLocationConstants {

    interface NetworkLocation {
        String MNC = "mnc";
        String CELLID = "cellId";
        String LOCATION = "lac";
    }

    interface RequestType {
        int FAST = 0;
        int FOREGROUND = 1;
        int ON_TRIP = 2;
        int IDLE = 3;
        int MAPLOCATION = 4;
        int GPS = 5;
        int LTS = 6;
    }
}

