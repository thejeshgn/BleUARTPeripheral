package thejeshgn.com.bleuartperipheral.data;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by thej on 15/12/16.
 */


public class DataManager {
    private final static String TAG = DataManager.class.getSimpleName();
    private static DataManager instance;
    private static List allPackets = new ArrayList();
    private static boolean final_packet_received = false;

    private DataManager(){

    }

    public static DataManager getInstance() {
        if (null == instance) {
            instance = new DataManager();
        }
        return instance;
    }

    public static void addPacket(byte[] packet){
        allPackets.add(packet);
        //if its last packet by checking if the last byte is \n
        if (packet[packet.length-2] == Util.packet_indicator_last[0] &&  packet[packet.length-1] == Util.packet_indicator_last[1]){
            final_packet_received = true;
        }
    }

    public static boolean isMessageComplete(){
        return final_packet_received;
    }



    public static byte[] getTheCompleteMesssage(){
        byte[] final_message={};
        for (int time = 0; time < allPackets.size(); time++) {
            byte[] packet = (byte[])allPackets.get(time);
            final_message = Util.appendData(final_message,packet);
        }
        return final_message;
    }

    public static void clear(){
        allPackets = new ArrayList();
        final_packet_received = false;
    }





}
