package net.torvald.getbatterystatus;

import java.io.IOException;

/**
 * Created by minjaesong on 2024-07-18.
 */
public class GetBatteryStatusTest {

    public static void main(String[] args) throws IOException {
        BatteryStatus bs = GetBatteryStatus.get();

        System.out.println("Has Battery: "+bs.hasBattery+"\n" +
                "Charging: "+bs.isCharging+"\n" +
                "Current Charge: "+bs.percentage+"%");
    }

}
