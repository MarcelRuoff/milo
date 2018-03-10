package org.eclipse.milo.examples.server.methods;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPRepository;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.events.SesameStartupEvent;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.Out;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.semanticweb.yars.nx.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.aifb.step.api.AbstractSemanticStateBasedResource;
import edu.kit.aifb.step.resources.FlsVisitourCallInstance;
import edu.kit.aifb.step.vocabs.STEP;
import edu.kit.aifb.step.wrapper.Utils;

public class MarmottaGET {
	@Inject
	private ConfigurationService configurationService;

	@Inject
	private LdpService ldpService;

	@Inject
	private ExportService exportService;

	@Inject
	private SesameService sesameService;

	@Inject
	private MarmottaIOService ioService;

	@Inject
	private LdpBinaryStoreService binaryStore;
	
	public Map<Node, String> interactionPatterns = new HashMap<Node, String>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @UaMethod
    public void invoke(
        InvocationContext context,

        @UaInputArgument(
            name = "InputBody",
            description = "Input Body for the Creation of a Resource")
            String InputBody,
        
        @UaInputArgument(
       		name = "URI")
        	String resource,

        @UaOutputArgument(
            name = "OutputBody",
            description = "Information about the creation")
            Out<String> outputBody) {
    	
    	//initialize();
    	
		interactionPatterns.put(STEP.BayesNode, "BayesNode");
		interactionPatterns.put(STEP.FLSVisitourAPI, "edu.kit.aifb.step.resources.FlsVisitourCallContainer");
		interactionPatterns.put(STEP.StartAPI, "edu.kit.aifb.step.linkeddatawebservice.LinkedDataWebService");

    	
    	try {

    		Repository repo = new HTTPRepository(resource);
    		repo.initialize();
    		
    		final RepositoryConnection conn = repo.getConnection();
    		conn.begin();
    		
			if (isCallInstanze(resource, conn)) {

				FlsVisitourCallInstance callInstance = new FlsVisitourCallInstance(resource,
						resource.substring(resource.lastIndexOf("/") + 1));

					String graph = "";
					Iterable<Node[]> nodes = callInstance.read();
					Iterator<Node[]> iter = nodes.iterator();
					while (iter.hasNext()) {
						Node[] node = iter.next();
						graph += node[0].getLabel() + " " + node[1].getLabel() + " " + node[2].getLabel() + "\n";
					}
					outputBody.set(graph);

					
			} else if (ldpService.isVirtualResource(conn, resource)) {

				// =============================================================================================
				//
				// is a FLSResource
				//
				// TODO einfügen von getChild()
				// =============================================================================================
				RepositoryResult<Statement> neededPatterns = conn.getStatements(
						ValueFactoryImpl.getInstance().createURI(resource),
						ValueFactoryImpl.getInstance().createURI(STEP.interactionPattern.getLabel()), null, true);

				while (neededPatterns.hasNext()) {
					Statement neededClass = neededPatterns.next();
					try {
						Class<?> cls_Test = Class.forName(interactionPatterns
								.get(new org.semanticweb.yars.nx.Resource(neededClass.getObject().toString())));
						Constructor<?> co = cls_Test.getConstructor(String.class, RepositoryConnection.class, LdpBinaryStoreService.class);
						AbstractSemanticStateBasedResource res = (AbstractSemanticStateBasedResource) co.newInstance(resource, conn, binaryStore);

							String graph = "";
							Iterable<org.semanticweb.yars.nx.Resource> childs= res.contains();
							Iterator<org.semanticweb.yars.nx.Resource> iterChilds = childs.iterator();
							Iterable<Node[]> nodes = res.read();
							Iterator<Node[]> iter = nodes.iterator();
							while (iter.hasNext()) {
								Node[] node = iter.next();
								graph += node[0].getLabel() + " " + node[1].getLabel() + " " + node[2].getLabel() + "\n";
							}
							while (iterChilds.hasNext()) {
								org.semanticweb.yars.nx.Resource child = iterChilds.next();
								graph += resource + " " + org.semanticweb.yars.nx.namespace.LDP.CONTAINS + " " + child.getLabel() + "\n";
							}

							conn.commit();
							outputBody.set(graph);
							
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("sqrt(" + InputBody.toString() + ")");
        logger.debug("Invoking post() method of Object '{}'", context.getObjectNode().getBrowseName().getName());
        
       
    }
    
	protected void initialize() {
		String root = UriBuilder.fromUri(configurationService.getBaseUri()).path(LdpWebService.PATH).build()
				.toASCIIString();
		try {
			final RepositoryConnection conn = sesameService.getConnection();
			try {
				conn.begin();
				ldpService.init(conn, conn.getValueFactory().createURI(root));
				System.out.println("Created LDP root container <{}>"+ root);
				conn.commit();
			} finally {
				conn.close();
			}
		} catch (RepositoryException e) {
			System.out.print("Error creating LDP root container <{}>: {}" + root + e.getMessage() + e);
		}
	}
    
    public boolean isCallInstanze(String uriInfo, RepositoryConnection conn) {

		//TODO make more agile for other InteractionPatterns

		uriInfo = uriInfo.substring(0, uriInfo.lastIndexOf("/"));

		try {
			return conn.hasStatement(ValueFactoryImpl.getInstance().createURI(uriInfo),
					ValueFactoryImpl.getInstance().createURI(STEP.interactionPattern.getLabel()),
					ValueFactoryImpl.getInstance().createURI(STEP.FLSVisitourAPI.getLabel()), true);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

}