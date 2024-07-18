package net.torvald.getbatterystatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by minjaesong on 2024-07-18.
 */
public class GetBatteryStatus {

    private static final int LINUX = 0;
    private static final int WIN = 1;
    private static final int OSX = 2;

    private static int getOS() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            return WIN;
        }
        else if (OS.contains("OS X")) {
            return OSX;
        }
        else {
            return LINUX;
        }
    }

    private static BatteryStatus nullStatus = new BatteryStatus(false, false, 0);

    /**
     * Don't call this function every frame or something, only poll the value sparingly (around 2 seconds)
     * @return
     * @throws IOException
     */
    public static BatteryStatus get() throws IOException {
        switch (getOS()) {
            case WIN:
                return getBattWin();
            case LINUX:
                return getBattLinux();
            case OSX:
                return getBattMac();
        };
        return nullStatus;
    }

    private static BatteryStatus getBattWin() throws IOException {
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

        return batteryStatus.toBatteryStatus();
    }

    private static BatteryStatus getBattMac() throws IOException {
        BufferedReader br = runCmdAndGetReader("pmset -g batt");
        String l1 = br.readLine(); // Now drawing from 'AC Power' or Now drawing from 'Battery Power'
        String l2 = br.readLine(); //  -InternalBattery-0 (id=some number)<tab>100%; charged; 0:00 remaining present: true

        if (l2 == null || l2.isEmpty()) return new BatteryStatus(false, true, 100);

        Pattern pat = Pattern.compile("(?<=\t)[0-9]+(?=%)");
        Matcher mat = pat.matcher(l2);

        boolean charging = l1.contains("'AC Power'");
        if (mat.find()) {
            String percentageStr = mat.group();
            int perc = Integer.parseInt(percentageStr);

            return new BatteryStatus(true, charging, perc);
        }
        else return nullStatus;
    }

    private static BatteryStatus getBattLinux() throws IOException {
        // check if battery is there
        // if the system has one, the output of `upower -e` should contain
        // `/org/freedesktop/UPower/devices/battery_BAT0`
        // `/org/freedesktop/UPower/devices/battery_macsms_battery`
        BufferedReader br1 = runCmdAndGetReader("upower -e");
        String devBat = null;
        String lineRead;
        while ((lineRead = br1.readLine()) != null) {
            if (lineRead.startsWith("/org/freedesktop/UPower/devices/battery_BAT0") ||
                    lineRead.startsWith("/org/freedesktop/UPower/devices/battery_macsms_battery")) {
                devBat = lineRead;
                break;
            }
        }

        if (devBat == null) return nullStatus;

        BufferedReader br2 = runCmdAndGetReader("upower -i "+devBat);

        boolean lineFound = false;
        boolean charging = false;
        while ((lineRead = br2.readLine()) != null) {
            // search for the line "    percentage:     00%" or "    percentage:     00.0000%"
            if (lineRead.matches("(?<=percentage:)[\\t ]+[0-9]+(\\.[0-9]+)?(?=%)")) {
                lineFound = true;
            }
            // search for the line "    state:   charging"
            else if (lineRead.matches("(?<=[\\t ])charging")) {
                charging = true;
            }
        }

        if (!lineFound) return nullStatus;

        Pattern pat = Pattern.compile("(?<=[\\t ])[0-9]+(\\.[0-9]+)?(?=%)");
        Matcher mat = pat.matcher(lineRead);
        if (mat.find()) {
            // get substring before '.'
            String percentageStr0 = mat.group();
            int dotIndex = percentageStr0.indexOf('.');
            int percentage;
            if (dotIndex < 0) {
                percentage = Integer.parseInt(percentageStr0);
            }
            else {
                String percentageStr1 = percentageStr0.substring(0, dotIndex);
                percentage = Integer.parseInt(percentageStr1);
            }

            return new BatteryStatus(true, charging, percentage);
        }
        else return nullStatus;
    }


    private static BufferedReader runCmdAndGetReader(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        InputStreamReader ir = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(ir);

        return br;
    }
}
