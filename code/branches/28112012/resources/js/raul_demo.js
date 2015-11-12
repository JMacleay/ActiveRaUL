//Global variables (for demo) Declaration Start
var editor;
var processingInfo = "<h2 class=\"processing_info\" style=\"color:#939adc; display:none; position:absolute; right:10%;\">Processing ... <img src=\"img/processing_icon.gif\" /></h2>"
//Global variables (for demo) Declaration End

function createLink(targetURI){
	//return "<a href=\"#\" onclick=\"getRequestRDFWrap('" + targetURI + "');return false;\">" + targetURI + "</a><br/>" +
	//"<a href=\"#\" onclick=\"putTest('" + targetURI + "');return false;\"> putTest </a><br/>";
	return "<a href=\"#\" onclick=\"getRequestRDFWrap('" + targetURI + "');return false;\">" + targetURI + "</a><br/>";
}

function showProcessingInfo(){
	jQuery("h2[class='processing_info']").each(
		function(index, el){
			jQuery(el).css("display","inline");
		}
	);	
}

function hideProcessingInfo(){
	jQuery("h2[class='processing_info']").each(
		function(index, el){
			jQuery(el).css("display","none");
		}
	);	
}

function initTab(){
			jQuery(".jquery-tabs span:first").addClass("current");
			jQuery(".jquery-tabs ul:not(:first)").hide();
			jQuery(".jquery-tabs span").mouseover(function(){
				jQuery(".jquery-tabs span").removeClass("current");
				jQuery(this).addClass("current");
				jQuery(".jquery-tabs ul").hide();
				jQuery("."+jQuery(this).attr("id")).fadeIn("slow");
			});
	
}

function initSyntaxHighlight(){
	editor = CodeMirror.fromTextArea(document.getElementById("showrdf"), {
											lineNumbers: true,
											mode: "xml"
											});
}

function initDemo(){
	initTab();
	initSyntaxHighlight();
}

jQuery(document).ready(
	function(){
		initRaulFrontEnd();
		initDemo();
		
		jQuery("textarea#showrdf").load(rdfFormDef, function(){editor.setValue(jQuery("textarea#showrdf").val());});
		
		jQuery('#formcreate').click(function() {
			editor.save();
			showProcessingInfo();
			
			jQuery("input#serviceURL").val(postForm(document.getElementById('showrdf').value));
			
			//document.getElementById('content').innerHTML = processingInfo + getForm(jQuery("input#serviceURL").val());
			document.getElementById('content').innerHTML = 
			processingInfo + createLink(jQuery("input#serviceURL").val()) + 
			getForm(jQuery("input#serviceURL").val());
			
			hideProcessingInfo();
			
		});
		
		jQuery('#formupdate').click(function() {
			editor.save();
			showProcessingInfo();
			 
			var tmpURI = jQuery("input#formURI").val();
			tmpURI = getBaseURL() + tmpURI.substring(tmpURI.indexOf("raul/"));
			putForm(tmpURI, document.getElementById('showrdf').value);
            jQuery("input#serviceURL").val(tmpURI);
			
			document.getElementById('content').innerHTML = 
			processingInfo + createLink(jQuery("input#serviceURL").val()) + 
			getForm(jQuery("input#serviceURL").val());
			
			hideProcessingInfo();
			
		});
                           
		jQuery('#show').click(function() {
			log.info("show the parsed RDF")
			jQuery("#result").find("tr:gt(0)").remove();
			var rdf = parseDom("content");
	
			jQuery("#result").find("tr:gt(0)").remove();
			jQuery('#result tr:last').after('<tr><td style="font: bold;">subject</td><td>predicate</td><td>object</td></tr>'); 
			
			rdf.where('?s ?p ?o').each(function() {
						jQuery('#result tr:last').after('<tr><td>'+this.s.value+'</td><td>'+this.p.value+'</td><td>'+this.o.value+'</td></tr>');
			});
			return false;
		});
					
		jQuery('#logging').click(function() {log.toggle();});
	}
);

function postDataWrap(){
	jQuery("textarea#showrdf").val("");
	var rdf = parseDom("content");
	var rdfString = rdf.databank.dump({format:'application/rdf+xml', serialize: true});
	
	editor.setValue(formatXml(rdfString));
	
	showProcessingInfo();
	var dataURI = postData(jQuery("input#serviceURL").val(), rdfString);
	//document.getElementById('content').innerHTML = processingInfo + getData(dataURI);
	document.getElementById('content').innerHTML = processingInfo + createLink(dataURI) + getData(dataURI);
	hideProcessingInfo();
}

function getRequestRDFWrap(requestURI){
	jQuery("textarea#showrdf").val("");
	
	showProcessingInfo();
	var returnRDF = formatXml(getRequestRDF(requestURI));
	editor.setValue(returnRDF);
	//document.getElementById('content').innerHTML = processingInfo + returnRDF;
	hideProcessingInfo();
}

function submitDataWrap(){
	originalSubjectURI = jQuery("[property='rdf:subject']").first().text();
	originalSubjectKey = originalSubjectURI.substr(originalSubjectURI.lastIndexOf('/') + 1);
	
	jQuery("textarea#showrdf").val("");
	var rdf = parseDom("content");
	var rdfString = rdf.databank.dump({format:'application/rdf+xml', serialize: true});
	
	if(agentName == "ChromeSafari"){
		var regex=/raul:subject xmlns:raul=\"http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#\"/g;
		rdfString = rdfString.replace(regex, "rdf:subject");
		regex=/raul:predicate xmlns:raul=\"http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#\"/g;
		rdfString = rdfString.replace(regex, "rdf:predicate");
		regex=/raul:object xmlns:raul=\"http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#\"/g;
		rdfString = rdfString.replace(regex, "rdf:object");
		regex=/xmlns:raul=\"http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#\"/g;
		rdfString = rdfString.replace(regex, "");
		regex=/raul:_/g;
		rdfString = rdfString.replace(regex, "rdf:_");		
		regex=/raul:subject/g;
		rdfString = rdfString.replace(regex, "rdf:subject");
		regex=/raul:predicate/g;
		rdfString = rdfString.replace(regex, "rdf:predicate");
		regex=/raul:object/g;
		rdfString = rdfString.replace(regex, "rdf:object");
		
	}
	
	editor.setValue(formatXml(rdfString));	
	
	showProcessingInfo();
	
	var dataInstanceURI = "";
	if(originalSubjectKey == "defaultInstanceGraph"){
		dataInstanceURI = postData(jQuery("input#serviceURL").val(), rdfString);
		jQuery("input#serviceURL").val(dataInstanceURI)
	}
	else{
		dataInstanceURI = jQuery("input#serviceURL").val();
		putData(dataInstanceURI, rdfString);
	}
	
	document.getElementById('content').innerHTML = 
	processingInfo + createLink(dataInstanceURI) + getData(dataInstanceURI);
	hideProcessingInfo();
	
	/*
	methodURI = jQuery("[typeof='raul:WidgetContainer']").find("span[property=raul:method]").text();
	raulMethod = jQuery("[about='" + methodURI + "']").attr("typeof");
	
	editor.setValue(formatXml(rdfString));	
	
	showProcessingInfo();
	
	var dataInstanceURI = "";
	if(raulMethod == "raul:CREATEOperation"){
		dataInstanceURI = postData(jQuery("input#serviceURL").val(), rdfString);
		jQuery("input#serviceURL").val(dataInstanceURI)
	}
	else if(raulMethod == "raul:UPDATEOperation"){
		putData(dataInstanceURI, userInputData);
	}
	
	document.getElementById('content').innerHTML = 
	processingInfo + createLink(dataInstanceURI) + getData(dataInstanceURI);
	hideProcessingInfo();
	*/
}

