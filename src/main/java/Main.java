import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    record Link(String name, String url) {
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        if (args.length ==0) {
            System.out.println("Please provide a config json file as first parameter.");
            return;
        }

        List<Link> links = new ArrayList<>();

        JSONObject config = new JSONObject(Files.readString(Path.of(args[0])));
        config.getJSONArray("links").forEach(link -> {
            links.add(new Link(
                    ((JSONObject) link).getString("name"),
                    ((JSONObject) link).getString("url")));
        });

        new Main(links).monitor();
    }

    private final List<Link> links;

    public Main(List<Link> links) {
        this.links = new ArrayList<>(links);
    }

    public void monitor() throws InterruptedException {

        log("Start monitoring: ");
        links.forEach(link -> log(link.name + " at " + link.url));

        Tray tray = new Tray();

        while (true) {
            List<String> notGoodItems = new ArrayList<>();
            links.forEach(link -> {
                try {
                    URL url = new URL(link.url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    int responseCode = con.getResponseCode();
                    if (responseCode >= 400) {
                        log(link.name + ", responseCode: " + responseCode);
                        notGoodItems.add(link.name);
                    }
                } catch (IOException ex) {
                    log(link.name + ", " + ex.getMessage());
                    notGoodItems.add(link.name);
                }
            });
            tray.updateTrayIcon(notGoodItems);
            TimeUnit.MINUTES.sleep(10);
        }
    }

    private void log(String str) {
        System.out.println(LocalDateTime.now() + ": " + str);
    }

}
