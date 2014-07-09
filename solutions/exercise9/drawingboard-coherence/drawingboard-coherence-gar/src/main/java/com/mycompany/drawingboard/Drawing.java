package com.mycompany.drawingboard;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * POJO representing a drawing.
 */
public class Drawing implements PortableObject, Comparable<Drawing> {
    
    /** Drawing ID. */
    private int id;
    
    /** Drawing name. */
    private String name;
    
    /** 
     * List of shapes the drawing consists of (or {@code null} if the drawing
     * is empty.
     */
    private List<Shape> shapes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void setShapes(List<Shape> shapes) {
        this.shapes = shapes;
    }
        
    @Override
    public String toString() {
        return "Drawing(" + name + ", " + shapes + ")";
    }

    @Override
    public void readExternal(PofReader reader) throws IOException {
        this.setId(reader.readInt(0));
        this.setName(reader.readString(1));
        
        ArrayList<Shape> arrayList = new ArrayList<Shape>();
        this.setShapes((List<Shape>) reader.readCollection(2, arrayList));
    }

    @Override
    public void writeExternal(PofWriter writer) throws IOException {
        writer.writeInt(0, this.getId());
        writer.writeString(1, this.getName());
        writer.writeCollection(2, this.getShapes());
    }

    public int compareTo(Drawing o) {    
        return Integer.compare(this.id, o.getId());  
       
    }
}
