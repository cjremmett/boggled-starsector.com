package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import org.lwjgl.input.Keyboard;
import sun.net.www.http.HttpClient;

import javax.net.ssl.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class BoggledUpdateNotificationScript implements EveryFrameScript
{
    private boolean hasWarned = false;
    private float timeUntilWarn = .75f;

    public BoggledUpdateNotificationScript() { }

    private void checkForUpdates()
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                runVersionCheck();
                reportSettings();
            }
        };

        thread.start();
    }

    private void runVersionCheck()
    {
        // This doesn't alert the user if the update check failed. That would just serve
        // to annoy the user, since it's unlikely they could fix the problem preventing them
        // from checking for updates - likely AV software or national firewalls.

        // Nexerelin has a built-in version checker that supports this mod.
        // It's superfluous to notify the player about TASC updates if they have Nex enabled.
        Boolean nexEnabled = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if(nexEnabled)
        {
            return;
        }

        CampaignUIAPI ui = Global.getSector().getCampaignUI();

        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}

                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL url = new URL("https://boggled-starsector.com/api/version/");

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(allHostsValid);

            int responseCode = connection.getResponseCode();

            if (responseCode != 200)
            {
                // Do nothing
            }
            else
            {
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext())
                {
                    inline = inline + scanner.nextLine();
                }

                scanner.close();

                inline = inline.replace("version", "");
                inline = inline.replace(":", "");
                inline = inline.replace("{", "");
                inline = inline.replace("}", "");
                inline = inline.replace("\"", "");
                inline = inline.replace("\"", "");
                inline = inline.replace(" ", "");

                String installedVersion = Global.getSettings().getModManager().getModSpec("Terraforming & Station Construction").getVersion();
                if(inline.equals(installedVersion))
                {
                    ui.addMessage("You have the latest version of TASC.", Color.GREEN);
                }
                else
                {
                    ui.addMessage("There is an update available for TASC.", Color.RED);
                }
            }
        }
        catch (Exception e)
        {
            // Do nothing

            //ui.addMessage("Unable to check for updates.", Color.RED);
        }
    }

    private void reportSettings()
    {
        // Check if a data file with the player's UUID exists. If not, create one.
        // Can't rely solely on IP address to identify players - they might be behind
        // the same proxy as other users (ex. might be behind the same popular VPN).
        String UUID = null;
        try
        {
            // If the specified data file doesn't exist, readTextFileFromCommon returns an empty string.
            // In that case, no exception is thrown and UUID is empty, but not null.
            UUID = Global.getSettings().readTextFileFromCommon("BOGGLED-TASC");

            // Generate data file if one doesn't exist
            if(UUID.equals(""))
            {
                try
                {
                    java.util.UUID rand = java.util.UUID.randomUUID();
                    UUID = rand.toString();
                    Global.getSettings().writeTextFileToCommon("BOGGLED-TASC",UUID);
                    reportSettingsCall(UUID);
                }
                catch (Exception f)
                {
                    // Do nothing
                }
            }
            else
            {
                reportSettingsCall(UUID);
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }

    }

    private void reportSettingsCall(String UUID)
    {
        CampaignUIAPI ui = Global.getSector().getCampaignUI();

        String query = getSettingsQuery();

        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}

                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL url = new URL("https://boggled-starsector.com/api/analytics?uuid=" + UUID + getSettingsQuery());

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(allHostsValid);

            // Print the response code and message. For debug purposes.
            int responseCode = connection.getResponseCode();
            //ui.addMessage("Response code: " + responseCode, Color.YELLOW);

            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())))
            {
                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null)
                {
                    content.append(line);
                    content.append(System.lineSeparator());
                }

                //ui.addMessage(content.toString(), Color.GREEN);
            }
            catch (Exception f)
            {
                // Do nothing

                //ui.addMessage("Exception thrown trying to read message content.", Color.RED);
            }
        }
        catch (Exception e)
        {
            // Do nothing

            //ui.addMessage("Exception thrown trying to make PUT request.", Color.RED);
        }
    }

    private String getSettingsQuery()
    {
        return "&boggledHydroponicsEnabled=" + Global.getSettings().getBoolean("boggledHydroponicsEnabled") + "&boggledCloningEnabled=" + Global.getSettings().getBoolean("boggledCloningEnabled") + "&boggledDomedCitiesBuildableOnWaterWorlds=" + Global.getSettings().getBoolean("boggledDomedCitiesBuildableOnWaterWorlds");
    }

    @Override
    public boolean isDone()
    {
        if(hasWarned)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean runWhilePaused()
    {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        // Don't do anything while in a menu/dialog
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (Global.getSector().isInNewGameAdvance() || ui.isShowingDialog() || ui.isShowingMenu())
        {
            return;
        }

        // On first game load, warn about any updates available
        if (!hasWarned && timeUntilWarn <= 0f)
        {
            checkForUpdates();
            hasWarned = true;
        }
        else
        {
            timeUntilWarn = timeUntilWarn - amount;
        }
    }
}