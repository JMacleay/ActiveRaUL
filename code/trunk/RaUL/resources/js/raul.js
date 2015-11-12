//Global variables Declaration Start
//var serviceURI = "/raul/service/public/forms";
var serviceURI = "/service/public/forms";
var postType = "application/rdf+xml";
var xmlDisplayingMethod = "view-source:data:text/html;,";
var agentName = "firefox";
//need to change
var dynamicIndex=1;
var dynamicIndexGroup={};
var sourceList='';
//var lastTextBoxId;
//var lastTemporalGroupId;

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

function generalGet(url,callback){
	jQuery.ajax({
			type: 'GET',
			async: false,
			url: url,
			dataType: 'html',
			success: function(data, textStatus, xhr){	
				callback(data);	
			}
	});	
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

//need to improve
function remotePost(formDef,url,callbackfunction){
	formDef = assignDefaultURI(formDef);
	jQuery.ajax({					
			type: 'POST',
			async: false,
			url: url,
			processData: false,			
			contentType: postType,
			data: formDef,
	
			success: function(data, textStatus, xhr){
						callbackfunction(data);
					}
	});
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
			url: formURI+'?pathType=program',
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
			url: dataURI+'?pathType=program',
			dataType: 'html',
			success: function(data, textStatus, xhr){
						htmlFormData = data;
					}
	});	
	return htmlFormData;
}

function getDataWithParam(dataURI){
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
			//changed to add url before the objectvalue
			var labelValue=jQuery("input[name='" + name + "'],input[id='" + id + "']").attr('labelvalue');
			if(objectValue == null){
				objectValue = jQuery("textarea[name='" + name + "'],input[id='" + id + "']").text();
			}
			
			jQuery(el).find("span[property=raul:value]").each(
				function(index1, el1){
					about = jQuery(el1).text();
					if(labelValue!=undefined&&labelValue!=null){
						var aboutValue=jQuery("input[name='" + name + "'],input[id='" + id + "']").attr('aboutvalue');
						jQuery("[about='"+ about +"']").children("[property='rdf:object']").text(Trim(aboutValue+'_'+objectValue, ''));
					}
					else
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
	jQuery('form').first().find("[typeof='raul:Listitem']").each(
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
	//if(identifier != ""){newURI = identifier;}else{newURI = sHKey;}
	newURI=(new Date().getTime())+'';
	//jQuery("[property='rdf:subject']").each(
	var subject=null;
	var predicate=null;
	var object=null;
	
	var allNameSpaces={};
	var div=jQuery("#formDiv").find('div').first();
	jQuery(div[0].attributes).each(function(){
		var name=this.nodeName;
		var value=this.nodeValue;
		allNameSpaces[name.split(':')[1]]=value;
	});
	jQuery("[property='rdf:subject'],[property='rdf:object']").each(
		function(index, el){
			oldResourceURI = jQuery(el).text();
            newResourceURI = oldResourceURI.replace(regex, newURI);
            jQuery(el).text(newResourceURI);
			if(jQuery(el).attr('property')=='rdf:subject'){
				subject=jQuery(el).text();
				predicate=jQuery(el).next().text();
				if(subject.indexOf('_type')!=-1){
					subject=subject.replace('_type','');
					jQuery(el).text(subject);
				}
			}
			if(jQuery(el).attr('property')=='rdf:object'){
				var parent=jQuery(el).parent();
				if(jQuery(el).text()!=''){
					var text=jQuery(el).text();
				if(parent.prev().attr('typeof').toLowerCase()=="raul:listbox"){
					var about=parent.prev().attr('about');
					var textAbout=about.replace('_type','');
					var textBoxPorp=jQuery('div[about="'+textAbout+'"]').first();
					textBoxPorp.find('span[property="rdfs:range"]').first().text(text);
				}
				if(parent.attr('about').toLowerCase().indexOf('label_value')!=-1){
					var newDivForRdfsLabel=jQuery('<div style="display:none;"/>');
					
					newDivForRdfsLabel .attr('about',subject);
					newDivForRdfsLabel.append('<span property="rdfs:label">'+text.substring(text.lastIndexOf('_')+1)+'</sapn>');
					jQuery(el).parent().after(newDivForRdfsLabel);
				}
				else{
					var box=parent.prev();
					var isObjectProperty=false;
					if(box.find('span[property="rdfs:comment"]').first().text().indexOf('ObjectProperty')!=-1)
						isObjectProperty=true;
					if(box.attr('typeof')!=undefined&&box.attr('typeof').toLowerCase()=='raul:group')
						isObjectProperty=true;
					if(predicate.indexOf('rdf:type')!=-1){
						var prefix=allNameSpaces[text.split(':')[0]];
						if(prefix.indexOf('#')==-1)
							prefix+='#';
						text=prefix+text.split(':')[1];
					}
					var newDiv=jQuery('<div style="display:none;"/>');
					newDiv.attr('about',subject);
					if(isObjectProperty==false)
						newDiv.append('<span property="'+predicate+'">'+text+'</sapn>');
					else
						newDiv.append('<span rel="'+predicate+'" resource="'+text+'"/>');
					
					var newDivForRdfsLabel=jQuery('<div style="display:none;"/>');
					//var newlyCreatedUrl=jQuery(el).parent().prev().attr('about');
					newDivForRdfsLabel.attr('about',text);
					if(text.indexOf('/')==-1){
						if(text.indexOf(':')!=-1)
							newDivForRdfsLabel.append('<span property="rdfs:label">'+text.split(':')[1]+'</sapn>');
						else
							newDivForRdfsLabel.append('<span property="rdfs:label">'+text+'</sapn>');
					}
					else{
						//migth need to be changed
						var textBox= jQuery(el).parent().next().children().first();
						newDivForRdfsLabel.append('<span property="rdfs:label">'+textBox.attr('labelvalue')+'</sapn>');
					}
					
					
					/*var newDivForType=jQuery('<div style="display:none"/>');
					if(text.indexOf('/')==-1&&box.find('span[property="rdfs:comment"]').first().text().indexOf('ObjectProperty')!=-1){
						var url=allNameSpaces[text.split(':')[0]];
						if(url.indexOf('#')==-1)
							url+='#';
						newDivForType.attr('about',url+text.split(':')[1]);
					}else{
						newDivForType.attr('about',text);
					}
					var range=box.find('span[property="rdfs:range"]').first().text();
					newDivForType.append('<span property="rdfs:type">'+range+'</sapn>');
					if(box.find('span[property="rdfs:comment"]').first().text().indexOf('ObjectProperty')!=-1||box.attr('typeof').toLowerCase()=='raul:group')
						jQuery(el).parent().after(newDivForType);*/
					if(box.attr('typeof').toLowerCase()!='raul:group'&&isObjectProperty==true&&box.attr('typeof').toLowerCase()!='raul:listbox')
						jQuery(el).parent().after(newDivForRdfsLabel);
					if(box.attr('typeof').toLowerCase()=='raul:listbox'){
						/*var newTripleForListBox=jQuery('<div style="display:none;"/>');
						var textboxObject=box.prev().prev().find('span[property="object"]').first().text();
						newTripleForListBox.attr('about',textboxObject);
						newTripleForListBox.append('<span rel="'+predicate+'" resource="'+subject+'"/>');
						jQuery(el).parent().after(newTripleForListBox);*/
					}
					jQuery(el).parent().after(newDiv);
					}
				}
			}
		}
	);
	//for the subject hash key generation
	
	log.profile('parsing RDFa');
	try{
		rdf = jQuery("#" + contentID).rdfa();
	}catch(err){
		alert(err.message);
	}
	log.profile('parsing RDFa');
	log.info("Number of statements: "+rdf.databank.size());
	/*jQuery(div[0].attributes).each(function(){
		var name=this.nodeName;
		var value=this.nodeValue;
		rdf.prefix(name.split(':')[1], value);
	});*/
	jQuery.each(allNameSpaces,function(key,value){
		rdf.prefix(key, value);
	});
	//rdf.prefix('raul', 'http://purl.org/NET/raul#');
	//rdf.prefix('dcterms', 'http://purl.org/dc/terms/');
	//rdf.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#');
	
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
function dynamicWidgetsAdd(uriDynamicGroup, buttonID, isOnwindow){
    var numOfLists, firstList,firstGroup;
	if(jQuery("a#" + buttonID).attr('class').toLowerCase()=='buttondisabled')
		return;
	if(isOnwindow==true){
		numOfLists=jQuery("a#" + buttonID).parent().find("div[about='"+ uriDynamicGroup+"']").find("span[property=raul:list]").length;
		firstList=jQuery("a#" + buttonID).parent().find("span[property=raul:list]").first().text();
		var fieldSet=jQuery("a#" + buttonID).parent().find('fieldset').first();
		//parentId=fieldSet.parent().attr('parentid');
	}else{
		numOfLists = jQuery("div[about='"+ uriDynamicGroup+"']").find("span[property=raul:list]").length;
		firstList = jQuery("div[about='"+ uriDynamicGroup+"']").find("span[property=raul:list]").first().text();
	}
	//firstGroup = jQuery("ol[about='"+firstList+"']").children().first().attr('resource');
	//if(sourceList=='')
		//sourceList=firstList;
	if(numOfLists==1){
		var removeButton=jQuery("a#" + buttonID).clone();
		removeButton.attr('id','removeDynamicWidgets_0');
		removeButton.attr('name','removeDynamicWidgets_0');
		removeButton.children().first().text(' - ');
		//removeButton.attr('style','margin:14px 5px;');
		removeButton.attr('class','button');
		removeButton.removeAttr("onclick");
		removeButton.removeAttr("group");
		removeButton.click(function(){
			if(isOnwindow==true)
				dynamicWidgetsRemove(firstList,uriDynamicGroup,this,'0',true);
			else
				dynamicWidgetsRemove(firstList,uriDynamicGroup,this,'0');
		});
		if(isOnwindow==true){
			jQuery("a#" + buttonID).before(removeButton);
		}
		else{
			jQuery("a#" + buttonID).parent().before(removeButton);
		}
		removeButton.wrap('<div></div>');
		//removeButtons['removeDynamicWidgets_0']=removeButton;
	}
	var currentIndex=getDynamicIndex(uriDynamicGroup);
    if(numOfLists >= 1){
        var newList = "\n\t\t<span property=\"raul:list\">" + handleLabel(firstList) + "_" + currentIndex + "</span>";        
        jQuery("[about='"+ uriDynamicGroup +"']").find("span[property=raul:list]").last().after(newList);
		var fieldSetHtml="";
		var legendhtml=jQuery('div[id="templates"]').find('fieldset[group="'+uriDynamicGroup+'"]').first().find("legend").first().clone();
		var button=jQuery("a#" + buttonID);
		if(isOnwindow==true){
			button.before(legendhtml);
		}
		else
			button.parent().before(legendhtml);
		
		//if(isOnwindow==true)
			//legendhtml.wrap('<div parentId="'+parentId+'"><fieldset></fieldset></div>');
		//else
			legendhtml.wrap('<div><fieldset></fieldset></div>');
		var legendEle=null;
		if(isOnwindow==true){
			legendEle=button.prev().find("legend").first();
		}else
			legendEle=button.parent().prev().find("legend").first();
        insertList(firstList, currentIndex, buttonID,legendEle,uriDynamicGroup,isOnwindow);
		//insertLink(uriDynamicGroup,legendEle,currentIndex);
    }
	//if(jQuery('input#removeDynamicWidgets_'+numOfLists).attr('id')==undefined){
		removeButton=jQuery("a#" + buttonID).clone();
		removeButton.attr('id','removeDynamicWidgets_'+currentIndex);
		removeButton.attr('name','removeDynamicWidgets_'+currentIndex);
		removeButton.children().first().text(' - ');
		removeButton.removeAttr("onclick");
		removeButton.removeAttr("group");
		removeButton.click(function(){
			if(isOnwindow==true)
				dynamicWidgetsRemove(handleLabel(firstList)+'_'+currentIndex,uriDynamicGroup,this,currentIndex,true);
			else
				dynamicWidgetsRemove(handleLabel(firstList)+'_'+currentIndex,uriDynamicGroup,this,currentIndex);
		});
		//removeButton.attr('style','margin:14px 5px;');
		removeButton.attr('class','button');
		if(isOnwindow==true)
			jQuery("a#" + buttonID).before(removeButton);
		else
			jQuery("a#" + buttonID).parent().before(removeButton);
		removeButton.wrap('<div></div>');
		//removeButtons['removeDynamicWidgets_'+currentIndex]=removeButton;
	//}
    jQuery("a#" + buttonID).attr("style","clear:none;margin:28px 5px;");
    //alert(jQuery("form").html());
	//dynamicIndex++;
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
function insertList(sourceList, numOfLists, buttonId,legendEle,uriDynamicGroup,isOnwindow){
	var newList;  
	if(jQuery("ol[about='"+ sourceList +"']").length==1)
		newList = jQuery("ol[about='"+ sourceList +"']").clone();
	else
		newList = jQuery('div[id="templates"]').find("ol[about='"+ sourceList +"']").first().clone();
    oldAbout = handleLabel(newList.attr("about"));
    newList.attr("about", oldAbout + "_" + numOfLists);
    newList.find("li").each(
        function(index, el){
            oldResource = handleLabel(jQuery(el).attr("resource"));
            jQuery(el).attr("resource", oldResource + "_" + numOfLists);
        }
    );
	//alert(newList.html());
	//if(jQuery("ol[about='"+ sourceList +"']").parent()[0].tagName.toLowerCase()=='fieldset')
		//legendEle.before(newList);
	//else
		jQuery("ol[about='" + sourceList + "']").after(newList);
    //insertListItems(sourceList, numOfLists, buttonID,legendEle);
	replicateFieldSet(uriDynamicGroup, numOfLists,legendEle);
}

function replicateFieldSet(uriDynamicGroup,numOfLists,legendEle){
	var fieldSet=jQuery('#templates').find('fieldset[group="'+uriDynamicGroup+'"]');
	var newFieldSet=legendEle.parent();
	legendEle.children().first().removeAttr('onclick');
	var a=legendEle.children().first();
	var range=fieldSet.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:range"]').first().text();
	var about=handleLabel(fieldSet.find('div[typeof="raul:Group"]').first().attr('about'))+'_'+numOfLists;
	a.bind('click',{'range':range,'about':about,'link':a},function(event){
		openSearchWindow(event.data.range,event.data.about,event.data.link);
	});
	fieldSet.children().each(function(index,el){
		var ele=jQuery(el);
		if(ele[0].tagName.toLowerCase()=='legend')
			return;
		else
			newFieldSet.append(ele.clone());
	});
	treeWalkToChangeValue(newFieldSet,numOfLists);
}

function treeWalkToChangeValue(element,numOfLists,isSingleBox){
	element.children().each(function(index,el){
		var ele=jQuery(el);
		if(ele[0].tagName.toLowerCase()=='legend')
			return;
		else if(ele.attr('typeof')!=undefined&&ele.attr('typeof').toLowerCase()=='raul:listitem'){
			changeValue(ele,numOfLists,isSingleBox);
		}
		else{
			changeValue(ele,numOfLists,isSingleBox);
			if(ele.children().length>0){
				treeWalkToChangeValue(ele,numOfLists,isSingleBox);
			}
		}
	});
}

function changeValue(element,numOfLists,isSingleBox){
	if(element.attr('about')!=null||element.attr('about')!=undefined){
		if(element.attr('typeof')!=undefined&&element.attr('typeof').toLowerCase()=='raul:listitem')
			element.attr('about',element.attr('about')+'_'+numOfLists);
		else
			element.attr('about',handleLabel(element.attr('about'))+'_'+numOfLists);
	}
	if(element[0].tagName.toLowerCase()=='span'){
		var propertyType=element.attr('property');
		//if(propertyType=='raul:id'||propertyType=='raul:list'||propertyType=='raul:value'||propertyType=='raul:id'||propertyType=='raul:label'||propertyType=='raul:name')
		if(propertyType=='raul:list'||propertyType=='raul:value'||propertyType=='raul:id'||propertyType=='raul:name')
			element.text(handleLabel(element.text())+'_'+numOfLists);
		//if(propertyType=='rdf:subject')
			//element.text(handleLabel(element.text())+'_'+numOfLists);
		if(propertyType=='rdfs:comment'&&element.text().indexOf(':')!=-1){
			element.text(element.text().substring(0,element.text().indexOf(':')));
		}
	}
	if(element[0].tagName.toLowerCase()=='input'){
		element.attr('id',handleLabel(element.attr('id'))+'_'+numOfLists);
		element.attr('name',handleLabel(element.attr('name'))+'_'+numOfLists);
		if(element.attr('type')!='button'){
			element.val('');
			element.attr('disabled',false);
		}
		if(element.attr('disabled')!=undefined&&element.attr('type')!='button'&&isSingleBox==true)
			element.attr('disabled',false);
	}
	if(element[0].tagName.toLowerCase()=='select'){
		element.attr('id',handleLabel(element.attr('id'))+'_'+numOfLists);
		if(element.parent().attr('style')!=undefined&&element.parent().attr('style').indexOf("display:none")!=-1)
			element.parent().removeAttr('style');
	}
	if(element[0].tagName.toLowerCase()=='li'){
		if(jQuery('div[about="'+element.attr('resource')+'"]').first().attr('typeof')=='raul:Listitem')
			element.attr('resource',element.attr('resource')+'_'+numOfLists);
		else
			element.attr('resource',handleLabel(element.attr('resource'))+'_'+numOfLists);
	}
	if(element.attr('id').indexOf('temp')!=-1){
		element.attr('id',handleLabel(element.attr('id'))+'_'+numOfLists);
	}
}

function changeSuffixOfString(name){
	if(name.indexOf('_')!=-1){
		var nameArray=name.split('_');
		if(isNaN(nameArray[nameArray.length-1])==false){
			return name.substring(0,name.lastIndexOf());
		}else
			return name;
	}
	return name;
}

function insertListItems(sourceList, numOfLists, buttonID,legendEle){
    var uriNewItem, newString, oldItem, newItem;
	var buttonLocation = jQuery("input#" + buttonID);
	var ol=null;
	if(jQuery("ol[about='"+ sourceList +"']").length==1)
		ol=jQuery("ol[about='"+ sourceList +"']");
	else
		ol=jQuery('div[id="templates"]').find("ol[about='"+ sourceList +"']").first();
    ol.find("li").each(
        function(index, el){
			uriNewItem = handleLabel(jQuery(el).attr("resource")) + "_" + numOfLists;
			oldItem = jQuery('div[id="templates"]').find("div[about='" + handleLabel(jQuery(el).attr("resource")) + "']").first();
            newItem = oldItem.clone();
            newItem.attr("about", uriNewItem);
            newItem.find("span[property]").each(
                function(index, el){
					if(jQuery(el).attr('datatype')!=undefined)
						return;
					if(jQuery(el).attr('property').indexOf('rdfs')!=-1)
						return;
                    if((jQuery(el).attr("property") != "raul:list") || (newItem.attr("typeof")=="raul:Group")){
							newString = handleLabel(jQuery(el).text()) +  "_" + numOfLists;
							jQuery(el).text(newString);						
					}
					
				}
            );
			//buttonLocation.parent().before(newItem);
			//alert(newItem[0].tagName);
			legendEle.before(newItem);
            insertListItems_value_html(newItem, numOfLists, buttonID,oldItem,legendEle);
			if(newItem.attr("typeof")=="raul:Group"){
				insertList(oldItem.find("span[property=raul:list]").text(), numOfLists, buttonID,legendEle);
				//insertListItems(oldItem.find("span[property=raul:list]").text(), numOfLists, buttonID);
			}
        }
    );
}

//add one more parameter for the convenience of removing the group
function insertListItems_value_html(newItem, numOfLists, buttonID,oldItem,legendEle){
    var newObjectValue;
	var newGroupSubject;
	var uriNewValueTriple = newItem.find("span[property='raul:value']").text();
    //var uriOldvalueTriple = uriNewValueTriple.substring(0, uriNewValueTriple.lastIndexOf('_'));
	var buttonLocation = jQuery("input#" + buttonID);
    var oldValueTriple = jQuery('div[id="templates"]').find("div[about='" + oldItem.find("span[property='raul:value']").text()  + "']").first();
    var newValueTriple = oldValueTriple.clone();
    newValueTriple.attr("about", uriNewValueTriple);
	//Note that we have separate checkbox, radio button and group from others
	if((newItem.attr("typeof")=="raul:Radiobutton") || (newItem.attr("typeof")=="raul:Radiobutton")){
	}
	else if(newItem.attr("typeof")=="raul:Group"){
		newObjectValue = handleLabel(newValueTriple.find("span[property='rdf:object']").text()) +  "_" + numOfLists;
		newValueTriple.find("span[property='rdf:object']").text(newObjectValue);
	}	
	else{
	    newValueTriple.find("span[property='rdf:object']").text("");
	}	
	
	if( !(isEmpty(newItem.find("span[property='raul:group']"))) ||newItem.attr('typeof')=='raul:Textbox'){
		newGroupSubject = handleLabel(newValueTriple.find("span[property='rdf:subject']").text()) +  "_" + numOfLists;
		newValueTriple.find("span[property='rdf:subject']").text(newGroupSubject);
	}
	
    //buttonLocation.parent().before(newValueTriple);
	legendEle.before(newValueTriple);
	
    var newID = newItem.find("span[property='raul:id']").text();    
    var newName = newItem.find("span[property='raul:name']").text();
    var oldID = oldItem.find("span[property='raul:id']").text();
    var oldName = oldItem.find("span[property='raul:name']").text();
	if(jQuery('div[id="templates"]').find("[name='" + oldName + "'],[id='" + oldID + "']").length>1)
		return;
    var oldHTML = jQuery('div[id="templates"]').find("[name='" + oldName + "'],[id='" + oldID + "']").first();
    var newHTML = oldHTML.clone();
    newHTML.attr("name", newName);
    newHTML.attr("id", newID);
    //the value of a checkbox or a radio button should be remained
    if(newHTML.attr("type") == "text"){
        newHTML.val("");
    }
    newHTML.text("");    //for textarea
    
    //buttonLocation.parent().before(newHTML);
	legendEle.before(newHTML);
	if(oldHTML.parent().attr('style')!=undefined)
		newHTML.wrap('<div style="display:none;"></div>');
	else
		newHTML.wrap("<div></div>");
}

function insertLink(uriDynamicGroup,legendEle,currentIndex){
	jQuery('div[id="templates"]').find('fieldset[group="'+uriDynamicGroup+'"]').first().find('a').each(function(index,el){
		if(jQuery(el).parent()[0].tagName.toLowerCase()=='legend')
			return;
		var element=jQuery(el).clone();
		if(currentIndex>0)
			element.attr('index',currentIndex);
		legendEle.before(element);
	});
	//var newLink=fieldset.find('a').first().clone();
	//legendEle.before(newLink);
}

function dynamicWidgetsRemove(targetDynamicGroup,dynamicGroup,removeButton,removeButtonIndex,isOnwindow){
	var container=jQuery(removeButton).parent().parent();
	if(container.find('div[about="'+dynamicGroup+'"]').attr('typeof').toLowerCase()!="raul:dynamicgroup"){
		var currentNode=container.find('ol[about="'+targetDynamicGroup+'"]');
		for(;;){
			if(currentNode.attr('typeof')=="raul:DynamicGroup")
				break;
			else
			    currentNode=currentNode.prev();
		}
		dynamicGroup=currentNode.attr('about');
	}
	var field=container.find('ol[about="'+targetDynamicGroup+'"]').find("li").first().attr("resource");
	var fieldDiv=null;
	var fieldDivs=container.find('div[about="'+field+'"]');
	if(fieldDivs.length==2){
		fieldDivs.each(function(index,el){
			var element=jQuery(el);
			if(element.parent().attr('group')==undefined||element.parent().attr('group')==null){
				fieldDiv=element;
			}
		});
	}else
		fieldDiv=fieldDivs;
	var fieldSet=fieldDiv.parent();
	container.find('ol[about="'+targetDynamicGroup+'"]').remove();
	container.find('div[about="'+dynamicGroup+'"]').find('span[property="raul:list"]').each(function(index,el){
		var element=jQuery(el);
		if(element.text()===targetDynamicGroup)
			element.remove();
	});
	if(removeButton!='')
		jQuery(removeButton).parent().remove();
	else
		fieldSet.parent().next().remove();
	fieldSet.parent().remove();
	if(container.find('div[about="'+dynamicGroup+'"]').first().find('span[property="raul:list"]').length==1){
		if(isOnwindow==true)
			container.find('a[group="'+dynamicGroup+'"]').prev().remove();
		else
			container.find('a[group="'+dynamicGroup+'"]').parent().prev().remove();
	}
}

function handleLabel(about){
	return about.replace(/_\d+/g,'');
}

function getDynamicIndex(dynamicGroupUri){
	/*var index=0;
	jQuery('div[about="'+dynamicGroupUri+'"]').find('span[property="raul:list"]').each(function(indexEl,el){
		var text=jQuery(el).text();
		if(text.lastIndexOf('_')!=-1){
			var currentIndex=Number(text.substring(text.lastIndexOf('_')+1));
			if(currentIndex>index){
				index=currentIndex;
			}
		}
	});
	return index+1;*/
	return new Date().getTime();
}
