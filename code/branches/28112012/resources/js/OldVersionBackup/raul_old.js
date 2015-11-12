function Trim(str,is_global) 
{ 
	var result; 
	result = str.replace(/(^\s+)|(\s+$)/g,""); 
	if(is_global.toLowerCase()=="g") 
		result = result.replace(/\s/g,""); 
	return result; 
} 

function parseDom() {

	var url="", sHKey="", sHKeyCount=0; //for the subject hash key generation
	
	log.profile("Updating raul:Textbox span elements");
	$("[typeof='raul:Textbox']").each(
		function(index, el){			
			var about="", name="", id="", objecValue="";
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();			
			
			objectValue = $("input[name='" + name + "'],input[id='" + id + "']").val();				
			$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
			
			//for the subject hash key generation
			var predValue ="";
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();      
			if((sHKeyCount < 2) && (objectValue != "") && (predValue != "owl:sameAs")){
				sHKey = sHKey + Trim(objectValue, 'g');              
				sHKeyCount++;         
		      }
		      //for the subject hash key generation
		}
	);	
		
	log.profile("Updating raul:Checkbox span elements");
	$("[typeof='raul:Checkbox']").each(
		function(index, el){		
			var about="";
			var appendLocation;
			appendLocation = $(el).find("span[property=raul:value]");
			about = appendLocation.text();
			
			if($(":checkbox[value='" + about + "']").attr('checked') == true){
				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">true</span>");								
				
				var selectedLabel = $(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;         
				}				
			}
			else{				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">false</span>");				
			}
		}
	);
		
	log.profile("Updating raul:Radiobutton span elements");
	$("[typeof='raul:Radiobutton']").each(
		function(index, el){		
			var about="";
			var appendLocation;
			appendLocation = $(el).find("span[property=raul:value]");
			about = appendLocation.text();
			
			if($(":radio[value='" + about + "']").attr('checked') == true){
				//alert($(":radio[value='" + about + "']").attr('value') + "\ntrue");
				//$("[about='"+ about +"']").children("[property='rdf:object']").text("true");
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">true</span>");								
				//$(el).find("span[property=raul:checked]").text("true");
				/*
				if( $(el).find("span[property=raul:checked]").text() != "")
					$(el).find("span[property=raul:checked]").text("true");
				else
					alert("not found");
				*/
				
				//for the subject hash key generation
				var selectedLabel = $(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;         
				}
				//for the subject hash key generation
			}
			else{
				//alert($(":radio[value='" + about + "']").attr('value') + "\nfalse");
				//$("[about='"+ about +"']").children("[property='rdf:object']").text("true");
				//$(el).find("span[property=raul:checked]").text("false");				
				appendLocation.after("\n\t\t\t<span property=\"raul:checked\" datatype=\"xsd:boolean\">false</span>");								
				/*
				if( $(el).find("span[property=raul:checked]").text() != "")
					$(el).find("span[property=raul:checked]").text("false");
				else
					alert("not found");
				*/
			}
		}
	);
	
	
	log.profile("Updating raul:Listitem span elements");	
	$("[typeof='raul:Listitem']").each(
		function(index, el){
			var about="";
			var appendLocation;
			appendLocation = $(el).find("span[property=raul:value]");
			about = appendLocation.text();
			$(el).find("span[property=raul:selected]").remove();
			
			if($("option[value='" + about + "']").attr('selected') == true){
				//alert(about + "\ntrue");
				appendLocation.after("\n\t\t\t<span property=\"raul:selected\" datatype=\"xsd:boolean\">true</span>");
				//alert($(el).html());
				
				//for the subject hash key generation
				var selectedLabel = $(el).find("span[property=raul:label]").text();
				selectedLabel = Trim(selectedLabel, 'g');
				if((sHKeyCount < 2) && (selectedLabel != "")){
					sHKey = sHKey + selectedLabel;
					sHKeyCount++;         
				}
				//for the subject hash key generation
			}
			else{				
				appendLocation.after("\n\t\t\t<span property=\"raul:selected\" datatype=\"xsd:boolean\">false</span>");
				
			}			
		}
	);
	
	$("[typeof='raul:Listbox']").each(
		function(index, el){			
			var about="", name="", id="", list_t="", objecValue="";
			var list;
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
			if( $(el).find("span[property=raul:list]").text != "" )
				list_t = $(el).find("span[property=raul:list]").text();
			
			//alert("list_t:" + list_t);
			//alert($("ol[about='"+ list_t +"']").html());			
			list = $("ol[about='"+ list_t +"']").get(0);
				
			var resource;				
			for(var i=1; i<list.childNodes.length; i++){				
				if(list.childNodes[i].nodeType != 3 && list.childNodes[i].nodeType != 8){ 
					resource = list.childNodes[i].attributes['resource'].value;					
					if($("[about='"+ resource +"']").find("[property='raul:selected']").text() == "true"){
						//alert("hit:" + resource);
						objectValue = $("[about='"+ resource +"']").find("[property='raul:value']").text();
						$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
						break;
					}					
				}
			}
		}
	);
			
	//for the subject hash key generation
	url = $("div[typeof='raul:WidgetContainer']").attr('about');
	url = url.slice(0, (url.indexOf("#")+1) );  
	$("[property='rdf:subject']").each(
		function(index, el){
		  $(el).text(url+sHKey);
		}
	);
	//for the subject hash key generation
	
	log.profile('parsing RDFa');
	rdf = $("html").rdfa();
	log.profile('parsing RDFa');
	log.info("Number of statements: "+rdf.databank.size());
	
	rdf.prefix('raul', 'http://purl.org/NET/raul#');
	rdf.prefix('dcterms', 'http://purl.org/dc/terms/');

	return rdf;
}

