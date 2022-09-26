import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

public class Tray {

    private final BufferedImage img;
    private final Graphics2D g2d;
    private final Supplier<Boolean> cbReadConfig;

    private TrayIcon trayIcon;

    private int intervalSeconds = 60 * 10;

    enum State {
        GOOD,
        BAD
    }

    private State previousState = State.GOOD;

    public Tray(Supplier<Boolean> cbReadConfig) {
        this.cbReadConfig = cbReadConfig;
        img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        try {
            trayIcon = createTrayicon(img);
            Font font = new Font("Fira Code", Font.PLAIN, 11);
            g2d.setFont(font);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            trayIcon.setImage(img);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public void updateTrayIcon(List<String> err) {
        boolean allThere = err.size() == 0;
        var text = allThere ? "OK" : "ER";

        if (allThere) {
            if (previousState == State.BAD) {
                previousState = State.GOOD;
                trayIcon.displayMessage("Wieder gut", "Alle Services sind (wieder) erreichbar.", TrayIcon.MessageType.INFO);
            }
            g2d.setBackground(new Color(0, 0, 0, 0));
            g2d.clearRect(0, 0, img.getWidth(), img.getHeight());
            g2d.setColor(Color.WHITE);
            trayIcon.setToolTip("All OK");
        } else {
            if (previousState == State.GOOD) {
                previousState = State.BAD;
                trayIcon.displayMessage("Achtung", "Mindestens ein Service ist nicht mehr erreichbar:\n" + String.join("\n", err), TrayIcon.MessageType.WARNING);
            }

            g2d.setBackground(new Color(1, 0, 0, 0.6f));
            g2d.clearRect(0, 0, img.getWidth(), img.getHeight());
            g2d.setColor(Color.RED);
            g2d.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
            g2d.setColor(Color.WHITE);
            trayIcon.setToolTip("ERROR:\n" + String.join("\n", err));
        }

        g2d.drawString(text, 1, 11);
        trayIcon.setImage(img);
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    private TrayIcon createTrayicon(BufferedImage img) throws AWTException {
        var tray = SystemTray.getSystemTray();

        final PopupMenu popup = new PopupMenu();
        MenuItem itemReadConfig = new MenuItem("Konfiguration lesen");
        MenuItem item10sec = new MenuItem("Intervall 10Sek");
        MenuItem item60sec = new MenuItem("Intervall 60Sek");
        MenuItem item3min = new MenuItem("Intervall 3Min");
        MenuItem item10min = new MenuItem("Intervall 10Min");
        MenuItem item30min = new MenuItem("Intervall 30Min");

        itemReadConfig.addActionListener(evt -> {
            cbReadConfig.get();
        });
        item10sec.addActionListener(evt -> {
            this.intervalSeconds = 10;
            item10sec.setEnabled(false);
            item60sec.setEnabled(true);
            item3min.setEnabled(true);
            item10min.setEnabled(true);
            item30min.setEnabled(true);
        });
        item60sec.addActionListener(evt -> {
            this.intervalSeconds = 60;
            item10sec.setEnabled(true);
            item60sec.setEnabled(false);
            item3min.setEnabled(true);
            item10min.setEnabled(true);
            item30min.setEnabled(true);
        });
        item3min.addActionListener(evt -> {
            this.intervalSeconds = 60 * 3;
            item10sec.setEnabled(true);
            item60sec.setEnabled(true);
            item3min.setEnabled(false);
            item10min.setEnabled(true);
            item30min.setEnabled(true);
        });
        item10min.addActionListener(evt -> {
            this.intervalSeconds = 60 * 10;
            item10sec.setEnabled(true);
            item60sec.setEnabled(true);
            item3min.setEnabled(true);
            item10min.setEnabled(false);
            item30min.setEnabled(true);
        });
        item30min.addActionListener(evt -> {
            this.intervalSeconds = 60 * 30;
            item10sec.setEnabled(true);
            item60sec.setEnabled(true);
            item3min.setEnabled(true);
            item10min.setEnabled(true);
            item30min.setEnabled(false);
        });

        popup.add(itemReadConfig);
        popup.addSeparator();
        popup.add(item10sec);
        popup.add(item60sec);
        popup.add(item3min);
        popup.add(item10min);
        popup.add(item30min);
        final TrayIcon trayIcn = new TrayIcon(img);
        trayIcn.setPopupMenu(popup);
        tray.add(trayIcn);
        return trayIcn;
    }
}
