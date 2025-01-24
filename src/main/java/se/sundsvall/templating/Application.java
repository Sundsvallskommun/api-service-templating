package se.sundsvall.templating;

import static org.springframework.boot.SpringApplication.run;

import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
public class Application {

	public static void main(String[] args) {
		System.setProperty("javax.xml.transform.TransformerFactory",
			"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

		run(Application.class, args);
	}
}
