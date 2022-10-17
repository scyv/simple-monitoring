import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Main {

    record Link(String name, String url) {
    }

    public static void main(String[] args) throws InterruptedException {

        if (args.length == 0) {
            System.out.println("Please provide a config json file as first parameter.");
            return;
        }

        new Main(args[0]).monitor();
    }

    private final List<Link> links;
    private final String configPath;

    public Main(String configPath) {
        this.configPath = configPath;
        this.links = new ArrayList<>();
        this.readConfig();
    }

    public void monitor() throws InterruptedException {
        Tray tray = new Tray(this::readConfig);

        Set<String> notGoodItems = new HashSet<>();
        while (true) {
            links.forEach(link -> {
                try {
                    URL url = new URL(link.url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    int responseCode = con.getResponseCode();
                    if (responseCode < 400) {
                        if (notGoodItems.remove(link.name)) {
                            log(link.name + " is back to normal: " + responseCode);
                        }
                    } else {
                        log(link.name + ", responseCode: " + responseCode);
                        notGoodItems.add(link.name);
                    }
                } catch (IOException ex) {
                    log(link.name + ", " + ex.getMessage());
                    notGoodItems.add(link.name);
                }
            });
            tray.updateTrayIcon(new ArrayList<>(notGoodItems));
            int timeout = 0;

            while (timeout < (tray.getIntervalSeconds())) {
                TimeUnit.SECONDS.sleep(1);
                timeout++;
            }
        }
    }

    private void log(String str) {
        System.out.println(LocalDateTime.now() + ": " + str);
    }

    private Boolean readConfig() {
        links.clear();
        try {
            JSONObject config = new JSONObject(Files.readString(Path.of(configPath)));
            config.getJSONArray("links").forEach(link -> {
                Link newLink = new Link(
                        ((JSONObject) link).getString("name"),
                        ((JSONObject) link).getString("url"));
                links.add(newLink);
                log(newLink.toString());
            });

            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }
}
