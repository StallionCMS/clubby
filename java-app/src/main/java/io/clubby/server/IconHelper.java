package io.clubby.server;

import com.amazonaws.util.Md5Utils;
import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;


public class IconHelper {

    public File getOrCreateAutoIcon() throws IOException {
        File file = new File(Settings.instance().getDataDirectory() + "/auto-logo-icon.png");
        if (file.exists()) {
            return file;
        }

        String name = ClubbyDynamicSettings.getSiteName();
        if (empty(name)) {
            String url = Settings.instance().getSiteUrl();
            if (empty(url)) {
                url = Context.getRequest().requestUrl();
            }
            name = new URL(url).getHost();
        }

        name = name.toUpperCase();

        String initials = "";
        if (name.length() <= 3) {
            initials = name;
        } else if (name.contains(" ")) {
            String[] parts = StringUtils.split(name, " ");
            if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0) {
                initials = parts[0].substring(0, 1) + parts[1].substring(0, 1);
            } else if (parts.length >= 3 && parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0 && parts[2].length() > 0) {
                initials = parts[0].substring(0, 1) + parts[1].substring(0, 1) + parts[2].substring(0, 1);
            }
        }
        List<Character> vowells = list('A', 'E', 'U', 'O', 'I');
        if (empty(initials)) {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < name.length(); i++) {

                char c = name.charAt(i);
                if (vowells.indexOf(c) > -1) {
                    continue;
                }
                b.append(c);
                if (b.length() >= 3) {
                    initials = b.toString();
                    break;
                }
            }
        }
        if (empty(initials)) {
            initials = name.substring(0, 1) + name.substring(name.length()-1);
        }
        initials = initials.toUpperCase();

        BufferedImage bufferedImage = new BufferedImage(128, 128,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHints(rh);

        //graphics.setColor(new Color(60, 60, 90));
        int i = Md5Utils.computeMD5Hash(name.getBytes())[0];
        Log.info("Hash I {0}", i);
        graphics.setColor(Color.getHSBColor(i/256f, .8f, .4f));
        graphics.fillRect(0, 0, 128, 128);

        graphics.setColor(new Color(230, 230, 230));
        graphics.setFont(new Font("Arial", Font.BOLD, 80));

        int width = graphics.getFontMetrics().stringWidth(initials);
        int y = 90;
        if (width > 140) {
            graphics.setFont(new Font("Arial", Font.BOLD, 50));
            width = graphics.getFontMetrics().stringWidth(initials);
            y = 80;
        } else if (width > 100) {
            graphics.setFont(new Font("Arial", Font.BOLD, 60));
            width = graphics.getFontMetrics().stringWidth(initials);
            y = 85;
        }
        int x = (128 - width) / 2;
        graphics.drawString(initials, x, y);

        file.createNewFile();
        ImageIO.write(bufferedImage, "png", file);

        return file;
    }


}
