/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.nisgs.nsls.util;

import  org.w3c.dom.Node;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  java.lang.Exception;

// import the JAXP APIs  

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
<P>A container class for utility functions for creating and navigating
DOM structures.</P>
<P>&lt;RANT></P>
If Java would let you add methods to an existing class, junk like this
would be unnecessary.
&,t;/RANT></P>
 */

public class DOMUtil {

    /**
       Reads an XML document and returns the root node
       (which is all you need 99% of the time)
    */
    public static Node readXMLFile(String filename) throws Exception
    {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setValidating( false );
			
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document document = null;

	// simplify exceptions because we are only interested in parsing
	// errors. 		
	try
	    {
		document = builder.parse( new File(filename) );
	    }
	catch( SAXParseException spe ){
			
	    String errtxt = 
		"parse error in " + spe.getSystemId()   +
		",\nline: " + spe.getLineNumber() + " " + spe.getMessage();

	    throw new IllegalArgumentException( errtxt );
	}
			
	return document.getDocumentElement();
    }

    /**
       Boolean testing whether or not the node in question is an Element
       with a single child text node.
    */
    public static boolean hasNodeText(Node n) {
	return
	    (n.getNodeType() == Node.ELEMENT_NODE
	     && n.hasChildNodes()
	     && n.getFirstChild().getNodeType() == Node.TEXT_NODE
	     && n.getFirstChild().equals(n.getLastChild()));
    }

    /**
       Fishes an expected single child text node out of its Element Node
       argument as a String.  Throws a hissy fit^W^W^Wan Exception if it
       isn't just a single text node child.

       @param n Node to eviscerate.
    */
    public static String getNodeText(Node n) throws Exception {
	if(DOMUtil.hasNodeText(n))
	    return n.getFirstChild().getNodeValue();
	else
	    throw new Exception("Node " + n.getNodeName() + " is not a simple text container");
    }

    /** Returns the String that is the attribute of Node <CODE>n</CODE>
	named by <CODE>attname</CODE> or "" if that attribute doesn't exist.
	Throws an Exception if <CODE>n</CODE> isn't an Element.

	@param n Node to query
	@param as String naming the attribute
    */
    public static String getNodeAttribute(Node n, String as) throws Exception {
	return ((Element)n).getAttribute(as);
    }

    /**
     Simple depth-first recursive search for a Node of a given name.
     It's ridiculous
     that something like this isn't a Node standard function.

     @param node The node being searched
     @param name String of the name we're looking for
     @return The first node with that name, or <code>null</code>.
     */
    public static Node find_node(Node node, String name) {
	if(name.equals(node.getNodeName()))
	    return node;
	for(Node child = node.getFirstChild();
	    child != null;
	    child = child.getNextSibling()) {
	    Node result = find_node(child, name);
	    if(result != null) return result;
	}
	return null;
    }

    /** 
     Insists that for every direct child node of src, there is a
     corresponding direct child node of dst.  Throws an Exception otherwise.
     @param src Node containing list of tags
     @param dst Node containing list of tags with values
     */
    public static void match_nodes(Node src, Node dst) throws Exception {
	for(Node schild = src.getFirstChild();
	    schild != null;
	    schild = schild.getNextSibling()){
	    String scname = schild.getNodeName();
	    if(!scname.startsWith("#")) {
		if(find_node(dst, scname) == null)
		    throw new Exception("missing tag " + scname);
	    }
	}
    }

    /**
     * Very well, we'll define Iterators for Element children
     * of Nodes which happen to be elements
     */
    static class NodeIterator
	implements Iterator<Node>
    {
	// null means don't care - want all children
	NodeList nl;
	int i;

	public NodeIterator(Node parent, String nodeName) {
	    if(nodeName == null)
		nodeName = "*";
	    nl = ((Element)parent).getElementsByTagName(nodeName);
	    i = 0;
	}

	public boolean hasNext() {
	    return i < nl.getLength();
	}

	public Node next() {
	    if(i >= nl.getLength())
		throw new NoSuchElementException("No more children");
	    return nl.item(i++);
	}

	public void remove() {
	    throw new UnsupportedOperationException("NodeIterator");
	}
    }
    /**
     * And an Iterable class to allow Java to foreach it...
     */
    static class NodeIterable implements Iterable<Node>
    {
	String nodeName;
	Node parent;

	public NodeIterable(Node parent, String nodeName)
	{
	    this.parent = parent;
	    this.nodeName = nodeName;
	}
	public Iterator<Node> iterator()
	{
	    return new NodeIterator(parent, nodeName);
	}
    }

    /**
     * Finally, the real functions we want!  Two static functions that
     * call the appropriate constructors to return correctly created
     * Iterables!
     */
    public static Iterable<Node> Children(Node parent, String nodeName)
    {
	return new NodeIterable(parent, nodeName);
    }

    public static Iterable<Node> Children(Node parent)
    {
	return new NodeIterable(parent, null);
    }

    /**
     * Takes a Node and returns a String which is the XML rendering
     * of the Node.  Code copied from StackOverflow - hope it works...
     */
    public static String node2String(Node node) throws Exception
    {
	StringWriter writer = new StringWriter();
	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	transformer.transform(new DOMSource(node), new StreamResult(writer));
	String xml = writer.toString();
	return xml;
    }
}
