package com.concord.ui;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class ConcordPanel extends PluginPanel
{
    private static final int LOGO_SIZE = 128;
    private static final int DISCORD_BUTTON_MAX_WIDTH = 140;
    private static final int DISCORD_BUTTON_MAX_HEIGHT = 48;

    private static final Color FOREGROUND_COLOR = new Color(34, 194, 93);
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 20);

    private JButton joinDiscordButton;
    private String discordInviteUrl = "";
    private JLabel connectionStatusLabel;

    public void init()
    {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setForeground(FOREGROUND_COLOR);
        setBorder(new EmptyBorder(20, 16, 20, 16));

        buildPanel();
    }

    private void buildPanel()
    {
        removeAll();
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        BufferedImage logoImage = ImageUtil.loadImageResource(getClass(), "/concord.png");
        Image scaledLogoImage = scaleImageToSize(logoImage, LOGO_SIZE, LOGO_SIZE);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogoImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Concord");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(FOREGROUND_COLOR);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitleLabel = new JLabel("Discord chat bridge");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(new Color(205, 214, 190));

        connectionStatusLabel = new JLabel("Status: Configure in settings");
        connectionStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectionStatusLabel.setForeground(new Color(200, 90, 90));

        BufferedImage discordButtonImage = ImageUtil.loadImageResource(getClass(), "/join_our_discord.png");
        Image scaledDiscordButtonImage = scaleImageToFit(
                discordButtonImage,
                DISCORD_BUTTON_MAX_WIDTH,
                DISCORD_BUTTON_MAX_HEIGHT
        );
        Image hoveredDiscordButtonImage = darkenImage(scaledDiscordButtonImage, 0.82f);
        joinDiscordButton = new JButton(new ImageIcon(scaledDiscordButtonImage));
        joinDiscordButton.setToolTipText("Join Our Discord");
        joinDiscordButton.setBorderPainted(false);
        joinDiscordButton.setContentAreaFilled(false);
        joinDiscordButton.setFocusPainted(false);
        joinDiscordButton.setOpaque(false);
        joinDiscordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        joinDiscordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinDiscordButton.setPreferredSize(new Dimension(scaledDiscordButtonImage.getWidth(null), scaledDiscordButtonImage.getHeight(null)));
        joinDiscordButton.setMaximumSize(joinDiscordButton.getPreferredSize());
        joinDiscordButton.addActionListener(e -> openDiscordInvite());
        joinDiscordButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                joinDiscordButton.setIcon(new ImageIcon(hoveredDiscordButtonImage));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                joinDiscordButton.setIcon(new ImageIcon(scaledDiscordButtonImage));
            }
        });

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(logoLabel);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(connectionStatusLabel);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(joinDiscordButton);
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void openDiscordInvite()
    {
        if (discordInviteUrl == null || discordInviteUrl.trim().isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Discord invite URL is not configured yet.");
            return;
        }

        try
        {
            Desktop.getDesktop().browse(new URI(discordInviteUrl));
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to open Discord: " + ex.getMessage());
        }
    }

    private Image scaleImageToFit(BufferedImage image, int maxWidth, int maxHeight)
    {
        if (image == null)
        {
            return image;
        }

        double widthScale = (double) maxWidth / image.getWidth();
        double heightScale = (double) maxHeight / image.getHeight();
        double scale = Math.min(1.0d, Math.min(widthScale, heightScale));

        int targetWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int targetHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
        return image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    }

    private Image scaleImageToSize(BufferedImage image, int width, int height)
    {
        if (image == null)
        {
            return null;
        }

        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();

        return scaled;
    }

    private Image darkenImage(Image image, float factor)
    {
        if (image == null)
        {
            return null;
        }

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        BufferedImage darkenedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int rgba = bufferedImage.getRGB(x, y);
                int alpha = (rgba >>> 24) & 0xFF;
                int red = (rgba >>> 16) & 0xFF;
                int green = (rgba >>> 8) & 0xFF;
                int blue = rgba & 0xFF;

                red = Math.max(0, Math.min(255, Math.round(red * factor)));
                green = Math.max(0, Math.min(255, Math.round(green * factor)));
                blue = Math.max(0, Math.min(255, Math.round(blue * factor)));

                darkenedImage.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }

        return darkenedImage;
    }

    public void setConnectionStatus(String text, Color color)
    {
        if (connectionStatusLabel != null)
        {
            connectionStatusLabel.setText(text);
            connectionStatusLabel.setForeground(color);
        }
    }

    public void setDiscordInviteUrl(String discordInviteUrl)
    {
        this.discordInviteUrl = discordInviteUrl == null ? "" : discordInviteUrl.trim();
        if (joinDiscordButton != null)
        {
            joinDiscordButton.setEnabled(!this.discordInviteUrl.isEmpty());
            joinDiscordButton.setToolTipText(this.discordInviteUrl.isEmpty()
                    ? "Discord invite is not configured yet"
                    : "Join Our Discord");
        }
    }

    public void showMessage(String message)
    {
        JOptionPane.showMessageDialog(this, message);
    }
}
