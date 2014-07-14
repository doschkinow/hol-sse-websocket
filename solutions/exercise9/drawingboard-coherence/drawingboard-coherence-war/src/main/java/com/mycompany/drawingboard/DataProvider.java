package com.mycompany.drawingboard;

import com.mycompany.drawingboard.processors.IDIncrementorProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.AbstractMapListener;
import com.tangosol.util.MapEvent;
import java.util.Collections;


//TODO: Umbauen auf JCache API

/**
 * Simple in-memory data storage for the application.
 */
class DataProvider {

    public static String DRAWINGS_CACHE_NAME = "drawings-cache";
    public static String ID_CACHE_NAME  = "id-cache";
    
    /**
     * Broadcaster for server-sent events.
     */
    private static final SseBroadcaster sseBroadcaster = new SseBroadcaster();

    /**
     * Map that stores web socket sessions corresponding to a given drawing ID.
     */
    private static final MultivaluedHashMap<Integer, Session> webSockets
            = new MultivaluedHashMap<>();

    /**
     * Retrieves a drawing by ID.
     *
     * @param drawingId ID of the drawing to be retrieved.
     * @return Drawing with the corresponding ID.
     */
    static synchronized Drawing getDrawing(int drawingId) {
        NamedCache drawingsCache = CacheFactory.getCache(DRAWINGS_CACHE_NAME);
        return (Drawing) drawingsCache.get(drawingId);
    }

    /**
     * Retrieves all existing drawings.
     *
     * @return List of all drawings.
     */
    static synchronized List<Drawing> getAllDrawings() {
        NamedCache drawingsCache = CacheFactory.getCache(DRAWINGS_CACHE_NAME);
        List<Drawing> list = new ArrayList(drawingsCache.values());
        Collections.sort(list);
        return list;
    }

    /**
     * Creates a new drawing based on the supplied drawing object.
     *
     * @param drawing Drawing object containing property values for the new
     * drawing.
     * @return ID of the newly created drawing.
     */
    static synchronized int createDrawing(Drawing drawing) {
        NamedCache idCache = CacheFactory.getCache(ID_CACHE_NAME);
        Integer lastIdInteger = (Integer) idCache.invoke(-1, new IDIncrementorProcessor());
        
        Drawing result = new Drawing();
        result.setId(lastIdInteger);
        result.setName(drawing.getName());
        
        NamedCache drawingsCache = CacheFactory.getCache(DRAWINGS_CACHE_NAME);
        drawingsCache.put(result.getId(), result);
        return result.getId();
    }

    /**
     * Delete a drawing with a given ID.
     *
     * @param drawingId ID of the drawing to be deleted.
     * @return {@code true} if the drawing was deleted, {@code false} if there
     * was no such drawing.
     */
    static synchronized boolean deleteDrawing(int drawingId) {
        
        NamedCache drawingsCache = CacheFactory.getCache(DRAWINGS_CACHE_NAME);
        return drawingsCache.remove(drawingId) != null;
    }

    /**
     * Add a new shape to the drawing.
     *
     * @param drawingId ID of the drawing the shape should be added to.
     * @param shape Shape to be added to the drawing.
     * @return {@code true} if the shape was added, {@code false} if no such
     * drawing was found.
     */
    static synchronized boolean addShape(int drawingId, Shape shape) {
        Drawing drawing = getDrawing(drawingId);
        if (drawing != null) {
            drawing.getShapes().add(shape); //TODO ??? 
            // Cache update
            NamedCache drawingsCache = CacheFactory.getCache(DRAWINGS_CACHE_NAME);
            drawingsCache.put(drawingId, drawing);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Registers a new channel for sending events. An event channel corresponds
     * to a client (browser) event source connection.
     *
     * @param ec Event channel to be registered for sending events.
     */
    static void addEventOutput(EventOutput eo) {
        sseBroadcaster.add(eo);
    }

    /**
     * Registers a new web socket session and associates it with a drawing ID.
     * This method should be called when a client opens a web socket connection
     * to a particular drawing URI.
     *
     * @param drawingId Drawing ID to associate the web socket session with.
     * @param session New web socket session to be registered.
     */
    static synchronized void addWebSocket(int drawingId, Session session) {
        webSockets.add(drawingId, session);

        Drawing drawing = getDrawing(drawingId);
        if (drawing != null && drawing.getShapes() != null) {
            for (Shape shape : drawing.getShapes()) {
                try {
                    session.getBasicRemote().sendObject(shape);
                } catch (IOException | EncodeException ex) {
                    Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Removes the existing web socket session associated with a drawing ID.
     * This method should be called when a client closes the web socket
     * connection to a particular drawing URI.
     *
     * @param drawingId ID of the drawing the web socket session is associated
     * with.
     * @param session Web socket session to be removed.
     */
    static synchronized void removeWebSocket(int drawingId, Session session) {
        List<Session> sessions = webSockets.get(drawingId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    /**
     * Broadcasts the newly added shape to all web sockets associated with the
     * affected drawing.
     *
     * @param drawingId ID of the affected drawing.
     * @param shape Shape that was added to the drawing or
     * {@link ShapeCoding#SHAPE_CLEAR_ALL} if the drawing was cleared (i.e. all
     * shapes were deleted).
     */
    private static void wsBroadcast(int drawingId, Shape shape) {
        List<Session> sessions = webSockets.get(drawingId);
        if (sessions != null) {
            for (Session session : sessions) {
                try {
                    session.getBasicRemote().sendObject(shape);
                } catch (IOException | EncodeException ex) {
                    Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static class DrawingsCacheEventListener extends AbstractMapListener {

        public void entryInserted(MapEvent event) {
            sseBroadcaster.broadcast(new OutboundEvent.Builder()
                    .name("create")
                    .data(Drawing.class, event.getNewValue())
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build());
        }

        public void entryUpdated(MapEvent event) {
            Drawing drawing = (Drawing) event.getNewValue();
            List<Shape> shapes = drawing.getShapes();
            Shape shape = shapes.get(shapes.size() - 1);
            wsBroadcast(drawing.getId(), shape);
        }

        public void entryDeleted(MapEvent event) {
            sseBroadcaster.broadcast(new OutboundEvent.Builder()
                .name("delete")
                .data(String.class, String.valueOf(((Drawing)event.getOldValue()).getId()))
                .build());
        }
    }
}
