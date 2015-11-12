function agentType()
{
	var Sys = {};
    var ua = navigator.userAgent.toLowerCase();
    var s;
    (s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] :
    (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
    (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
    (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
    (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
	
	postType = "application/rdf+xml";	

    //if (Sys.ie) 
    //if (Sys.firefox)
    if (Sys.chrome) postType = "application/xml";
    //if (Sys.opera)
    if (Sys.safari) postType = "application/xml";
}

function dataFillIn(inputID, fromDomain, hasClass, hasPredicate, hasOntology){
	var inputString;
    inputString = $("input[id='" + inputID + "']").val();
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
		queryExtData(inputString, fromDomain, hasClass, hasPredicate, hasOntology);
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
	
	$.ajax({
				beforeSend: function(req){req.setRequestHeader("Accept", "application/rdf+xml");},
				type: 'GET',
				url: queryString,				
				dataType: 'xml',
				success: function(data, textStatus, xhr){
					var numResults = $(data).find("totalResults").text();
					if(numResults != "0"){
						/*
						ExtURI = $(data).find("link").attr("rdf:resource")
						$.get("proxy.php?url=" + ExtURI, function(Extdata) {		
							document.getElementById('showrdf').innerHTML = Extdata;
							parseData(Extdata);
						});
						*/
						ExtURI = $(data).find("cache:first").text();
						$.ajax({
								beforeSend: function(req){req.setRequestHeader("Accept", "application/rdf+xml");},
								type: 'GET',
								url: ExtURI,
								dataType: 'xml',
								success: function(data, textStatus, xhr){
										//document.getElementById('showrdf').innerHTML = data;
										parseSindiceCache(data);
										var rdfString = (new XMLSerializer()).serializeToString(data);
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

function parseSindiceCache(data){

	
	$("[typeof='raul:Textbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
			
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			
			predValue = predValue.slice(predValue.indexOf(":")+1);
			objectValue = $(data).find(predValue).first().text();
									
			if(objectValue != ""){
				objectValue = Trim(objectValue, '');
			}
			else{
				objectValue = $(data).find(predValue).first().attr("rdf:resource");
				if(objectValue != null){
					objectValue = Trim(objectValue, '');
				}
			}
			
			$("input[name='" + name + "'],input[id='" + id + "']").val(objectValue);
			$("textarea[name='" + name + "'],input[id='" + id + "']").text(objectValue);
			$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
		}
	);
	
	$("[typeof='raul:Radiobutton']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = $("[about='"+ about +"']").children("[property='rdf:object']").text();
						
			predValue = predValue.slice(predValue.indexOf(":")+1);		
			SindiceCacheData = $(data).find(predValue).first().text();
			
			if(objectValue == SindiceCacheData){
				$(el).find("span[property=raul:checked]").text("true");
				document.getElementById(id).checked=true;				
			}
			else{
				$(el).find("span[property=raul:checked]").text("false");
				document.getElementById(id).checked=false;
			}
		}
	);
	
	/*
	$("[typeof='raul:Checkbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = $("[about='"+ about +"']").children("[property='rdf:object']").text();
									
			var dataArray = [];
			var dataTags = doc.getElementsByTagName(predValue);
			for(var i=0,mx=dataTags.length; i<mx;i++){
				dataArray[i] = dataTags[i].textContent;
				if(objectValue == dataArray[i]){
					$(el).find("span[property=raul:checked]").text("true");
					document.getElementById(id).checked=true;
					break;
				}
				else{
					$(el).find("span[property=raul:checked]").text("false");
					document.getElementById(id).checked=false;
				}
			}
		}
	);
	*/
	
	$("[typeof='raul:Listbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			predValue = predValue.slice(predValue.indexOf(":")+1);
			objectValue = $(data).find(predValue).first().text();
									
			if(objectValue != ""){
				objectValue = Trim(objectValue, '');
			}
			else{
				objectValue = $(data).find(predValue).first().attr("rdf:resource");
				if(objectValue != null){
					objectValue = Trim(objectValue, '');
				}
			}

			$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);			
			$("select[name='" + name + "'],select[id='" + id + "']").children("option[value='" + objectValue + "']").attr('selected', true);
		}
	);
	
}

function getExtData(dataURI){
	$.get("proxy.php?url=" + dataURI, function(data) {		
		data = formatXml(data);
		document.getElementById('showrdf').innerHTML = data;		
		editor.setValue(data);
		parseData(data);
	});
}

function parseData(data){

	var doc = document.createElement("div");
	doc.innerHTML = data;
	
	$("[typeof='raul:Textbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
			
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			var dataArray = [];
			var dataTags = doc.getElementsByTagName(predValue);
			for(var i=0,mx=dataTags.length; i<mx;i++){
				dataArray[i] = dataTags[i].textContent;
			}
			objectValue = dataArray[0];
			if(objectValue != null){
				objectValue = Trim(objectValue, '');
			}
			$("input[name='" + name + "'],input[id='" + id + "']").val(objectValue);
			$("textarea[name='" + name + "'],input[id='" + id + "']").text(objectValue);
			$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);
		}
	);
	
	$("[typeof='raul:Radiobutton']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = $("[about='"+ about +"']").children("[property='rdf:object']").text();
			
			var dataArray = [];
			var dataTags = doc.getElementsByTagName(predValue);
			for(var i=0,mx=dataTags.length; i<mx;i++){
				dataArray[i] = dataTags[i].textContent;
			}
			
			if(objectValue == dataArray[0]){
				$(el).find("span[property=raul:checked]").text("true");
				document.getElementById(id).checked=true;				
			}
			else{
				$(el).find("span[property=raul:checked]").text("false");
				document.getElementById(id).checked=false;
			}
		}
	);
	
	$("[typeof='raul:Checkbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			objectValue = $("[about='"+ about +"']").children("[property='rdf:object']").text();
			
			var dataArray = [];
			var dataTags = doc.getElementsByTagName(predValue);
			for(var i=0,mx=dataTags.length; i<mx;i++){
				dataArray[i] = dataTags[i].textContent;
				if(objectValue == dataArray[i]){
					$(el).find("span[property=raul:checked]").text("true");
					document.getElementById(id).checked=true;
					break;
				}
				else{
					$(el).find("span[property=raul:checked]").text("false");
					document.getElementById(id).checked=false;
				}
			}
		}
	);
	
	$("[typeof='raul:Listbox']").each(
		function(index, el){			
			var about="", name="", id="", predValue ="", objecValue="";
			
			about = $(el).find("span[property=raul:value]").text();
			if( $(el).find("span[property=raul:name]").text() != "" )			
				name = $(el).find("span[property=raul:name]").text();				
			if( $(el).find("span[property=raul:id]").text != "" )
				id = $(el).find("span[property=raul:id]").text();
				
			predValue = $("[about='"+ about +"']").children("[property='rdf:predicate']").text();
			
			var dataArray = [];
			var dataTags = doc.getElementsByTagName(predValue);
			for(var i=0,mx=dataTags.length; i<mx;i++){
				dataArray[i] = dataTags[i].textContent;				
			}
			objectValue = dataArray[0];					
			$("[about='"+ about +"']").children("[property='rdf:object']").text(objectValue);			
			$("select[name='" + name + "'],select[id='" + id + "']").children("option[value='" + objectValue + "']").attr('selected', true);
		}
	);
	
}

function postData(){
		
	$("textarea#showrdf").val("");
	var rdf = parseDom();	
	var rdfString = rdf.databank.dump({format:'application/rdf+xml', serialize: true});
	rdfString = formatXml(rdfString);
	editor.setValue(rdfString);
	
	
	showProcessingInfo();
	$.ajax({
			type: 'POST',												  
			url: $("input#postdatauri").val(),
			processData: false,
			//contentType: 'application/rdf+xml',
			contentType: postType,
			data: rdf.databank.dump({format:'application/rdf+xml', serialize: true}),
			
			success: function(data, textStatus, xhr){				
				$.ajax({
						beforeSend: function(req){
										req.setRequestHeader("Accept", "application/xhtml+xml");
									},
						type: 'GET',
						url: xhr.getResponseHeader('Location'),
						dataType: 'html',
						success: function(data, textStatus, xhr){
									document.getElementById('content').innerHTML = "<h2 class=\"processing_info\" style=\"color:#FF0000; display:none; position:absolute; right:10%;\">Processing ... <img src=\"img/processing_icon.gif\" /></h2>" + data;
									hideProcessingInfo();
								}
				});
			}
	});
	
}

var editor, postType;
$(document).ready(
	function(){
			agentType();
			$(".jquery-tabs span:first").addClass("current");			
			$(".jquery-tabs ul:not(:first)").hide();
			$(".jquery-tabs span").mouseover(function(){
				$(".jquery-tabs span").removeClass("current");
				$(this).addClass("current");
				$(".jquery-tabs ul").hide();
				$("."+jQuery(this).attr("id")).fadeIn("slow");
			});
			
			editor = CodeMirror.fromTextArea(document.getElementById("showrdf"), {
											lineNumbers: true,											
											mode: "xml"
											});
			
			$("textarea#showrdf").load('productAdd.rdf', 
								function() {									
									editor.setValue(document.getElementById("showrdf").value);
								}
							);
			
			$('#formcreate').click(function() {
				editor.save();
				showProcessingInfo();
				$.ajax({					
					type: 'POST',												  
					url: '/raul/service/public/forms',
					processData: false,
					//contentType: 'application/rdf+xml',
					contentType: postType,
					data: document.getElementById('showrdf').value,
	
					success: function(data, textStatus, xhr){								
								$("input#postdatauri").val(xhr.getResponseHeader('Location'));
								
								$.ajax({
									beforeSend: function(req){
													req.setRequestHeader("Accept", "application/xhtml+xml");
												},
									type: 'GET',
									url: xhr.getResponseHeader('Location'),
									dataType: 'html',
									success: function(data, textStatus, xhr){
												document.getElementById('content').innerHTML = "<h2 class=\"processing_info\" style=\"color:#FF0000; display:none; position:absolute; right:10%;\">Processing ... <img src=\"img/processing_icon.gif\" /></h2>" + data;
												hideProcessingInfo();
											}
									});
							}
				});
			});
                           
		$('#show').click(function() {
			log.info("show the parsed RDF")
			$("#result").find("tr:gt(0)").remove();
			var rdf = parseDom();
	
			$("#result").find("tr:gt(0)").remove();
			$('#result tr:last').after('<tr><td style="font: bold;">subject</td><td>predicate</td><td>object</td></tr>'); 
			
			rdf.where('?s ?p ?o').each(function() {
						$('#result tr:last').after('<tr><td>'+this.s.value+'</td><td>'+this.p.value+'</td><td>'+this.o.value+'</td></tr>');
			});
			return false;
		});
					
		$('#logging').click(function() {log.toggle();});
	}
);



