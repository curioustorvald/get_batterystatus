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
        return null;
    }

    private static BatteryStatus getBattWin() throws IOException {
        return null;
    }

    private static BatteryStatus getBattMac() throws IOException {
        BufferedReader br = runCmdAndGetReader("pmset -g batt");
        String l1 = br.readLine(); // Now drawing from 'AC Power' or Now drawing from 'Battery Power'
        String l2 = br.readLine(); //  -InternalBattery-0 (id=some number)<tab>100%; charged; 0:00 remaining present: true

        if (l2 == null || l2.isEmpty()) return new BatteryStatus(false, true, 100);

        Pattern pat = Pattern.compile("(?<=\t)[0-9]+(?=%)");
        Matcher mat = pat.matcher(l2);

        boolean charging = l1.contains("'AC Power'");
        mat.find();
        String percentageStr = mat.group();
        int perc = Integer.parseInt(percentageStr);

        return new BatteryStatus(true, charging, perc);
    }

    private static BatteryStatus getBattLinux() throws IOException {
        return null;
    }


    private static BufferedReader runCmdAndGetReader(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        InputStreamReader ir = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(ir);

        return br;
    }
}
