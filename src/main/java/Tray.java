import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
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
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tray {

    private final BufferedImage img;
    private final Graphics2D g2d;

    private TrayIcon trayIcon;

    enum State {
        GOOD,
        BAD
    }

    private State previousState = State.GOOD;

    public Tray() {
        img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        try {
            trayIcon = createTrayicon(img);
            Font font = new Font("Hack NF", Font.PLAIN, 11);
            g2d.setFont(font);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            trayIcon.setImage(img);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public void updateTrayIcon(List<String> err) {
        boolean allThere = err.size() == 0;
        var text = allThere? "OK" : "ER";

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
            g2d.drawRect(0, 0, img.getWidth()-1, img.getHeight()-1);
            g2d.setColor(Color.WHITE);
            trayIcon.setToolTip("ERROR:\n" + String.join("\n", err));
        }

        g2d.drawString(text, 1, 11);

        trayIcon.setImage(img);

    }


    private TrayIcon createTrayicon(BufferedImage img) throws AWTException {
        var tray = SystemTray.getSystemTray();

        final TrayIcon trayIcon = new TrayIcon(img);
        tray.add(trayIcon);
        return trayIcon;
    }
}
