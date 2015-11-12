//Global variables Declaration Start
var serviceURI = "/raul/service/public/forms";
var postType = "application/rdf+xml";
var xmlDisplayingMethod = "view-source:data:text/html;,";
var agentName = "firefox";

//Global variables Declaration End

function initRaulFrontEnd(){
	agentType();
}

/*
function agentType()
Description:
	To detect the type of client agent.	
*/
function agentType(){
	var Sys = {};
    var ua = navigator.userAgent.toLowerCase();
    var s;
    (s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] :
    (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
    (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
    (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
    (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
	
    //if (Sys.ie) 
	//if (Sys.opera)
    if (Sys.firefox){
		postType = "application/rdf+xml";
		xmlDisplayingMethod = "view-source:data:text/html;,";
		agentName = "firefox";
	}
    if (Sys.chrome || Sys.safari){
		postType = "application/xml";
		xmlDisplayingMethod = "data:text/xml;,";
		agentName = "ChromeSafari";
	}
}

function getRequestRDF(requestURI){
	var rdfData;	
	jQuery.ajax({
			beforeSend: function(req){
							req.setRequestHeader("Accept", "application/rdf+xml");
						},
			type: 'GET',
			async: false,
			url: requestURI,
			dataType: 'xml',
			success: function(data, textStatus, xhr){	
						rdfData = (new XMLSerializer()).serializeToString(data);
						var newWindow = window.open(showXML(rdfData));
					}
	});	
	return rdfData;
}

function showXML(s){
  return xmlDisplayingMethod+escape(s);  
}

/*
function postForm(formDef)
Description:
	For creating a RaUL-based form (post a form definition).
Parameters: 
	formDef -- The form definition described in RDF/XML format (string type).
Retrun values:
	formURI -- The URI of the deployed form (assigned by ActiveRaUL service).	
*/
function postForm(formDef){
	var formURI;
	formDef = assignDefaultURI(formDef);
	jQuery.ajax({					
			type: 'POST',
			async: false,
			url: serviceURI,
			processData: false,			
			contentType: postType,
			data: formDef,
	
			success: function(data, textStatus, xhr){
						formURI = xhr.getResponseHeader('Location');
					}
	});
	return formURI;
}

function putForm(formURI, formDef){
	formDef = assignDefaultURI(formDef);
	jQuery.ajax({					
			type: 'PUT',
			async: false,
			url: formURI,
			processData: false,			
			contentType: postType,
			data: formDef,
	
			success: function(data, textStatus, xhr){
						//formURI = xhr.getResponseHeader('Location');
					}
	});
	//return formURI;
}

function assignDefaultURI(formDef){
	var parser = new DOMParser();
	var xmlDocument = parser.parseFromString(formDef, "text/xml");
	
	var defaultSubject;	
	//jQuery(xmlDocument).find("rdf\\:type").each(
	jQuery(xmlDocument).find("[nodeName=rdf:type]").each(		
		function(index, el){
			if(jQuery(el).attr("rdf:resource") == "http://purl.org/NET/raul#Page"){
				defaultSubject = jQuery(el).parent().attr("rdf:about") + "/defaultInstanceGraph";
				return false;
			}
		}
	);		
	jQuery(xmlDocument).find("[nodeName=rdf:subject]").text(defaultSubject);	
	
	var subjectOfGroupFields, listOfGroup, valueOfGroup;	
	jQuery(xmlDocument).find("[nodeName=rdf:type]").each(
		function(index, el){
			if(jQuery(el).attr("rdf:resource") == "http://purl.org/NET/raul#Group"){
				jQuery(el).siblings().each(
					function(i, ele){
						if(jQuery(ele).get(0).tagName == "raul:id"){
							subjectOfGroupFields = defaultSubject + "_" + jQuery(ele).text();
						}
						else if(jQuery(ele).get(0).tagName == "raul:list"){
							listOfGroup = jQuery(ele).text();
						}						
						else if(jQuery(ele).get(0).tagName == "raul:value"){
							valueOfGroup = jQuery(ele).text();
						}
					}
				);
				//update the object value of a group
				//jQuery(xmlDocument).find("rdf\\:object").each(
				jQuery(xmlDocument).find("[nodeName=rdf:object]").each(
					function(i, ele){
						if(jQuery(ele).parent().attr("rdf:about") == valueOfGroup){
							jQuery(ele).text(subjectOfGroupFields);
							return false;
						}	
					}
				);
				//update the subject value of fields of the group				
				updateSubjectOfGroup(jQuery(xmlDocument), subjectOfGroupFields, listOfGroup);
			}
		}
	);
	
	var serializer = new XMLSerializer();
	var xmlstring = serializer.serializeToString( xmlDocument ); 
	return xmlstring;
}

function updateSubjectOfGroup(xmlJqueryObject, subjectOfGroupFields, listOfGroup){
	var VoGF; //vaule URI of a field of a given group
	xmlJqueryObject.find("[nodeName=rdf:Description]").each(
		function(i, ele){
			if(jQuery(ele).attr("rdf:about") == listOfGroup){
				jQuery(ele).find("[nodeName!=rdf:type]").each(
						function(j, elem){							
							VoGF = findValueOfGroupField(xmlJqueryObject, jQuery(elem).attr("rdf:resource"), subjectOfGroupFields);
							updateSoG(xmlJqueryObject, VoGF, subjectOfGroupFields);
						}
				);
				return false;
			}	
		}
	);
}

function findValueOfGroupField(xmlJqueryObject, fieldURI, subjectOfGroupFields){
	var VoGF; //vaule URI of a field of a given group
	xmlJqueryObject.find("[nodeName=rdf:Description]").each(
		function(i, ele){
			if(jQuery(ele).attr("rdf:about") == fieldURI){
				VoGF = jQuery(ele).find("[nodeName=raul:value]").text();
				return false;
			}	
		}
	);
	return VoGF;
}

function updateSoG(xmlJqueryObject, VoGF, subjectOfGroupFields){
	xmlJqueryObject.find("[nodeName=rdf:Description]").each(
		function(i, ele){
			if(jQuery(ele).attr("rdf:about") == VoGF){
				jQuery(ele).find("[nodeName=rdf:subject]").text(subjectOfGroupFields);
				return false;
			}	
		}
	);
}

/*
function getForm(formURI)
Description:
	For retrieving an exist form.
Parameters: 
	formURI -- The form URI.
Return value:
	The form definition in XHTML/RDFa format.
*/
function getForm(formURI){	
	var htmlForm;
	jQuery.ajax({
			beforeSend: function(req){
							req.setRequestHeader("Accept", "application/xhtml+xml");
						},
			type: 'GET',
			async: false,
			url: formURI,
			dataType: 'html',
			success: function(data, textStatus, xhr){
						htmlForm = data;
					}
	});
	return htmlForm;
}

/*
function postData(formURI, data)
Description:
	For submitting user input data to the server.
Parameters: 
	formURI -- The form URI.
	userInputData -- The user input data.
Return value:
	dataURI -- The URI of the submitted data (assigned by ActiveRaUL service).	
*/
function postData(formURI, userInputData){	
	var dataURI;	
	jQuery.ajax({					
			type: 'POST',
			async: false,
			url: formURI,
			processData: false,			
			contentType: postType,
			data: userInputData,
	
			success: function(data, textStatus, xhr){
						dataURI = xhr.getResponseHeader('Location');
					}
	});
	return dataURI;
}

function putData(dataInstanceURI, userInputData){	
	//var dataURI;
	jQuery.ajax({					
			type: 'PUT',
			async: false,
			url: dataInstanceURI,
			processData: false,			
			contentType: postType,
			data: userInputData,
	
			success: function(data, textStatus, xhr){
						//dataURI = xhr.getResponseHeader('Location');
					}
	});
	//return dataURI;
}

/*
function getData(dataURI)
Description:
	To retrive form data.
Parameters: 
	dataURI -- The data URI.
Return value:
	The request data in XHTML/RDFa format.
*/
function getData(dataURI){
	var htmlFormData;	
	jQuery.ajax({
			beforeSend: function(req){
							req.setRequestHeader("Accept", "application/xhtml+xml");
						},
			type: 'GET',
			async: false,
			url: dataURI,
			dataType: 'html',
			success: function(data, textStatus, xhr){
						htmlFormData = data;
					}
	});	
	return htmlFormData;
}

/*
function deleteInstance(instanceURI)
Description:
	To delete a form/data instance.
Parameters: 
	instanceURI -- The form/data URI.
*/
function deleteInstance(instanceURI){
	jQuery.ajax({			
			type: 'DELETE',			
			url: instanceURI,			
			success: function(data, textStatus, xhr){
						alert("A form/data instance " + formURI + " was deleted.");
					}
	});
}

/*
parseDom(contentID)
Description:
	To parse the input data that provided by the end user during the runtime.
Parameters: 
	contentID -- Element ID of the <div> that contains and displays the rendered XHTML+RDFa form.
*/
function parseDom(contentID){
	
	var url="", sHKey="", sHKeyCount=0; //for the subject hash key generation
	var identifier="";
	
	log.profile("Updating raul:Textbox span elements");
	jQuery("[typeof='raul:Textbox']").each(
		function(index, el){			
			var about="", name="", id="", objecValue="";
			
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();			
			
			objectValue = jQuery("input[name='" + name + "'],input[id='" + id + "']").val();
			if(objectValue == null){
				objectValue = jQuery("textarea[name='" + name + "'],input[id='" + id + "']").text();
			}
			
			jQuery(el).find("span[property=raul:value]").each(
				function(index1, el1){
					about = jQuery(el1).text();
					jQuery("[about='"+ about +"']").children("[property='rdf:object']").text(Trim(objectValue, ''));
				}
			);
			
			//for the subject hash key generation
			var predValue ="";			
			predValue = jQuery("[about='"+ about +"']").children("[property='rdf:predicate']").text();      
			if((objectValue != "") && (predValue != "owl:sameAs")){
				if(jQuery(el).find("span[property=raul:isIdentifier]").text() == "true"){
					identifier = identifier + Trim(objectValue, 'g');
				}
				else if(sHKeyCount < 2){
					sHKey = sHKey + Trim(objectValue, 'g');    
					sHKeyCount++;
				}
			}
			/*
			if((sHKeyCount < 2) && (objectValue != "") && (predValue != "owl:sameAs")){
				sHKey = sHKey + Trim(objectValue, 'g');              
				sHKeyCount++;
		    }
			*/
		      //for the subject hash key generation
		}
	);	
	
	log.profile("Updating raul:Checkbox span elements");
	jQuery("[typeof='raul:Checkbox']").each(
		function(index, el){		
			var about="";
			var appendLocation;
			appendLocation = jQuery(el).find("span[property=raul:value]");
			about = appendLocation.text();
			jQuery(el).find("span[property=raul:checked]").remove();
			
			if(jQuery(":checkbox[value='" + about + "']").attr('checked') == true){
				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">true</span>");								
				
				var selectedLabel = jQuery(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				
				if(selectedLabel != ""){
					if(jQuery(el).find("span[property=raul:isIdentifier]").text() == "true"){
						identifier = identifier + selectedLabel;
					}
					else if(sHKeyCount < 2){
						sHKey = sHKey + selectedLabel;
						sHKeyCount++;
					}
				}
				/*
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;
				}
				*/
			}
			else{				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">false</span>");				
			}
		}
	);
		
	log.profile("Updating raul:Radiobutton span elements");
	jQuery("[typeof='raul:Radiobutton']").each(
		function(index, el){		
			var about="";
			var appendLocation;
			appendLocation = jQuery(el).find("span[property=raul:value]");
			about = appendLocation.text();
			jQuery(el).find("span[property=raul:checked]").remove();
			
			if(jQuery(":radio[value='" + about + "']").attr('checked') == true){				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">true</span>");								
								
				//for the subject hash key generation
				var selectedLabel = jQuery(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				
				if(selectedLabel != ""){
					if(jQuery(el).find("span[property=raul:isIdentifier]").text() == "true"){
						identifier = identifier + selectedLabel;
					}
					else if(sHKeyCount < 2){
						sHKey = sHKey + selectedLabel;
						sHKeyCount++;
					}
				}
				/*
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;
				}
				*/
				//for the subject hash key generation
			}
			else{				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">false</span>");								
				
			}
		}
	);
	
	log.profile("Updating raul:Listitem span elements");	
	jQuery("[typeof='raul:Listitem']").each(
		function(index, el){
			var about="";
			var appendLocation;
			appendLocation = jQuery(el).find("span[property=raul:value]");
			about = appendLocation.text();
			jQuery(el).find("span[property=raul:selected]").remove();
			
			//if(jQuery("option[value='" + about + "']").attr('selected') == true){				
			if(jQuery(el).nextAll().find("option[value='"+about+"']").attr('selected') == true){
				appendLocation.after("\n\t\t\t<span property=\"raul:selected\" datatype=\"xsd:boolean\">true</span>");
				
				//for the subject hash key generation
				var selectedLabel = jQuery(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				if(selectedLabel != ""){
					if(jQuery(el).find("span[property=raul:isIdentifier]").text() == "true"){
						identifier = identifier + selectedLabel;
					}
					else if(sHKeyCount < 2){
						sHKey = sHKey + selectedLabel;
						sHKeyCount++;
					}
				}
				/*
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;
				}
				*/
				//for the subject hash key generation
			}
			else{				
				appendLocation.after("\n\t\t\t<span property=\"raul:selected\" datatype=\"xsd:boolean\">false</span>");
				
			}			
		}
	);
	
	jQuery("[typeof='raul:Listbox']").each(
		function(index, el){			
			var about="", name="", id="", list_t="", objecValue="";
			var list;
			
			about = jQuery(el).find("span[property=raul:value]").text();
			if( jQuery(el).find("span[property=raul:name]").text() != "" )			
				name = jQuery(el).find("span[property=raul:name]").text();				
			if( jQuery(el).find("span[property=raul:id]").text != "" )
				id = jQuery(el).find("span[property=raul:id]").text();
			if( jQuery(el).find("span[property=raul:list]").text != "" )
				list_t = jQuery(el).find("span[property=raul:list]").text();			
			list = jQuery("ol[about='"+ list_t +"']").get(0);
				
			var resource;				
			for(var i=1; i<list.childNodes.length; i++){				
				if(list.childNodes[i].nodeType != 3 && list.childNodes[i].nodeType != 8){ 
					resource = list.childNodes[i].attributes['resource'].value;					
					if(jQuery("[about='"+ resource +"']").find("[property='raul:selected']").text() == "true"){
						objectValue = jQuery("[about='"+ resource +"']").find("[property='raul:value']").text();
						jQuery("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
						break;
					}					
				}
			}
		}
	);
	
			
	//for the subject hash key generation	
	var regex=/defaultInstanceGraph/g;
	var oldResourceURI = "", newResourceURI = "";
	var newURI = "";
	if(identifier != ""){newURI = identifier;}else{newURI = sHKey;}
	//jQuery("[property='rdf:subject']").each(
	jQuery("[property='rdf:subject'],[property='rdf:object']").each(
		function(index, el){						
			oldResourceURI = jQuery(el).text();
            newResourceURI = oldResourceURI.replace(regex, newURI);
            jQuery(el).text(newResourceURI);
		}
	);
	//for the subject hash key generation
	
	log.profile('parsing RDFa');	
	rdf = jQuery("#" + contentID).rdfa();
	log.profile('parsing RDFa');
	log.info("Number of statements: "+rdf.databank.size());
	
	rdf.prefix('raul', 'http://purl.org/NET/raul#');
	rdf.prefix('dcterms', 'http://purl.org/dc/terms/');

	return rdf;
}

function getBaseURL() {
    var url = location.href;  // entire url including querystring - also: window.location.href;
    var baseURL = url.substring(0, url.indexOf('/', 14));

    if (baseURL.indexOf('http://localhost') != -1) {
        // Base Url for localhost
        var url = location.href;  // window.location.href;
        var pathname = location.pathname;  // window.location.pathname;
        var index1 = url.indexOf(pathname);
        var index2 = url.indexOf("/", index1 + 1);
        var baseLocalUrl = url.substr(0, index2);

        return baseLocalUrl + "/";
    }
    else {
        // Root Url for domain name
        return baseURL + "/";
    }

}

/*
formatXml(xml)
Description: 
	To tidy a xml string.
Parameters:
	xml -- The input xml string.
Return values:
	formatted -- The formatted xml string.
*/
function formatXml(xml){
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>jQuery/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*jQuery/ )) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
}

/*
Trim(str,is_global)
Description: 
	To trim off unnecessary space in a given string.
Parameters:
	str -- The input string.
	is_global -- If the value of "is_global" is 'g'/'G', this function trim off all space in the given string (not only prefixed and postfixed space).
*/
function Trim(str, is_global){ 
	var result; 
	result = str.replace(/(^\s+)|(\s+jQuery)/g,""); 
	if(is_global.toLowerCase()=="g") 
		result = result.replace(/\s/g,""); 
	return result; 
}

function isEmpty(value){
    return (value === undefined || value == null || value.length <= 0) ? true : false;
}

/*
dynamicWidgetsAdd()
Description:
	To dynamically add widgets.
*/
function dynamicWidgetsAdd(uriDynamicGroup, buttonID){

    var numOfLists, firstList;

    numOfLists = jQuery("div[about='"+ uriDynamicGroup+"']").find("span[property=raul:list]").length;

    firstList = jQuery("[about='"+ uriDynamicGroup+"']").find("span[property=raul:list]").first().text();

    if(numOfLists >= 1){
        var newList = "\n\t\t<span property=\"raul:list\">" + firstList + "_" + numOfLists + "</span>";        
        jQuery("[about='"+ uriDynamicGroup +"']").find("span[property=raul:list]").last().after(newList);
        //insert new widgets before the "add button"
        insertList(firstList, numOfLists, buttonID);
    }
    
    //alert(jQuery("form").html());
}

/*
insertList(sourceList, numOfLists, buttonID)
Description:
	To add the list of new widgets.
Parameters:
	sourceList -- the new list is a copy of "sourceList" with new sequence number
	numOfList -- this argument is used to caculate the sequence number for the new list
	buttonID -- the location where we prepend (using before() API of jquery) the new list
*/
function insertList(sourceList, numOfLists, buttonID){    
	var newList;    
    newList = jQuery("ol[about='"+ sourceList +"']").clone();
    oldAbout = newList.attr("about");
    newList.attr("about", oldAbout + "_" + numOfLists);
    newList.find("li").each(
        function(index, el){
            oldResource = jQuery(el).attr("resource");
            jQuery(el).attr("resource", oldResource + "_" + numOfLists);
        }
    );
    jQuery("ol[about='" + sourceList + "']").after(newList);
    
    insertListItems(sourceList, numOfLists, buttonID);
}

function insertListItems(sourceList, numOfLists, buttonID){
    
    var uriNewItem, newString, oldItem, newItem;
	var buttonLocation = jQuery("input#" + buttonID);
	
    jQuery("ol[about='"+ sourceList +"']").find("li").each(
        function(index, el){
            uriNewItem = jQuery(el).attr("resource") + "_" + numOfLists;
			oldItem = jQuery("div[about='" + jQuery(el).attr("resource") + "']");
            newItem = oldItem.clone();
            newItem.attr("about", uriNewItem);
            newItem.find("span[property]").each(
                function(index, el){
                    /*
                    we should separate "raul:list" of Listbox from others because
                    the list could be reuse.
                    */
                    if((jQuery(el).attr("property") != "raul:list") || (newItem.attr("typeof")=="raul:Group")){
							newString = jQuery(el).text() +  "_" + numOfLists;
							jQuery(el).text(newString);						
					}
				}
            );
			buttonLocation.parent().before(newItem);
            insertListItems_value_html(newItem, numOfLists, buttonID);
			if(newItem.attr("typeof")=="raul:Group"){
				insertList(oldItem.find("span[property=raul:list]").text(), numOfLists, buttonID);
				//insertListItems(oldItem.find("span[property=raul:list]").text(), numOfLists, buttonID);
			}
        }
    );
}

function insertListItems_value_html(newItem, numOfLists, buttonID){
    var newObjectValue;
	var newGroupSubject;
	var uriNewValueTriple = newItem.find("span[property='raul:value']").text();
    var uriOldvalueTriple = uriNewValueTriple.substring(0, uriNewValueTriple.lastIndexOf('_'));
	var buttonLocation = jQuery("input#" + buttonID);
    var oldValueTriple = jQuery("div[about='" + uriOldvalueTriple  + "']");
    var newValueTriple = oldValueTriple.clone();
    newValueTriple.attr("about", uriNewValueTriple);
	//Note that we have separate checkbox, radio button and group from others
	if((newItem.attr("typeof")=="raul:Radiobutton") || (newItem.attr("typeof")=="raul:Radiobutton")){
	}
	else if(newItem.attr("typeof")=="raul:Group"){
		newObjectValue = newValueTriple.find("span[property='rdf:object']").text() +  "_" + numOfLists;
		newValueTriple.find("span[property='rdf:object']").text(newObjectValue);
	}	
	else{
	    newValueTriple.find("span[property='rdf:object']").text("");
	}	
	
	if( !(isEmpty(newItem.find("span[property='raul:group']"))) ){
		newGroupSubject = newValueTriple.find("span[property='rdf:subject']").text() +  "_" + numOfLists;
		newValueTriple.find("span[property='rdf:subject']").text(newGroupSubject);
	}
	
    buttonLocation.parent().before(newValueTriple);

    var newID = newItem.find("span[property='raul:id']").text();    
    var newName = newItem.find("span[property='raul:name']").text();
    var oldID = newID.substring(0, newID .lastIndexOf('_'));
    var oldName = newName.substring(0, newName .lastIndexOf('_'));    
    var oldHTML = jQuery("[name='" + oldName + "'],[id='" + oldID + "']");
    var newHTML = oldHTML.clone();
    newHTML.attr("name", newName);
    newHTML.attr("id", newID);
    //the value of a checkbox or a radio button should be remained
    if(newHTML.attr("type") == "text"){
        newHTML.val("");
    }
    newHTML.text("");    //for textarea
    
    buttonLocation.parent().before(newHTML);
    newHTML.wrap("<div></div>");
}
