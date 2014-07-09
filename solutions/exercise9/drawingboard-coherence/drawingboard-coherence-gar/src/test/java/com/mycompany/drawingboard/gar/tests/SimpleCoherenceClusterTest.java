package com.mycompany.drawingboard.gar.tests;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.mycompany.drawingboard.Shape;
import com.mycompany.drawingboard.Drawing;

import com.oracle.tools.junit.AbstractTest;
import com.oracle.tools.runtime.coherence.Cluster;
import com.oracle.tools.runtime.coherence.ClusterBuilder;
import com.oracle.tools.runtime.coherence.ClusterMember;
import com.oracle.tools.runtime.coherence.ClusterMemberSchema;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.NativeJavaApplicationBuilder;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheFactoryBuilder;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.XmlDocument;
import com.tangosol.run.xml.XmlHelper;

import java.util.ArrayList;
import java.util.List;

public class SimpleCoherenceClusterTest extends AbstractTest {

    static {
        System.setProperty("tangosol.coherence.clusterport", "41111");
        System.setProperty("tangosol.coherence.cluster", "simple-coherence-cluster");
        System.setProperty("tangosol.distributed.localstorage", "false");
    }

    private static final int CLUSTER_SIZE = 2;
    private static final int CLUSTER_PORT = Integer.parseInt(System
            .getProperty("tangosol.coherence.clusterport"));
    private static final String CLUSTER_NAME = System
            .getProperty("tangosol.coherence.cluster");
    private static final String CACHE_CONFIG_FILE = "coherence-drawingboard-cache-config.xml";
    private static final String CACHE_NAME = "DrawingsCache";

    private Cluster cluster;
    private ConfigurableCacheFactory clientCacheFactory;
    private NamedCache cache;

    @Before
    public void setupTest() {

        createCluster();

        try {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();

        }

        clientCacheFactory = createClientCacheFactory(CACHE_CONFIG_FILE);
        
        cache = clientCacheFactory.ensureCache(CACHE_NAME, null);
        
        warmUpCache();

    }

    @Test
    public void testMe() {

        try {
             
            Assert.assertEquals(cache.size(), 1);
            
            Drawing aDrawing = (Drawing) cache.get(10);
            
            Shape aShape = aDrawing.getShapes().get(0);
            
            Assert.assertEquals(aShape.getType(), Shape.ShapeType.BIG_CIRCLE);
            
            Shape shape2 = new Shape();
            shape2.setColor(Shape.ShapeColor.RED);
            shape2.setType(Shape.ShapeType.BIG_SQUARE);
            shape2.setX(20);
            shape2.setY(20);
        
        
            aDrawing.getShapes().add(shape2);
            
            cache.put(aDrawing.getId(), aDrawing);

            return;

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void warmUpCache() {
        Shape shape1 = new Shape();
        shape1.setColor(Shape.ShapeColor.RED);
        shape1.setType(Shape.ShapeType.BIG_CIRCLE);
        shape1.setX(10);
        shape1.setY(10);
        
        List<Shape> shapes = new ArrayList<Shape>();
        shapes.add(shape1);
        
        Drawing drawing1 = new Drawing();
        drawing1.setId(10);
        drawing1.setName("Cool Drawing");
        drawing1.setShapes(shapes);
        
        cache.put(Integer.valueOf(drawing1.getId()), drawing1);
    }

    @After
    public void tearDownTest() {
        closeCluster();
    }

    private ConfigurableCacheFactory createClientCacheFactory(
            String cacheConfigurationURI) {

        CacheFactoryBuilder cacheFactoryBuilder = CacheFactory
                .getCacheFactoryBuilder();

        XmlDocument clientConfigXml = XmlHelper.loadFileOrResource(
                cacheConfigurationURI, "client");

        cacheFactoryBuilder.setCacheConfiguration(cacheConfigurationURI, null,
                clientConfigXml);

        return cacheFactoryBuilder.getConfigurableCacheFactory(
                cacheConfigurationURI, null);
    }

    private void createCluster() {

        ClusterMemberSchema schema = new ClusterMemberSchema()
                .setCacheConfigURI(CACHE_CONFIG_FILE).useLocalHostMode()
                .setClusterPort(CLUSTER_PORT).setClusterName(CLUSTER_NAME);

        try {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(
                    new NativeJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>(),
                    schema, "DCS", CLUSTER_SIZE);

            cluster = builder.realize(new SystemApplicationConsole());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    private void closeCluster() {
        try {
            if (cluster != null) {
                cluster.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
