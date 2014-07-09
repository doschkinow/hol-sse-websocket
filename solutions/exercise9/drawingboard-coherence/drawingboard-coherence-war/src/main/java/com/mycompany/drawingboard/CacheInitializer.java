/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.drawingboard;

import static com.mycompany.drawingboard.DataProvider.ID_CACHE_NAME;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache; 
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author mbraeuer
 */
@Singleton
@Startup
public class CacheInitializer {

    @PostConstruct
    void initialiseID() {
        NamedCache drawingsCache = CacheFactory.getCache(ID_CACHE_NAME);
            
        //drawingsCache.put(-1, new Integer(0));
        // conditional put
        drawingsCache.invoke(-1, new ConditionalPut(new NotFilter(PresentFilter.INSTANCE), new Integer(0)));
    }
    
    
}
