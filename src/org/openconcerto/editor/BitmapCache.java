package org.openconcerto.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BitmapCache {
    private final Font f;
    private final int charWidth;
    private final int charHeight;
    private final Color color;
    private int charY;

    public BitmapCache(Font f, Color color) {
        this.f = f;
        this.color = color;
        this.charWidth = f.getSize();
        this.charHeight = (int) (f.getSize() * 1.5);
        this.charY = f.getSize();
    }

    private Map<Character, Image> characterImageCache = new HashMap<>();

    /**
     * Get character image from cache or create if not present
     * 
     * @param c the character to render
     * @return an image of the character
     */
    public Image getCharacterImage(char c) {
        if (this.characterImageCache.containsKey(c)) {
            return this.characterImageCache.get(c);
        } else {
            // Create and cache the image
            Image characterImage = createCharacterImage(c);
            this.characterImageCache.put(c, characterImage);
            return characterImage;
        }
    }

    private Image createCharacterImage(char c) {

        final BufferedImage image = new BufferedImage(this.charWidth, this.charHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setColor(this.color);
        g2d.setFont(this.f); // Adjust font size as needed
        g2d.drawString(String.valueOf(c), 0, this.charY);

        g2d.dispose();
        return image;
    }

}
