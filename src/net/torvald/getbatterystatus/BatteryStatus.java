package net.torvald.getbatterystatus;

/**
 * Created by minjaesong on 2024-07-18.
 */
public class BatteryStatus {

    public boolean hasBattery;
    public boolean isCharging;
    public int percentage;

    public BatteryStatus(
        boolean hasBattery,
        boolean isCharging,
        int percentage
    ) {
        this.hasBattery = hasBattery;
        this.isCharging = isCharging;
        this.percentage = percentage;
    }

}
