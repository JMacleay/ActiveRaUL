jQuery(document).ready(
	var formDefString, formURI, htmlForm, dataRDF, dataURI, htmlData;
	
	initRaulForntEnd();
	
	/* Use "formDefString" to store a form definition */
	
	formURI = postForm(formDefString);	//post the form definition
	
	htmlForm = getForm(formURI);	//retrive the deployed form (xhtml+RDFa)
			
	/* display "htmlForm" in a <div> whose ID is "content" */
                           
	dataRDF = parseDom("content");	//parse user input data
	rdfString = dataRDF.databank.dump({format:'application/rdf+xml', serialize: true});
	
	dataURI = postData(formURI, rdfString);	//post instance data
	htmlData = getData(dataURI);	//retrive the sumbitted data
	
);
