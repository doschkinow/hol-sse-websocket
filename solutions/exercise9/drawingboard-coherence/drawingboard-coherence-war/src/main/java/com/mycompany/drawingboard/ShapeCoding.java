package com.mycompany.drawingboard;

import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Encoder and decoder that de/en-codes web socket messages into/from Shape
 * objects.
 */
public class ShapeCoding implements Decoder.Text<Shape>, Encoder.Text<Shape> {

    @Override
    public Shape decode(String s) throws DecodeException {
        Shape shape = new Shape();

        try (JsonReader reader = Json.createReader(new StringReader(s))) {
            JsonObject object = reader.readObject();
            shape.setX(object.getInt("x"));
            shape.setY(object.getInt("y"));
            shape.setType(Shape.ShapeType.valueOf(
                                 object.getString("type")));
            shape.setColor(Shape.ShapeColor.valueOf(
                                 object.getString("color")));
        }

        return shape;
    }

    @Override
    public boolean willDecode(String s) {
        // we can always return true, as this decoder can work with any messages
        // used by this application
        return true;
    }

    @Override
    public String encode(Shape object) throws EncodeException {
        StringWriter result = new StringWriter();

        try (JsonGenerator gen = Json.createGenerator(result)) {
            gen.writeStartObject()
                    .write("x", object.getX())
                    .write("y", object.getY())
                    .write("type", object.getType().toString())
                    .write("color", object.getColor().toString())
                    .writeEnd();
        }

        System.out.println("ShapeCoding:encode " + result.toString());
        
        return result.toString();
    }

    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }
}
