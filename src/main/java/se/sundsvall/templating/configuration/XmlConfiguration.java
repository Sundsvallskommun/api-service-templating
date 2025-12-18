package se.sundsvall.templating.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlConfiguration {

	// There is a dependency conflict between logbook and docx4j. This solves the issue.
	static {
		System.setProperty("javax.xml.transform.TransformerFactory",
			"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}
}
