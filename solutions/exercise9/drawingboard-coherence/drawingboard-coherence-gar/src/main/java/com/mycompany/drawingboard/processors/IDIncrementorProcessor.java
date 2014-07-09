/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.drawingboard.processors;

import com.mycompany.drawingboard.Shape;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mbraeuer
 */
public class IDIncrementorProcessor extends AbstractProcessor implements PortableObject {
    
    public IDIncrementorProcessor () {
    }
    
    public Object process(InvocableMap.Entry entry) {
        Integer id = (Integer) entry.getValue();
        
        Integer increment = new Integer(id.intValue() + 1); 
        entry.setValue(increment);
       
        return increment;
    }
    
    @Override
    public void readExternal(PofReader reader) throws IOException {
        //...
    }

    @Override
    public void writeExternal(PofWriter writer) throws IOException {
        //...
    }
    
}
