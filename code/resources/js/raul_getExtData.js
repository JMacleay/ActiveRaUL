function refExtData(inputID, fromDomain, hasClass, hasPredicate, hasOntology){
	var inputString;
    inputString = jQuery("input[id='" + inputID + "']").val();
	inputString = Trim(inputString, '');
	if(inputString.search("http") == 0){
		getExtData(inputString);
	}
	else if(
			(inputString.length > (".rdf".length)) &&
			((inputString.length - (".rdf".length) - inputString.indexOf(".rdf")) == 0)
	){
		getExtData(inputString);
	}
	else{
		//queryExtData(inputString, fromDomain, hasClass, hasPredicate, hasOntology);
		sindiceSearchWindow('keywordSearch', inputID);
	}
}

function queryExtData(keyWord, fromDomain, hasClass, hasPredicate, hasOntology){

    //var queryString, keyWord, hasClass, hasPredicate, ExtURI;
	var queryString, ExtURI;
	showProcessingInfo();
	
	if(fromDomain != "")
		fromDomain = "domain:" + fromDomain;
	if(hasClass != "")
		hasClass = "class:" + hasClass;
	
	if(hasPredicate != ""){
		hasPredicate = Trim(hasPredicate, 'g');
		var aPrdicate = hasPredicate.split(',');
		hasPredicate = "";
		for (var i = 0; i < aPrdicate.length; i++) {
			hasPredicate = hasPredicate + "predicate:" + aPrdicate[i] + " ";
		}
		hasPredicate = Trim(hasPredicate, '');
	}
	
    queryString = "http://api.sindice.com/v3/search?q=" + keyWord +"&fq=" + fromDomain + " " + hasClass + " " + hasPredicate + " " + "&format=rdfxml";
	
	jQuery.ajax({
				beforeSend: function(req){req.setRequestHeader("Accept", "application/rdf+xml");},
				type: 'GET',
				url: queryString,				
				dataType: 'xml',
				success: function(data, textStatus, xhr){
					var numResults = jQuery(data).find("totalResults").text();
					if(numResults != "0"){
						/*
						ExtURI = jQuery(data).find("link").attr("rdf:resource")
						jQuery.get("proxy.php?url=" + ExtURI, function(Extdata) {		
							document.getElementById('showrdf').innerHTML = Extdata;
							parseData(Extdata);
						});
						*/
						ExtURI = jQuery(data).find("cache:first").text();
						jQuery.ajax({
								beforeSend: function(req){req.setRequestHeader("Accept", "application/rdf+xml");},
								type: 'GET',
								url: ExtURI,
								dataType: 'xml',
								success: function(data, textStatus, xhr){
										//document.getElementById('showrdf').innerHTML = data;
										//parseSindiceCache(data);
										var rdfString = (new XMLSerializer()).serializeToString(data);
										parseData(rdfString);
										rdfString = formatXml(rdfString);
										editor.setValue(rdfString);
										hideProcessingInfo();
								}
						});
					}
					else{
						hideProcessingInfo();
						alert("Your query criteria return no matched results. Please try again ...");
					}
				}
	});
}

function getExtData(dataURI){
	jQuery.get("proxy.php?url=" + dataURI, function(data) {		
		data = formatXml(data);
		document.getElementById('showrdf').innerHTML = data;		
		editor.setValue(data);
		if(dataURI.indexOf("#") == -1)
			parseData(data, "");
		else
			parseData(data, dataURI);
	});
}

//function parseData(data){
function parseData(data, main_subjectURI){

	var parser = new DOMParser();
	var xmlDocument = parser.parseFromString(data, "text/xml");
	var about = "", name = "", id = "", predValue = "", objectValue = "";
	var main_subject;
		
	jQuery("[typeof='raul:Textbox']").each(
		function(index, el){
			about="", name="", id="", predValue ="", objectValue="";
			var groupSubject = "";
			jQuery(el).find("span[property=raul:value]").each(
				function(index1, el1){
					about = jQuery(el1).text();
					
					predValue = jQuery("[about='"+ about +"']").children("[property='rdf:predicate']").text();			
					
					if( jQuery(el).find("span[property=raul:group]").length > 0 ){
						var i = groupOrder(jQuery(el));
						if(jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").length > 0){							
							var groupRoot = jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").eq(i);
							
							var pseudoNodeRoot = findPseudoNodeRoot(groupRoot, xmlDocument);
							if( !(isEmpty(pseudoNodeRoot)) ){
								groupRoot = pseudoNodeRoot;
								
								if( !(isEmpty(groupRoot.attr("rdf:about"))) )
									groupSubject = groupRoot.attr("rdf:about");
								//else if( !(isEmpty(groupRoot.attr("rdf:nodeID"))) )
								//	groupSubject = groupRoot.attr("rdf:nodeID");
								
							}
							
							if(groupRoot.find("[nodeName=" + predValue +"]").length > 0){
								objectValue = groupRoot.find("[nodeName=" + predValue +"]").first().text();
								if(objectValue == ""){					
									objectValue = groupRoot.find("[nodeName=" + predValue +"]").first().attr("rdf:resource");
								}
								objectValue = Trim(objectValue, '');
								
								/*
								if( (groupSubject == "") && (predValue == "rdfs:seeAlso") )
									groupSubject = objectValue;
								*/
							}
							
							if(groupSubject != ""){
								updateGroupSubjectValue(groupSubject, jQuery("[about='"+ about +"']").children("[property='rdf:subject']").text());						
							}
							
						}				
					}
					else{					
						if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").length > 0){
							if(main_subjectURI != ""){
								if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().parent().attr("rdf:about") == main_subjectURI){
									objectValue = findValue(xmlDocument, predValue);
								}
							}
							else{
								objectValue = findValue(xmlDocument, predValue);
							}
						}
					}
					jQuery("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
				}
			);	
			
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();
			
			jQuery("input[name='" + name + "'],input[id='" + id + "']").val(objectValue);
			jQuery("textarea[name='" + name + "'],input[id='" + id + "']").text(objectValue);
		}
	);
	
	jQuery("[typeof='raul:Radiobutton']").each(
		function(index, el){			
			about="", name="", id="", predValue ="", objectValue="";
			
			about = jQuery(el).find("span[property=raul:value]").text();
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();
				
			predValue = jQuery("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = jQuery("[about='"+ about +"']").children("[property='rdf:object']").text();
						
			var checkedValue = "";
			if( jQuery(el).find("span[property=raul:group]").length > 0 ){
				var i = groupOrder(jQuery(el));
				if(jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").length > 0){
					var groupRoot = jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").eq(i);
					
					var pseudoNodeRoot = findPseudoNodeRoot(groupRoot, xmlDocument);
					if( !(isEmpty(pseudoNodeRoot)) )
						groupRoot = pseudoNodeRoot;
					
					if(groupRoot.find("[nodeName=" + predValue +"]").length > 0){
						checkedValue = groupRoot.find("[nodeName=" + predValue +"]").first().text();
						if( checkedValue != ""){
							checkedValue = Trim(checkedValue, '');
							if(checkedValue == objectValue){
								jQuery(el).find("span[property=raul:checked]").text("true");
								document.getElementById(id).checked=true;				
							}
							else{
								jQuery(el).find("span[property=raul:checked]").text("false");
								document.getElementById(id).checked=false;
							}
						}
					}
				}
			}
			else{
				if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").length > 0){
					if(main_subjectURI != ""){
						if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().parent().attr("rdf:about") == main_subjectURI){
							checkedValue = findValue(xmlDocument, predValue);
						}
					}
					else{
						checkedValue = findValue(xmlDocument, predValue);
					}
										
					if(checkedValue == objectValue){
						jQuery(el).find("span[property=raul:checked]").text("true");
						document.getElementById(id).checked=true;				
					}
					else{
						jQuery(el).find("span[property=raul:checked]").text("false");
						document.getElementById(id).checked=false;
					}
					
				}
			}
		}
	);
	
	jQuery("[typeof='raul:Checkbox']").each(
		function(index, el){			
			about="", name="", id="", predValue ="", objectValue="";
			
			about = jQuery(el).find("span[property=raul:value]").text();
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();
				
			predValue = jQuery("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = jQuery("[about='"+ about +"']").children("[property='rdf:object']").text();
			
			var selectedValue = "";
			if( jQuery(el).find("span[property=raul:group]").length > 0 ){
				var i = groupOrder(jQuery(el));
				if(jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").length > 0){
					var groupRoot = jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").eq(i);
					
					var pseudoNodeRoot = findPseudoNodeRoot(groupRoot, xmlDocument);
					if( !(isEmpty(pseudoNodeRoot)) )
						groupRoot = pseudoNodeRoot;
					
					if(groupRoot.find("[nodeName=" + predValue +"]").length > 0){
						groupRoot.find("[nodeName=" + predValue +"]").each(
							function(i, ele){
								if( jQuery(ele).text() != ""){
									selectedValue = Trim(jQuery(ele).text(), '');
									if(selectedValue == objectValue){
										jQuery(el).find("span[property=raul:checked]").text("true");
										document.getElementById(id).checked=true;				
									}
									else{
										jQuery(el).find("span[property=raul:checked]").text("false");
										document.getElementById(id).checked=false;
									}
								}
							}
						);
					}
				}
			}
			else{
				if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").length > 0){
					jQuery(xmlDocument).find("[nodeName=" + predValue +"]").each(
						function(i, ele){
							if( jQuery(ele).text() != ""){
								//selectedValue = Trim(jQuery(ele).text(), '');
								if(main_subjectURI != ""){
									if(jQuery(ele).parent().attr("rdf:about") == main_subjectURI){
										selectedValue = Trim(jQuery(ele).text(), '');
									}
								}
								else{
									selectedValue = Trim(jQuery(ele).text(), '');
								}								
								if(selectedValue == objectValue){
									jQuery(el).find("span[property=raul:checked]").text("true");
									document.getElementById(id).checked=true;				
								}
								else{
									jQuery(el).find("span[property=raul:checked]").text("false");
									document.getElementById(id).checked=false;
								}
							}
							
						}
					);
				}
			}			
		}
	);
	
	jQuery("[typeof='raul:Listbox']").each(
		function(index, el){			
			about="", name="", id="", predValue ="", objectValue="";
			
			about = jQuery(el).find("span[property=raul:value]").text();
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();
				
			predValue = jQuery("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			
			if( jQuery(el).find("span[property=raul:group]").length > 0 ){
				var i = groupOrder(jQuery(el));
				if(jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").length > 0){
					var groupRoot = jQuery(xmlDocument).find("[nodeName=" + groupPred +"]").eq(i);
					
					var pseudoNodeRoot = findPseudoNodeRoot(groupRoot, xmlDocument);
					if( !(isEmpty(pseudoNodeRoot)) )
						groupRoot = pseudoNodeRoot;
					
					if(groupRoot.find("[nodeName=" + predValue +"]").length > 0){
						objectValue = groupRoot.find("[nodeName=" + predValue +"]").first().text();
						if( objectValue != ""){
							objectValue = Trim(objectValue, '');
						}
						else{
							objectValue = jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().attr("rdf:resource");
							if(objectValue != ""){
								objectValue = Trim(objectValue, '');
							}
						}
					}
				}
			}
			else{
				if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").length > 0){
					if(main_subjectURI != ""){
						if(jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().parent().attr("rdf:about") == main_subjectURI){
							objectValue = findValue(xmlDocument, predValue);
						}
					}
					else{
						objectValue = findValue(xmlDocument, predValue);
					}
				}
			}
			
			jQuery("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);			
			jQuery("select[name='" + name + "'],select[id='" + id + "']").children("option[value='" + objectValue + "']").attr('selected', true);
		}
	);
	
}

function findValue(xmlDocument, predValue){
	Value = jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().text();
	if(Value == ""){					
		Value = jQuery(xmlDocument).find("[nodeName=" + predValue +"]").first().attr("rdf:resource");
	}
	Value = Trim(Value, '');
	return Value;
}

function groupOrder(ele){
	var group = "", groupVal = "", value_G = "", pred_G = "", i = -1;
	group = ele.find("span[property=raul:group]").text();
	groupVal = jQuery("div[about='" + group + "']").find("span[property=raul:value]").text();
	groupPred = jQuery("div[about='" + groupVal + "']").find("span[property=rdf:predicate]").text();
		
	jQuery("[typeof='raul:Group']").each(
		function(counter, ele){
			value_G = jQuery(ele).find("span[property=raul:value]").text();
			pred_G = jQuery("div[about='" + value_G + "']").find("span[property=rdf:predicate]").text();

			if(pred_G == groupPred){
				i++;
			}
			if(value_G == groupVal){
				return false;
			}		
		}
	);	
	
	return i;
}

function findPseudoNodeRoot(groupRoot, xmlDocument){
	var pseudoNodeRoot;
	if( !(isEmpty(groupRoot.attr("rdf:resource"))) ){		
		jQuery(xmlDocument).find("[nodeName='rdf:Description']").each(function(index2, el2){
			if(jQuery(el2).attr("rdf:about") == groupRoot.attr("rdf:resource")){
				pseudoNodeRoot = jQuery(el2);
				return false;
			}
		});		
	}
	else if( !(isEmpty(groupRoot.attr("rdf:nodeID"))) ){		
		jQuery(xmlDocument).find("[nodeName='rdf:Description']").each(function(index2, el2){
			if(jQuery(el2).attr("rdf:nodeID") == groupRoot.attr("rdf:nodeID")){
				pseudoNodeRoot = jQuery(el2);
				return false;
			}
		});		
	}
	return pseudoNodeRoot;
}

function updateGroupSubjectValue(groupSubject, originalGroupSubject){
	jQuery("span[property=rdf:subject]").each(function(index, el){
			if(jQuery(el).text() == originalGroupSubject){
				jQuery(el).text(groupSubject);
			}
	});
	
	jQuery("span[property=rdf:object]").each(function(index, el){
			if(jQuery(el).text() == originalGroupSubject){
				jQuery(el).text(groupSubject);
			}
	});
	
}