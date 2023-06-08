package com.ihsoy.ghostsepulchre;

import com.ihsoy.ghostsepulchre.recording.StateHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
@Slf4j
public class GhostSepulchreOverlay extends Overlay {

    private final Client client;
    private final GhostSepulchreConfig config;
    private final GhostSepulchrePlugin plugin;
    private final StateHandler stateHandler;

    @Inject
    private GhostSepulchreOverlay(Client client, GhostSepulchrePlugin plugin, GhostSepulchreConfig config, StateHandler stateHandler) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.stateHandler = stateHandler;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        WorldPoint point = stateHandler.getCurrentPlaybackPoint();
        if(point != null) {
            if (point.getPlane() == client.getPlane())
            {
                renderGhost(graphics, point);
            }
        }
        return null;
    }

    private void renderGhost(final Graphics2D graphics, final WorldPoint point) {
        log.info("Point: {}", point);

        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, config.ghostColor());
    }
}