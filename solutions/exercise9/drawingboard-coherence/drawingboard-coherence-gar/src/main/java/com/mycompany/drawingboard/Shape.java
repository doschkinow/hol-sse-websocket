package com.mycompany.drawingboard;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import java.io.IOException;

/**
 * POJO representing a shape.
 */
public class Shape implements PortableObject{

    /**
     * Shape types.
     */
    public static enum ShapeType {

        BIG_CIRCLE,
        SMALL_CIRCLE,
        BIG_SQUARE,
        SMALL_SQUARE,
    }

    /**
     * Shape colors.
     */
    public static enum ShapeColor {

        RED,
        GREEN,
        BLUE,
        YELLOW,
    }

    /**
     * Type of the shape.
     */
    private ShapeType type;
    
    /**
     * Shape color.
     */
    private ShapeColor color;
    
    /**
     * Shape coordinates.
     */
    private int x, y;

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }

    public ShapeColor getColor() {
        return color;
    }

    public void setColor(ShapeColor color) {
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "Shape(" + x + ", " + y + ", " + type + ", " + color + ")";
    }
    
    @Override
    public void readExternal(PofReader reader) throws IOException {
        this.setType(ShapeType.valueOf(reader.readString(0)));
        this.setColor(ShapeColor.valueOf(reader.readString(1)));
        this.setX(reader.readInt(2));
        this.setY(reader.readInt(3));
    }

    @Override
    public void writeExternal(PofWriter writer) throws IOException {
        writer.writeString(0, this.getType().toString());
        writer.writeString(1, this.getColor().toString());
        writer.writeInt(2, this.getX());
        writer.writeInt(3, this.getY());
    }
    
}
