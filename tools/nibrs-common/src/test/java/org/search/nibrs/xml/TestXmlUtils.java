package org.search.nibrs.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.search.nibrs.xml.NibrsNamespaceContext.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestXmlUtils {
	
	@Test
	public void testAppendChildElement() throws Exception {
		
		Document d = XmlUtils.createNewDocument();
		assertNotNull(d);
		Element child1 = XmlUtils.appendChildElement(d, Namespace.nibrs, "child1");
		String contents = "Child 1 contents";
		child1.setTextContent(contents);
		XmlUtils.appendChildElement(child1, Namespace.nibrs, "child2");
		Node c1 = XmlUtils.xPathNodeSearch(d, "/" + Namespace.nibrs.prefix + ":child1");
		assertNotNull(c1);
		assertEquals(contents, c1.getTextContent());
		Node c2 = XmlUtils.xPathNodeSearch(d, "/" + Namespace.nibrs.prefix + ":child1/" + Namespace.nibrs.prefix + ":child2");
		assertNotNull(c2);
	}

}