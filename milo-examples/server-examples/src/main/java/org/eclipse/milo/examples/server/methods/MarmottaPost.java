package org.eclipse.milo.examples.server.methods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
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
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.util.AbstractResourceUriGenerator;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.apache.marmotta.platform.ldp.util.RandomUriGenerator;
import org.apache.marmotta.platform.ldp.util.SlugUriGenerator;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.Out;
import org.eclipse.recommenders.jayes.BayesNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.aifb.step.api.AbstractSemanticStateBasedResource;
import edu.kit.aifb.step.api.SemanticStateBasedResource;
import edu.kit.aifb.step.resources.FlsVisitourCallInstance;
import edu.kit.aifb.step.vocabs.STEP;
import edu.kit.aifb.step.wrapper.Utils;

public class MarmottaPost {
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
        	
        @UaInputArgument(
        	name = "accept-type")
           	String accept_type,

        @UaInputArgument(
            name = "slug")
           	String slug,
 
        @UaInputArgument(
            name = "Content-Type")
           	String type,           	
        @UaOutputArgument(
            name = "OutputBody",
            description = "Information about the creation")
            Out<String> outputBody) {
    	
    	//initialize();
    	
		interactionPatterns.put(STEP.BayesNode, "BayesNode");
		interactionPatterns.put(STEP.FLSVisitourAPI, "edu.kit.aifb.step.resources.FlsVisitourCallContainer");
		interactionPatterns.put(STEP.StartAPI, "edu.kit.aifb.step.linkeddatawebservice.LinkedDataWebService");

		InputStream stream = new ByteArrayInputStream(InputBody.getBytes(StandardCharsets.UTF_8));
		
		final String container = resource;
		
		RDFFormat format = RDFFormat.TURTLE;

		final RepositoryConnection conn = sesameService.getConnection();
		try {
			conn.begin();

			if (!ldpService.exists(conn, container)) {
				final Response.ResponseBuilder resp;
				if (ldpService.isReusedURI(conn, container)) {
					outputBody.set("Reused URI");
				} else {
					outputBody.set("Not existing URI");				}
				conn.rollback();			}
			/*
			 * sba:
			 */
			else {

				//updateStatistics(container);
			}

			/*
			 * sba:
			 */
			if ( ldpService.isStartAPI(conn, container) ) {

				// RepositoryResult<Statement> statements = conn.getStatements(
				// ValueFactoryImpl.getInstance().createURI(resource),
				// ValueFactoryImpl.getInstance().createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				// null, true, new Resource[0]);

				//final Response.ResponseBuilder resp = createWebServiceResponse(conn, 200, container, postBody, format);

				conn.commit();
				outputBody.set("To be done");

			}

			if (ldpService.isVirtualResource(conn, container)) {

				// =============================================================================================
				//
				// is a FLSResource
				//
				// =============================================================================================
				RepositoryResult<Statement> neededPatterns = conn.getStatements(
						ValueFactoryImpl.getInstance().createURI(resource),
						ValueFactoryImpl.getInstance().createURI(STEP.interactionPattern.getLabel()), null, true);

				while (neededPatterns.hasNext()) {
					Statement neededClass = neededPatterns.next();
					try {
						outputBody.set(Response.Status.CREATED.toString());
						Class cls_Test = Class.forName(interactionPatterns
								.get(new org.semanticweb.yars.nx.Resource(neededClass.getObject().toString())));
						Constructor co = cls_Test.getConstructor(String.class);
						SemanticStateBasedResource res = (SemanticStateBasedResource) co.newInstance(resource);
						if(res.create(postBodyToIterableNode(stream, new URIImpl(resource))) == null)
							outputBody.set(Response.Status.METHOD_NOT_ALLOWED.toString());

						conn.commit();

					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// Check that the target container supports the LDPC Interaction Model
			final LdpService.InteractionModel containerModel = ldpService.getInteractionModel(conn, container);
			if (containerModel != LdpService.InteractionModel.LDPC) {
				outputBody.set(Response.Status.METHOD_NOT_ALLOWED.toString());
				conn.commit();
			}

			// Get the LDP-Interaction Model (Sec. 5.2.3.4 and Sec. 4.2.1.4)
			//final LdpService.InteractionModel ldpInteractionModel = ldpService.getInteractionModel(linkHeaders);

			if (ldpService.isNonRdfSourceResource(conn, container)) {
				outputBody.set(Response.Status.METHOD_NOT_ALLOWED.toString());

				conn.commit();
			}

			final AbstractResourceUriGenerator uriGenerator;
			if (StringUtils.isBlank(slug)) {
				/* Sec. 5.2.3.8) */
				uriGenerator = new RandomUriGenerator(ldpService, container, conn);
			} else {
				// Honor client wishes from Slug-header (Sec. 5.2.3.10)
				// http://www.ietf.org/rfc/rfc5023.txt
				uriGenerator = new SlugUriGenerator(ldpService, container, slug, conn);
			}

			final String newResource = uriGenerator.generateResourceUri();

			// connection is closed by buildPostResponse
			outputBody.set(buildPostResponse(conn, container, newResource, stream, type));
			// return buildPostResponse(conn, container, newResource, ldpInteractionModel,
			// null, type);
		} catch (IllegalArgumentException e) {
			conn.commit();
			outputBody.set(e.getMessage());
		} catch (MissingArgumentException e) {
			conn.commit();
			outputBody.set(e.getMessage());
		} catch (RemoteException e) {
			conn.commit();
			outputBody.set(e.getMessage());
		} catch (InvocationTargetException e) {
			conn.commit();
			outputBody.set(e.getMessage());
		} catch (IllegalAccessException e) {
			conn.commit();
			outputBody.set(e.getMessage());
		} catch (final Throwable t) {
			conn.rollback();
			throw t;
		} finally {
			conn.close();
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
    
	public List<Node[]> postBodyToIterableNode(InputStream postBody, URI resource) {
		Map<BayesNode, String> evidence = new HashMap<BayesNode, String>();

		// TurtleParser turtleParser = new TurtleParser(input_nodes,
		// Charset.defaultCharset(), new java.net.URI( resource.stringValue() ) );
		RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
		org.openrdf.model.Graph myGraph = new org.openrdf.model.impl.GraphImpl();
		StatementCollector collector = new StatementCollector(myGraph);
		rdfParser.setRDFHandler(collector);

		try {
			rdfParser.parse(postBody, resource.stringValue());
		} catch (RDFParseException | RDFHandlerException | IOException e) {

			List<Node[]> error = new LinkedList<Node[]>();
			error.add(new org.semanticweb.yars.nx.Node[] { new org.semanticweb.yars.nx.BNode("You"), RDFS.LABEL,
					new org.semanticweb.yars.nx.Literal("failed!") });
		}

		List<org.semanticweb.yars.nx.Node[]> input_nodes = new LinkedList<org.semanticweb.yars.nx.Node[]>();

		myGraph.forEach(s -> {
			try {

				if (s.getObject() instanceof org.openrdf.model.Resource) {
					Node[] node = { new org.semanticweb.yars.nx.Resource(s.getSubject().toString()),
							new org.semanticweb.yars.nx.Resource(s.getPredicate().toString()),
							new org.semanticweb.yars.nx.Resource(s.getObject().toString()) };
					input_nodes.add(node);
				} else {
					org.semanticweb.yars.nx.Node[] node = {
							new org.semanticweb.yars.nx.Resource(s.getSubject().toString()),
							new org.semanticweb.yars.nx.Resource(s.getPredicate().toString()),
							new org.semanticweb.yars.nx.Literal(s.getObject().stringValue()) };
					input_nodes.add(node);
				}
			} catch (ClassCastException e) {
				org.semanticweb.yars.nx.Node[] node = { new org.semanticweb.yars.nx.Resource(s.getSubject().toString()),
						new org.semanticweb.yars.nx.Resource(s.getPredicate().toString()),
						new org.semanticweb.yars.nx.Literal(s.getObject().stringValue()) };
				input_nodes.add(node);
			}
		});

		return input_nodes;
	}
	
	private String buildPostResponse(RepositoryConnection connection, String container, String newResource,
			InputStream requestBody, String type)
					throws RepositoryException {
		final String mimeType = type;
		// checking if resource (container) exists is done later in the service
		try {
			String location = ldpService.addResource(connection, container, newResource, LdpService.InteractionModel.LDPC, mimeType,
					requestBody);

			connection.commit();
			 return Response.Status.CREATED.toString();
		} catch (IOException | RDFParseException e) {
			connection.rollback();
			return e.getMessage();
		} catch (UnsupportedRDFormatException e) {
			connection.rollback();
			return e.getMessage();
		}
	}

}