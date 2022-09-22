import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    record Link(String name, String url) {    }

    private final List<Link> URLS = List.of(
            new Link("ALPHA XSERVICES", "https://ebil-alpha.sparkassenverlag.de/ebilxservices/api/private/1.0/accesspass/authn/config/zbv/"),
            new Link("GAMMA XSERVICES", "https://ebil-gamma.sparkassenverlag.de/ebilxservices/api/private/1.0/accesspass/authn/config/zbv/"),
            new Link("PROD XSERVICES", "https://ebil.sparkassenverlag.de/ebilxservices/api/private/1.0/accesspass/authn/config/zbv/")
    );

    public static void main(String[] args) throws InterruptedException {
        new Main().monitor();
    }

    public void monitor() throws InterruptedException {
        Tray tray = new Tray();

        while (true) {
            List<String> notGoodItems = new ArrayList<>();
            URLS.forEach(link -> {
                try {
                    URL url = new URL(link.url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    int responseCode = con.getResponseCode();
                    if (responseCode >= 400) {
                        System.out.println(link.name + ", responseCode: " + responseCode);
                        notGoodItems.add(link.name);
                    }
                } catch (IOException ex) {
                    System.out.println(link.name + ", " + ex.getMessage());
                    notGoodItems.add(link.name);
                }
            });
            tray.updateTrayIcon(notGoodItems);
            TimeUnit.MINUTES.sleep(10);
        }
    }


}
