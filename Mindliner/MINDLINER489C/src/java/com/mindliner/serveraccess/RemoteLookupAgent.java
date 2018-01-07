/*
 * ManagerLookupAgent.java
 *
 * Created on 21.09.2007, 23:20:15
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.serveraccess;

import com.mindliner.managers.CategoryManagerRemote;
import com.mindliner.managers.ColorManagerRemote;
import com.mindliner.managers.ConsistencyManagerRemote;
import com.mindliner.managers.EnvironmentManagerRemote;
import com.mindliner.managers.FeatureManagerRemote;
import com.mindliner.managers.HeadlineParserRemote;
import com.mindliner.managers.ImageManagerRemote;
import com.mindliner.managers.ImportManagerRemote;
import com.mindliner.managers.IslandManagerRemote;
import com.mindliner.managers.LinkManagerRemote;
import com.mindliner.managers.LogManagerRemote;
import com.mindliner.managers.ObjectFactoryRemote;
import com.mindliner.managers.ObjectManagerRemote;
import com.mindliner.managers.RatingAgentRemote;
import com.mindliner.managers.ReleaseManagerRemote;
import com.mindliner.managers.ReportManagerRemote;
import com.mindliner.managers.SearchManagerRemote;
import com.mindliner.managers.SecurityManagerRemote;
import com.mindliner.managers.SynchManagerRemote;
import com.mindliner.managers.TestManagerRemote;
import com.mindliner.managers.UserManagerRemote;
import com.mindliner.managers.WorkManagerRemote;
import com.mindliner.managers.ContainerMapManagerRemote;
import com.mindliner.managers.SubscriptionManagerRemote;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 *
 * @author Marius Messerli
 */
public class RemoteLookupAgent {

    private static Boolean isLocalStart = null;

    private static ObjectManagerRemote objectManager = null;
    private static LinkManagerRemote linkerRemote = null;
    private static CategoryManagerRemote categoryManagerRemote = null;
    private static SecurityManagerRemote securityManagerRemote = null;
    private static ColorManagerRemote colorManager = null;
    private static LogManagerRemote logManager = null;
    private static UserManagerRemote userManager = null;
    private static WorkManagerRemote workManager = null;
    private static RatingAgentRemote ratingAgent = null;
    private static SynchManagerRemote synchManager = null;
    private static ConsistencyManagerRemote consistencyManager = null;
    private static ObjectFactoryRemote objectFactory = null;
    private static ImportManagerRemote importManager = null;
    private static FeatureManagerRemote featureManager = null;
    private static TestManagerRemote testManager = null;
    private static ImageManagerRemote imageManager = null;
    private static HeadlineParserRemote headlineParser = null;
    private static ReportManagerRemote reportManager = null;
    private static SearchManagerRemote searchManager = null;
    private static EnvironmentManagerRemote environmentManager = null;
    private static ReleaseManagerRemote releaseManager = null;
    private static IslandManagerRemote islandManager = null;
    private static ContainerMapManagerRemote cmManager = null;
    private static SubscriptionManagerRemote subscriptionManager = null;
    private static InitialContext ctx = null;

    public static void clear() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException ex) {
                Logger.getLogger(RemoteLookupAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            ctx = null;
        }
        objectManager = null;
        linkerRemote = null;
        categoryManagerRemote = null;
        securityManagerRemote = null;
        colorManager = null;
        logManager = null;
        userManager = null;
        workManager = null;
        ratingAgent = null;
        synchManager = null;
        consistencyManager = null;
        objectFactory = null;
        importManager = null;
        featureManager = null;
        testManager = null;
        imageManager = null;
        headlineParser = null;
        reportManager = null;
        searchManager = null;
        environmentManager = null;
        releaseManager = null;
        islandManager = null;
        cmManager = null;
        subscriptionManager = null;

    }

    public static InitialContext getContext() throws NamingException {
        if (ctx == null) {
            ctx = new InitialContext();
        }
        return ctx;
    }

    /**
     * Indicates whether the current Mindliner instance was started through a
     * local start or not (web start or dev start).
     *
     * @return
     */
    public static boolean isLocalStart() {
        if (isLocalStart == null) {
            isLocalStart = !"localhost".equals(System.getProperty("org.omg.CORBA.ORBInitialHost"));
        }
        return isLocalStart;
    }

    public static Object getManagerForClass(Class c) throws NamingException {
        Object ref;
        getContext();
        if (c == CategoryManagerRemote.class) {
            if (categoryManagerRemote == null) {
                ref = ctx.lookup("com.mindliner.managers.CategoryManagerRemote");
                categoryManagerRemote = (CategoryManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return categoryManagerRemote;
        } else if (c == SecurityManagerRemote.class) {
            if (securityManagerRemote == null) {
                ref = ctx.lookup("com.mindliner.managers.SecurityManagerRemote");
                securityManagerRemote = (SecurityManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return securityManagerRemote;
        } else if (c == ColorManagerRemote.class) {
            if (colorManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ColorManagerRemote");
                colorManager = (ColorManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return colorManager;
        } else if (c == ObjectManagerRemote.class) {
            if (objectManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ObjectManagerRemote");
                objectManager = (ObjectManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return objectManager;
        } else if (c == LinkManagerRemote.class) {
            if (linkerRemote == null) {
                ref = ctx.lookup("com.mindliner.managers.LinkManagerRemote");
                linkerRemote = (LinkManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return linkerRemote;
        } else if (c == LogManagerRemote.class) {
            if (logManager == null) {
                ref = ctx.lookup("com.mindliner.managers.LogManagerRemote");
                logManager = (LogManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return logManager;
        } else if (c == UserManagerRemote.class) {
            if (userManager == null) {
                ref = ctx.lookup("com.mindliner.managers.UserManagerRemote");
                userManager = (UserManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return userManager;
        } else if (c == RatingAgentRemote.class) {
            if (ratingAgent == null) {
                ref = ctx.lookup("com.mindliner.managers.RatingAgentRemote");
                ratingAgent = (RatingAgentRemote) PortableRemoteObject.narrow(ref, c);
            }
            return ratingAgent;
        } else if (c == WorkManagerRemote.class) {
            if (workManager == null) {
                ref = ctx.lookup("com.mindliner.managers.WorkManagerRemote");
                workManager = (WorkManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return workManager;
        } else if (c == SynchManagerRemote.class) {
            if (synchManager == null) {
                ref = ctx.lookup("com.mindliner.managers.SynchManagerRemote");
                synchManager = (SynchManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return synchManager;
        } else if (c == ConsistencyManagerRemote.class) {
            if (consistencyManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ConsistencyManagerRemote");
                consistencyManager = (ConsistencyManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return consistencyManager;
        } else if (c == ObjectFactoryRemote.class) {
            if (objectFactory == null) {
                ref = ctx.lookup("com.mindliner.managers.ObjectFactoryRemote");
                objectFactory = (ObjectFactoryRemote) PortableRemoteObject.narrow(ref, c);
            }
            return objectFactory;
        } else if (c == ImportManagerRemote.class) {
            if (importManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ImportManagerRemote");
                importManager = (ImportManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return importManager;
        } else if (c == FeatureManagerRemote.class) {
            if (featureManager == null) {
                ref = ctx.lookup("com.mindliner.managers.FeatureManagerRemote");
                featureManager = (FeatureManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return featureManager;
        } else if (c == TestManagerRemote.class) {
            if (testManager == null) {
                ref = ctx.lookup("com.mindliner.managers.TestManagerRemote");
                testManager = (TestManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return testManager;
        } else if (c == ImageManagerRemote.class) {
            if (imageManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ImageManagerRemote");
                imageManager = (ImageManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return imageManager;
        } else if (c == HeadlineParserRemote.class) {
            if (headlineParser == null) {
                ref = ctx.lookup("com.mindliner.managers.HeadlineParserRemote");
                headlineParser = (HeadlineParserRemote) PortableRemoteObject.narrow(ref, c);
            }
            return headlineParser;
        } else if (c == ReportManagerRemote.class) {
            if (reportManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ReportManagerRemote");
                reportManager = (ReportManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return reportManager;
        } else if (c == SearchManagerRemote.class) {
            if (searchManager == null) {
                ref = ctx.lookup("com.mindliner.managers.SearchManagerRemote");
                searchManager = (SearchManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return searchManager;
        } else if (c == EnvironmentManagerRemote.class) {
            if (environmentManager == null) {
                ref = ctx.lookup("com.mindliner.managers.EnvironmentManagerRemote");
                environmentManager = (EnvironmentManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return environmentManager;
        } else if (c == ReleaseManagerRemote.class) {
            if (releaseManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ReleaseManagerRemote");
                releaseManager = (ReleaseManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return releaseManager;
        } else if (c == IslandManagerRemote.class) {
            if (islandManager == null) {
                ref = ctx.lookup("com.mindliner.managers.IslandManagerRemote");
                islandManager = (IslandManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return islandManager;
        } else if (c == ContainerMapManagerRemote.class) {
            if (cmManager == null) {
                ref = ctx.lookup("com.mindliner.managers.ContainerMapManagerRemote");
                cmManager = (ContainerMapManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return cmManager;
        } else if (c == SubscriptionManagerRemote.class) {
            if (subscriptionManager == null) {
                ref = ctx.lookup("com.mindliner.managers.SubscriptionManagerRemote");
                subscriptionManager = (SubscriptionManagerRemote) PortableRemoteObject.narrow(ref, c);
            }
            return subscriptionManager;
        } else {
            throw new IllegalArgumentException("No manager available for specified class.");
        }
    }
}
