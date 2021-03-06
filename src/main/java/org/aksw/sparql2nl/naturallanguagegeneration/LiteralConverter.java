package org.aksw.sparql2nl.naturallanguagegeneration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

public class LiteralConverter {
	
	private static final Logger logger = Logger.getLogger(LiteralConverter.class);

	private URIConverter uriConverter;

	public LiteralConverter(URIConverter uriConverter) {
		this.uriConverter = uriConverter;
	}

	public String convert(Literal lit) {
		return convert(NodeFactory.createLiteralNode(lit.getLexicalForm(), lit.getLanguage(),
				lit.getDatatypeURI()).getLiteral());
	}

	public String convert(LiteralLabel lit) {
		RDFDatatype dt = lit.getDatatype();

		String s = lit.getLexicalForm();
		if (dt == null) {// plain literal, i.e. omit language tag if exists
			s = lit.getLexicalForm();
		} else {// typed literal
			if (dt instanceof XSDDatatype) {// built-in XSD datatype
				if (dt.equals(XSDDatatype.XSDdate) || dt.equals(XSDDatatype.XSDdateTime)) {
					try {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD");
						Date date = simpleDateFormat.parse(s);
						DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
						String newDate = format.format(date);
						s = newDate;
					} catch (ParseException e) {
						logger.error("Could not parse literal with date datatype: " + lit, e);
					}
				}
			} else {// user-defined datatype
				s = lit.getLexicalForm() + " " + splitAtCamelCase(uriConverter.convert(dt.getURI(), false));
			}
		}
		return s;
	}
	
	public boolean isPlural(LiteralLabel lit){
		boolean singular = false;
    	double value = 0;
    	try {
			value = Integer.parseInt(lit.getLexicalForm());
			singular = (value == 0d);
		} catch (NumberFormatException e) {
			try {
				value = Double.parseDouble(lit.getLexicalForm());
				singular = (value == 0d);
			} catch (NumberFormatException e1) {
				
			}
		}
    	boolean isPlural = (lit.getDatatypeURI() != null) && !(lit.getDatatype() instanceof XSDDatatype) && !singular;
    	return isPlural;
	}
	
	private String splitAtCamelCase(String s){
		String regex = "([a-z])([A-Z])";
        String replacement = "$1 $2";
        return s.replaceAll(regex, replacement).toLowerCase();
	}

	public static void main(String[] args) {
		LiteralConverter conv = new LiteralConverter(new URIConverter(
				SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		LiteralLabel lit = NodeFactory.createLiteralNode("123", null,
				"http://dbpedia.org/datatypes/squareKilometre").getLiteral();
		System.out.println(lit);
		System.out.println(conv.convert(lit));
		
		lit = NodeFactory.createLiteralNode("1999-10-24", null,
				XSD.date.getURI()).getLiteral();
		System.out.println(lit);
		System.out.println(conv.convert(lit));
		
	}

}
