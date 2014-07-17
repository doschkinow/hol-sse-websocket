package com.mycompany.drawingboard.processors;

import com.mycompany.drawingboard.Drawing;
import com.mycompany.drawingboard.Shape;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;
import java.io.IOException;

/**
 * Processor to add a Shape to an existing Drawing
 * 
 * @author mbraeuer
 * 
 */
public class AddShapeProcessor extends AbstractProcessor implements PortableObject {

    private Shape shape;

    public AddShapeProcessor() {
        
    }
    
    public AddShapeProcessor(Shape shape) {
        this.shape = shape;
    }

    public Object process(InvocableMap.Entry entry) {
        Drawing drawing = (Drawing) entry.getValue();
        if (drawing != null) {
            drawing.getShapes().add(shape);
            entry.setValue(drawing);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void readExternal(PofReader reader) throws IOException {
        shape = (Shape) reader.readObject(0);
    }

    @Override
    public void writeExternal(PofWriter writer) throws IOException {
        writer.writeObject(0, this.shape);
    }

}
