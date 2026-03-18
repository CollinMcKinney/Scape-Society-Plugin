package com.Concord;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ConcordPanel extends PluginPanel
{
    // Reference to main plugin
    private ConcordPlugin plugin;

    private static final Color CLAN_COLOR_PRIMARY = new Color(117, 170, 0);
    private static final Color CLAN_COLOR_SECONDARY = new Color(64, 64, 64);

    public void setPlugin(ConcordPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void init()
    {
        // Remove default PluginPanel border and make background opaque
        setLayout(new BorderLayout());
        setBackground(CLAN_COLOR_SECONDARY);
        setForeground(CLAN_COLOR_PRIMARY);
        setBorder(new EmptyBorder(10,10, 10, 10));
        setOpaque(true);


        buildPanel(this);
    }

    private void buildPanel(JPanel wrapper)
    {
        wrapper.removeAll();

        // --- HEADER ---
        JPanel headerPanel = buildHeader();
        wrapper.add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CLAN_COLOR_SECONDARY);

        wrapper.add(contentPanel, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(0,0,0,0));

        revalidate();
        repaint();
    }

    private JPanel buildHeader()
    {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLAN_COLOR_SECONDARY);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        // --- ICON ---
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon_128x128.png");
        JLabel imageLabel = new JLabel(new ImageIcon(icon));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(imageLabel, BorderLayout.CENTER);

        // --- OPTIONAL TITLE ---
        // JLabel title = new JLabel("Concord");
        // title.setFont(FontManager.getRunescapeFont());
        // title.setForeground(CLAN_COLOR_PRIMARY);
        // title.setHorizontalAlignment(SwingConstants.CENTER);
        // header.add(title, BorderLayout.SOUTH);

        // --- OPTIONAL BUTTON ---
        // JButton settingsBtn = new JButton("⚙");
        // header.add(settingsBtn, BorderLayout.EAST);

        return header;
    }
}