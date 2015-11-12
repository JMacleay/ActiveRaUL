//Global variables (for demo) Declaration Start
var editor;
var processingInfo = "";
//Global variables (for demo) Declaration End
var currentTextarea;
var removableProperty={};
var sequenceGenerator={};
var idForMultipleTextbox={};
var record_size=3;

function createLink(targetURI){
	return "<input type=\"button\" value=\"Get Raul Rdf\" onclick=\"getRequestRDFWrap('" + targetURI + "');return false;\" style=\"margin-left:15px;display:none;\"></input>";
}

function createLinkWithStyle(targetURI){
	//return "<a href=\"#\" onclick=\"getRequestRDFWrap('" + targetURI + "');return false;\" style='clear: left;float: left;padding-left 0px;'>" + targetURI + "</a><br/>";
	return "<input type=\"button\" value=\"getRaulRdf\" onclick=\"getRequestRDFWrap('" + targetURI + "');return false;\" style=\"margin-left:15px;display:none;\"></input>";
}

//Show a process icon on the center of the screen when system is loading 
function showProcessingInfo(event){
	jQuery('body').append('<div class="overlay-container" style="display: block;"/>');
	jQuery("h2[class='processing_info']").each(
		function(index, el){
			jQuery(el).css("display","inline");
			jQuery(el).css("position","fixed");
			jQuery(el).css("top",'50%');
			jQuery(el).css("left",'50%');
		}
	);
}

//Hide the process icon right after the system finishes loading
function hideProcessingInfo(){
	jQuery("h2[class='processing_info']").each(
		function(index, el){
			jQuery(el).css("display","none");
		}
	);
	jQuery('body').children().last().remove();
}

function initTab(){
			jQuery(".jquery-tabs span:first").addClass("current");
			jQuery(".jquery-tabs ul:not(:first)").hide();
			jQuery(".jquery-tabs span").click(function(){
				if(jQuery(this).parent()[0].tagName.toLowerCase()=='a')
					return;
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
			processingInfo  + 
			getForm(jQuery("input#serviceURL").val())+ createLink(jQuery("input#serviceURL").val());
			
			//just for testing the new function of adding property dynamically
			//jQuery('select[id="birthday_year"]').parent().after('<input type="button" id="addDynamicProperty" value="+" onclick="addDynamicProperty(\'\',\'addDynamicProperty\')"/>');
			
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
			processingInfo  + 
			getForm(jQuery("input#serviceURL").val())+ createLink(jQuery("input#serviceURL").val());
			
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
		
		//For binding the event to a button which user clicks on to convert a arbitrary ontology to
		//a RaUL based RDF form and the all the classes of that ontology will be returned to front end. Then
		// user is able to select his/her preferred class to extract the corresponding form
		jQuery('#complexFormCreate').click(function(event) {
			if(jQuery('#tab_2').attr('style').indexOf('display:none')==-1)
				jQuery('#tab_2').attr('style','display:none;');
			editor.save();
			showProcessingInfo(event);
			var updateDiv=jQuery('#updateDiv');
			jQuery('div[id="extractedClasses"]').empty();
			jQuery('div[id="formDiv"]').empty();
			remotePost(document.getElementById('showrdf').value,'/service/public/forms/class',function(data){
				var html=data['buttons']+data['comments'];
				document.getElementById('extractedClasses').innerHTML=html;
				currentTextarea=data['currentTextarea'];
				jQuery('#generateRelevantRdf').after(updateDiv);
			});
			hideProcessingInfo();
			jQuery('#tab_2').attr('style','');
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
	document.getElementById('content').innerHTML = processingInfo + getData(dataURI)+ createLink(dataURI) ;
	hideProcessingInfo();
}

function getRequestRDFWrap(requestURI){
	jQuery("textarea#showrdf").val("");
	
	showProcessingInfo();
	var returnRDF = formatXml(getRequestRDF(requestURI));
	editor.setValue(returnRDF);
	hideProcessingInfo();
}

function submitDataWrap(){
	showProcessingInfo();
	originalSubjectURI = jQuery("[property='rdf:subject']").first().text();
	originalSubjectKey = originalSubjectURI.substr(originalSubjectURI.lastIndexOf('/') + 1);
	
	jQuery("textarea#showrdf").val("");
	var templates=jQuery('div[id="templates"]');
	if(templates!=undefined)
		templates.remove();
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
	
	var dataInstanceURI = "";
	var url=jQuery("input#serviceURL").val();
	if(url==='')
		url=jQuery("input#serviceURLforClass").val();
	if(originalSubjectKey.indexOf("defaultInstanceGraph")!=-1){
		dataInstanceURI = postData(url, rdfString);
		if(jQuery("input#serviceURL").val()!='')
			jQuery("input#serviceURL").val(dataInstanceURI);
		else
			jQuery("input#serviceURLforClass").val(dataInstanceURI);
	}
	else{
		dataInstanceURI = jQuery("input#serviceURL").val();
		if(dataInstanceURI=='')
			dataInstanceURI=jQuery("input#serviceURLforClass").val();
		putData(dataInstanceURI, rdfString);
	}
	
	if(jQuery("input#serviceURL").val()!=''){
		document.getElementById('content').innerHTML = getData(dataInstanceURI)+ createLink(dataInstanceURI);
		hideProcessingInfo();
	}else{
		document.getElementById('formDiv').innerHTML = getData(dataInstanceURI)+createLinkWithStyle(dataInstanceURI);
		hideProcessingInfo();
	}
}

function changeComment(){
	var clazz=jQuery('select[id="classSelector"]').find("option:selected").text();
	if(currentTextarea==clazz)
		return;
	else{
		jQuery('div[id="'+currentTextarea+'"]').hide();
		jQuery('div[id="'+clazz+'"]').show();
		currentTextarea=clazz;
	}
}

function createFormBySelectedClass(){
	jQuery('#updateDiv').css("display","none");
	var hostName='http://'+document.location.hostname;
	if(document.location.port!=='')
		hostName+=':'+document.location.port;
	//alert(document.location.hostname+":"+document.location.port);
	var dataUrl=jQuery('select[id="classSelector"]').val();
	//var clazz=jQuery("#classSelector option:selected").text();
	showProcessingInfo();
	var encodedUrl=encodeURIComponent(dataUrl);
	document.getElementById('formDiv').innerHTML =getDataWithParam(hostName+serviceURI+'/tempFormId?pathType=program&classURI='+encodedUrl)+createLinkWithStyle(hostName+serviceURI+'/'+dataUrl);
	//createLinkForMultipleClass(serviceURI+'/'+dataUrl);
	var formUrl=jQuery('div[typeof="raul:Page"]').first().attr('about');
	var formId=formUrl.substring(formUrl.lastIndexOf('/')+1);
	jQuery("input#serviceURLforClass").val(hostName+serviceURI+'/'+formId);
	changeSubjectValueUnderGroup();
	jQuery('#updateDiv').css("display","block");
	hideProcessingInfo();
}

//Change the subject values of all the elements belonging to a group which is inside another group
//Specifically , this function works for groups which have level property with value greater than 1
function changeSubjectValueUnderGroup(){
	jQuery('fieldset[copied="copied"]').each(function(index,el){
		var groupId=jQuery(el).find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
		var value=jQuery(el).find('div[typeof="raul:Group"]').first().find('span[property="raul:value"]').first().text();
		var level1GroupUrl='';
		jQuery(el).children().each(function(i,e){	
			var ele=jQuery(e);
			if(ele[0].tagName.toLowerCase()!='div')
				return;
			if(ele.find('span[property="rdf:subject"]').length>0){
				if(ele.attr('about')!=undefined&&ele.attr('about')==value){
					var text=ele.find('span[property="rdf:subject"]').text();
					var defaultSubject=text.substring(0,text.indexOf('_'));
					ele.find('span[property="rdf:subject"]').text(defaultSubject);
					ele.find('span[property="rdf:object"]').text(defaultSubject+'_'+groupId);
					level1GroupUrl=defaultSubject+'_'+groupId;
				}else{
					ele.find('span[property="rdf:subject"]').text(level1GroupUrl);
				}
			}
			if(ele.attr('level')!=undefined){
				changeSubjectValueSubLevel(level1GroupUrl,ele);
			}
		});
	});
}

//Recursive function for traversing all the sub groups of a group and change the subject values of the elements under those subgroup
function changeSubjectValueSubLevel(lastLevelGroupId,div){
	var thisLevelGroupUrl='';
	var groupId='';
	var groupValue='';
	var nextLevelGroupUrl='';
	div.find('fieldset').first().children().each(function(index,el){
		var ele=jQuery(el);
		if(ele[0].tagName.toLowerCase()!='div')
			return;
		if(ele.attr('typeof')!=undefined&&ele.attr('typeof').toLowerCase()=='raul:group'){
			groupId=ele.find('span[property="raul:id"]').first().text();
			groupValue=ele.find('span[property="raul:value"]').first().text();
			return;
		}
		if(ele.find('span[property="rdf:subject"]').length>0){
			if(ele.attr('about')!=undefined&&ele.attr('about')==groupValue){
				var text=ele.find('span[property="rdf:subject"]').text();
				ele.find('span[property="rdf:subject"]').text(lastLevelGroupId);
				ele.find('span[property="rdf:object"]').text(lastLevelGroupId+'_'+groupId);
				nextLevelGroupUrl=lastLevelGroupId+'_'+groupId;
			}else{
				ele.find('span[property="rdf:subject"]').text(nextLevelGroupUrl);
			}
		}
		if(ele.attr('level')!=undefined){
			changeSubjectValueSubLevel(nextLevelGroupUrl,ele);
		}
	});
}

function addDynamicProperty(propertyName,buttonId){
	var targetHtml=jQuery('div[about="'+propertyName+'"]');
	var newNode=changeValueOfProperty(targetHtml.clone(),propertyName);
	//insert new property into list
	var ol=targetHtml.parent().first();
	addNewPropertyToList(propertyName,newNode.attr('about'),ol);
	var currentNode=targetHtml;
	var button=jQuery('input[id="'+buttonId+'"]');
	var newNodes=[];
	newNodes[0]=newNode;
	var index=1;
	//button.before(newNode);
	//for(var i=0;currentNode.next().attr('id')!=buttonId;){
	for(;;){
		//for list box and radio box, we can reuse some parts of their componenets
		//if(true)
		currentNode=currentNode.next();
		if(currentNode.first().attr('id')==buttonId)
			break;
		if(currentNode[0].tagName.toLowerCase()=='ol')
			continue;
		if(currentNode.attr('typeof')=='raul:Listitem')
			continue;
		newNodes[index++]=changeValueOfProperty(currentNode.clone(),propertyName);
		//just for test, appending new property just after the first element of the form
		//button.before(newNode);
	}
	removableProperty[newNode.attr('about')]=[];
	button.after('<input type="button" value="-" onclick="removeDynamicProperty(this,\''+newNode.attr('about')+'\')" style="margin:10px 0px;"/>');
	for(var i=newNodes.length;i>=0;i--){
		button.after(newNodes[i]);
		removableProperty[newNode.attr('about')][i]=newNodes[i];
	}
	sequenceIncrease(propertyName);
}

function changeValueOfProperty(newProperty,about){
	var oldAbout=newProperty.attr('about');
	if(oldAbout!=null&&oldAbout!=undefined)
		newProperty.attr('about',addExtraNumber(oldAbout,about));
	newProperty.find('span').each(function(index,el){
		var jel=jQuery(el);
		var property=jel.attr('property');
		if(property=='raul:label'||property=='raul:id'||property=='raul:name'||property=='raul:value'){
			if(property=='raul:label'&&jel.parent().attr('typeof')=='raul:Radiobutton'){
				return;
			}
			else
				jel.text(addExtraNumber(jel.text(),about));
		}
	});
	newProperty.find('input').each(function(index,el){
		var jel=jQuery(el);
		jel.attr('id',addExtraNumber(jel.attr('id'),about));
		jel.attr('name',addExtraNumber(jel.attr('name'),about));
		if(jel.attr('type').toLowerCase()=='radio'&&jel.attr('value')!='')
			jel.attr('value',addExtraNumber(jel.attr('value'),about));
		else{
			jel.attr('value','');
			jel.text('');
		}
	});
	//for selector
	newProperty.find('select').each(function(index,el){
		var jel=jQuery(el);
		jel.attr('id',addExtraNumber(jel.attr('id'),about));
		jel.attr('name',addExtraNumber(jel.attr('name'),about));
	});
	return newProperty;
}

function addNewPropertyToList(oldAbout,newAbout,ol){
	var increase=false;
	var newListItem=jQuery('li[resource="'+oldAbout+'"]').clone();
	newListItem.attr('resource',newAbout);
	var rel=newListItem.attr('rel');
	var index=rel.substring(rel.indexOf('_')+1);
	var prefix=rel.substring(0,rel.indexOf('_')+1);
	newListItem.attr('rel',prefix+(Number(index)+1));
	jQuery('li[resource="'+oldAbout+'"]').after(newListItem);
	ol.find('li').each(function(index,el){
		var jel=jQuery(el);
		var about=jel.attr('resource');
		if(increase==true){
			var rel=jel.attr('rel');
			var index=rel.substring(rel.indexOf('_')+1);
			var prefix=rel.substring(0,rel.indexOf('_')+1);
			jel.attr('rel',prefix+(Number(index)+1));
		}
		if(about==newAbout){
			increase=true;
		}
	});
}

function removeDynamicProperty(removeButton,propertyName){
	var currentNode=jQuery('div[about="'+propertyName+'"]');
	var ol=currentNode.parent().find('ol').first();
	removeButton=jQuery(removeButton);
	var removedNode;
	for(;;){
		if(currentNode.attr('value')==removeButton.attr('value')){
			currentNode.remove();
			break;
		}else{
			removedNode=currentNode;
			currentNode=currentNode.next();
			removedNode.remove();
		}
	}
	var decrease=false;
	ol.find('li').each(function(index,el){
		var jel=jQuery(el);
		var about=jel.attr('resource');
		if(decrease==true){
			var rel=jel.attr('rel');
			var index=rel.substring(rel.lastIndexOf('_')+1);
			var prefix=rel.substring(0,rel.lastIndexOf('_')+1);
			jel.attr('rel',prefix+(Number(index)-1));
		}
		if(about==propertyName){
			jel.remove();
			decrease=true;
		}
	});
}

function addExtraNumber(str,about){
	if(sequenceGenerator[about]==null||sequenceGenerator[about]==undefined)
		sequenceGenerator[about]=1;
	return str+'_'+sequenceGenerator[about];
}

function sequenceIncrease(about){
	sequenceGenerator[about]=sequenceGenerator[about]+1;
}

//open a window that contains the objects matching the searching criteria (range)
function openSearchWindow(range,about,link){
	//link is the id of the button. The button consists of an <a> tag and a <span> tag inside the <a> tag.
	//Find button by specifying the id of the button
	var button=jQuery(link);
	//If the form/group is currently not editable, there will be no response for user's click on the button.
	if(button.attr('class').toLowerCase()=='buttondisabled')
		return;
	if(button.attr('class').toLowerCase()=='editgroup'&&button.attr('disabled')=='disabled')
		return;
		
	var params={
		range:range
	}
	//Request url which contains the searching criteria (range)
	var url='/service/public/forms/instances?'+jQuery.param(params);
	
	//Send request to back end to fetch all the objects matching the searching criteria
	//The window displaying all the matching objects will be built in a call back function
	generalGet(url,function(tableHtml){
		// Window constructor
		var window=new CusWindow({
			width:800,
			id:'searching',
		});
		
		//Page constructor
		//A page object controls the pagniation of the matching objects.
		var page=new Page({
			recordSize:record_size,
			data:tableHtml,
			about:about,
			link:link,
			window:window
		});
		
		//Get all the objects on the first page.
		var tableData=page.getDataSegement(1);
	
		window.setContent(tableData);
	
		window.setFootbar(page.createPage());
	
		page.setWindowBody(window.createNewWindow({}));
	
	});
	
}

//Open a window which contains all the previously created objects which have the same class as the object displayed on the current
//page. Use this window to select a previously created object to update the current form.
function openUpdateWindow(link){
	var dataType=jQuery('#classSelector option:selected').text();
	var range='http://purl.oclc.org/NET/ssnx/ssn#'+dataType;
	var about=jQuery('div[typeof="raul:Page"]').attr('about');
	openSearchWindow(range,about,link);
}

//Fill the group form/single textbox with urls which user selected from the window displaying all
//the objects matching the range of the target sub group form/single textbox.
function fill(instanceUrl,page){
	var retVal = confirm("Are you sure to select this instance ?");
	if( retVal == false ){
	  return;
	}
	var button=jQuery(page.link);
	var parent=button.parent().parent();
	//Fill single textbox with only the url of the object selected from the searching window. 
	if(parent.find('div[about="'+page.about+'"]').first().attr('typeof')!=undefined&&parent.find('div[about="'+page.about+'"]').first().attr('typeof').toLowerCase()=='raul:textbox'){
		var textbox=parent.find('div[about="'+page.about+'"]').first();
		var input=textbox.next().next();
		input.children().first().val(jQuery(instanceUrl).find('a[class="resultURI"]').first().attr('about'));
	}//update entire raul-based web form with url and values of properties of the selected object
	else if(jQuery('div[about="'+page.about+'"]').attr('typeof').toLowerCase()=='raul:page'){
		var form=jQuery('form');
		var url=jQuery(instanceUrl).find('a[class="resultURI"]').first().attr('about');
		//Iterate all properties of the raul-based web form to replace the subject of those properties with
		//the url of the selected object.
		form.children().each(function(index,el){
			var ele=jQuery(el);
			//If the type of the property is raul:group, the value of the subject of the group will be replaced by the url of the 
			//selected object. And its object value will be updated accordingly. All the subject values of properties of that group
			//will also be replaced by new object value of the group.
			if(ele.find('fieldset').length>0){
				var firstSubject=ele.find('span[property="rdf:subject"]').first();
				firstSubject.text(url);
				var group=firstSubject.parent().prev();
				var id=group.find('span[property="raul:id"]').first().text();
				var newGroupUrl=url+'_'+id;
				ele.find('span[property="rdf:object"]').first().text(newGroupUrl);
				ele.children().first().children().each(function(index,el){
					var element=jQuery(el);
						if(element.find('span[property="rdf:subject"]').length>0){
							if(element.find('span[property="rdf:object"]').first().text()==newGroupUrl)
								return;
							if(element.attr('level')!=undefined){
								
							}else{
								element.find('span[property="rdf:subject"]').first().text(newGroupUrl);
							}
						}
					});
				return;
			}
			if(ele.find('span[property="rdf:subject"]').length>0){
				ele.find('span[property="rdf:subject"]').first().text(url);
			}
		});
		form.find('input').first().val(jQuery(instanceUrl).find('a[class="resultURI"]').first().attr('label'));
		//To update all the values of properties of raul-based web form.
		var labelId=form.find('input').first().attr('id');
		form.children().each(function(index,el){
			var ele=jQuery(el);
			if(ele.find('fieldset').length>0){
				var input=ele.find('input').first();
				var tripleDiv=input.parent().prev();
				var subject=tripleDiv.find('span[property="rdf:subject"]').first().text();
				var id=subject.substring(subject.lastIndexOf('_')+1);
				//var id=handleLabel(input.attr('id'));
				var element=jQuery(instanceUrl).find('p[propvalue*="'+id+'"]').first();
				var text=jQuery.trim(element.text().split(':')[1]);
				input.val(text);
				return;
			}
			if(ele.children().first()[0]!=undefined&&ele.children().first()[0].tagName.toLowerCase()=='input'){
				var input=ele.children().first();
				if(input.attr('id')==labelId)
					return;
				input.val('');
				var id=handleLabel(input.attr('id'));
				id=id.substring(id.lastIndexOf('_')+1);
				var propertyList=jQuery(instanceUrl).find('p[about*="'+id+'"]');
				currentList=form.find('input[id*="'+id+'"]');
				propertyList.each(function(index,el){
						var element=jQuery(el);
						var text=jQuery.trim(element.text().split(':')[1]);
						var inputTarget=jQuery(currentList[index]);
						var subject=inputTarget.parent().prev().find('span[property="rdf:subject"]').first();
						inputTarget.val(subject.text()+'_'+text);
				});
			}
		});
	}
	//filling the group form with url of the selected object from the searching window and values of properties 
	//of that object
	else{
		if(parent[0].tagName.toLowerCase()!='fieldset'){
			parent=jQuery('#winContent').find('fieldset').first();
		}
		//change the object value of the group to url of the selected object
		var targetGroupUrl=jQuery(instanceUrl).find('a[class="resultURI"]').first().attr('about');
		var groupValueTriple=parent.find('div[about="'+page.about+'"]').first().next();
		groupValueTriple.find('span[property="rdf:object"]').first().text(targetGroupUrl);
		//Iterate all properties of the group to replace their subject values with url of the selected object.
		parent.children().each(function(index,el){
			//type of ele : div
			var ele=jQuery(el);
			if(ele.find('span[property="rdf:subject"]').length>0){
				if(ele.find('span[property="rdf:object"]').first().text()==targetGroupUrl)
					return;
				if(ele.attr('level')!=undefined){
					//we currently just neglect the nested dynamic groups
				}else{
					ele.find('span[property="rdf:subject"]').first().text(targetGroupUrl);
				}
			}
		});
		var label=jQuery(instanceUrl).find('a[class="resultURI"]').first().text();
		var valueAdded={};
		//Fill properties of the group with values of properties of the selected object.
		parent.children().each(function(index,el){
			var ele=jQuery(el);
			if(ele.children().first()[0]!=undefined&&ele.children().first()[0].tagName.toLowerCase()=='input'){
				var input=ele.children().first();
				var id=handleLabel(input.attr('id'));
				id=id.substring(id.lastIndexOf('_')+1);
				if(id.indexOf('label')!=-1){
					var labelValue=label.replace('"','').replace('"','');
					if(labelValue.indexOf(' ')!=-1)
						input.val(labelValue.split(' ')[0]);
					else
						input.val(labelValue);	
					return;
				}
			}
			//revert to the older version, fill in all the values a group has
			if(valueAdded[id]==undefined||valueAdded[id]==null){
					var propertyList=jQuery(instanceUrl).find('p[about*="'+id+'"]');
					var currentList=parent.find('input[id*="'+id+'"]');
					if(propertyList.length>currentList.length){
						var lastEle=currentList.last().parent();
						for(var i=0;i<propertyList.length-currentList.length;i++){
							var button=lastEle.next().children().first();
							button.attr('class','button');
							// button.click();
							// button.attr('class','buttonDisabled');
							lastEle=parent.find('input[id*="'+id+'"]').last().parent();
						}
					}
					if(propertyList.length<currentList.length){
						var firstEle=currentList.first().parent();
						for(var i=0;i<currentList.length-propertyList.length;i++){
							var button=firstEle.next().children().first();
							button.attr('class','button');
							// button.click();
							// button.attr('class','buttonDisabled');
							firstEle=parent.find('input[id*="'+id+'"]').first().parent();
						}
					}
					currentList=parent.find('input[id*="'+id+'"]');
					propertyList.each(function(index,el){
						var element=jQuery(el);
						var text=jQuery.trim(element.text().split(':')[1]);
						var inputTarget=jQuery(currentList[index]);
						var subject=inputTarget.parent().prev().find('span[property="rdf:subject"]').first();
						inputTarget.val(subject.text()+'_'+text);
					});
					valueAdded[id]=id;
			}
		});
	}
	//var fieldset=button.parent().parent();
	page.window.close();
}

function copySpecifiedWidgets(widgetName){
	var ele=jQuery('input[name="'+widgetName+'"]');
	var elementsOnWin=new Array();
	if(ele.length==0){
		var groups=jQuery('div[about="'+widgetName+'"]');
		var groupDiv=null;
		groups.each(function(index,el){
			if(jQuery(el).parent().attr('group')!=undefined)
				return;
			else
				groupDiv=jQuery(el);
		});
		var fieldset=groupDiv.parent();
		var i=0;
		fieldset.children().each(function(index,el){
			var o=jQuery(el);
			if(o[0].tagName.toLowerCase()=='div'){
				if(o.attr('typeof')=='raul:Textbox')
					elementsOnWin[i++]=o.clone();
				if(o.find('input').length>0||o.find('select').length>0){
					var oldEle=null;
					if(o.find('input').length>0)
						oldEle=o.find('input').first();
					else
						oldEle=o.find('select').first();
					var newEle=oldEle.clone();
					newEle.attr('id',oldEle.attr('id')+'_win');
					newEle.attr('name',oldEle.attr('name')+'_win');
					newEle.removeAttr('readonly');
					newEle.removeAttr('style');
					newEle.val('');
					elementsOnWin[i++]=newEle;
				}
			}
		});
	}else{
		var label={};
		var fields={};
		jQuery('span[property="raul:name"]').each(function(index,el){
			if(jQuery(el).text().indexOf(widgetName)!=-1){
				label[jQuery(el).text()]=jQuery(el).parent().parent().clone();
			}
		});
		jQuery('input[name*="'+widgetName+'"]').each(function(index,el){
			var oldInput=jQuery(el);
			var newInput=jQuery(el).clone();
			newInput.attr('id',oldInput.attr('id')+'_win');
			newInput.attr('name',oldInput.attr('name')+'_win');
			newInput.removeAttr('readonly');
			newInput.removeAttr('style');
			newInput.val('');
			fields[oldInput.attr('name')]=newInput;
		});
		jQuery('select[name*="'+widgetName+'"]').each(function(index,el){
			var oldInput=jQuery(el);
			var newInput=jQuery(el).clone();
			newInput.attr('id',oldInput.attr('id')+'_win');
			newInput.attr('name',oldInput.attr('name')+'_win');
			fields[oldInput.attr('name')]=newInput;
		});
		var index=0;
		for(var key in label){
			elementsOnWin[index++]=label[key];
			elementsOnWin[index++]=fields[key];		
		}
	}
	return elementsOnWin;
}


function editGroup(link){
	var fieldset=jQuery(link).parent().parent();
	var lengthOfFields=fieldset.find('input').length+fieldset.find('select').length;
	if(lengthOfFields==0)
		return;
	if(fieldset.find('p[id="indication"]').length>0)
		fieldset.find('p[id="indication"]').first().remove();
	var indication='<p id="indication" class="groupURI">Class URI : '+fieldset.find('div[typeof="raul:Group"]').first().attr('about')+'</p>';
	fieldset.children().each(function(index,el){
		var ele=jQuery(el);
		if(ele.children().length>0&&(ele.children().first()[0].tagName.toLowerCase()=='input'||ele.children().first()[0].tagName.toLowerCase()=='select')){
			if(ele.children().first()[0].tagName.toLowerCase()=='input')
				ele.children().first().attr('disabled',false);
			else
				ele.attr('style','display:block;');
		}
	});
	if(fieldset.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:comment"]').length==0){
		fieldset.find('div[typeof="raul:Group"]').children().first().append('<span property="rdfs:comment">edited</span>');
	}else{
		var commentSpan=fieldset.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:comment"]').first();
		if(commentSpan.text().indexOf('edited')==-1)
			commentSpan.text(commentSpan.text()+':edited');
	}
	if(fieldset.find('#indication').length==0)
		fieldset.find('div[typeof="raul:Group"]').first().after(indication);
}

function editSingleField(about,link){
	var ele=jQuery('div[about="'+about+'"]');
	for(;;){
		var eleTagName=ele[0].tagName.toLowerCase();
		if(eleTagName=='a'&&ele.attr('class')=='button')
			break;
		if(eleTagName=='div'){
			ele.find('input').each(function(index,el){
				//jQuery(el).removeAttr('disabled');
				jQuery(el).attr('disabled',false);
			});
		}
		if(ele.find('select').length>0)
			ele.attr('style','display:block;');
		if(ele.attr('typeof')!=undefined){
			if(ele.find('span[property="rdfs:comment"]').length>0){
				var span=ele.find('span[property="rdfs:comment"]').first();
				var value=span.text();
				if(value.indexOf('edited')==-1)
					span.text(value+':edited');
			}else{
				ele.find('span[style="display:none"]').append('<span property="rdfs:comment">edited</span>');
			}
		}
		ele=ele.next();
	}
}

//String cleanning function. Only works for data type property
function autoAddBaseUrl(textbox){
	var value=jQuery(textbox).val();
	if(value=='')
		return;
	if(/(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(value)==false){
		value=value.replace(/[|";:\$\/#\?&@%<>\(\)\+~]/g,' ');
		value=value.replace(/\s+/,'_');
		jQuery(textbox).attr('labelvalue',value);
		jQuery(textbox).val(value);
	}
}

//Open a window to show the form of a group which is inside another group.
//If this group has sub groups (groups are inside this group), they are also displayed in web forms
//on the same window right beneath the form of the group.
//Specifically, the value of the level property of the group is greater than 1.
function openSubClassWindow(id,a){
	var button=jQuery(a);
	var fieldSet=button.parent();
	var comment=fieldSet.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:comment"]').first().text();
	if(button.attr('class').toLowerCase()=='buttondisabled')
		return;
	var newForm=jQuery('<form/>');
	var tempDiv=fieldSet.find('div[id="'+id+'"]');
	var divForG;
	if(tempDiv.length==0){
		divForG=fieldSet.find('div[id*="'+id+'"]').children().first();
		id=fieldSet.find('div[id*="'+id+'"]').attr('id');
	}else
		divForG=tempDiv.children().first();
	divForG.attr('parentId',id);
	var pageIdForG=divForG.find('div[typeof="raul:Group"]').first().attr('about');
	var olForDG=fieldSet.find('li[resource="'+pageIdForG+'"]').first().parent();
	var dyGroupListName=olForDG.attr('about');
	var divForDG;
	fieldSet.find('span[property="raul:list"]').each(function(index,el){
		var text=jQuery(el).text();
		if(text==dyGroupListName){
			divForDG=jQuery(el).parent().parent();
			divForDG.attr('parentId',divForDG.parent().attr('id'));
		}
	});
	newForm.append(divForDG);
	newForm.append(olForDG);
	newForm.append(divForG);
	var newDiv=null;
	var dynamicButtonTemplate=null;
	//var correspondingDGAbout=null;
	var correspondingDGAbout=[];
	//newForm.append(jQuery('<div/>'));
	var indexForDG=0;
	divForG.find('div[id*="_temp"]').each(function(index,el){
		var div=jQuery(el);
		var firstChild=div.children().first();
		if(firstChild.attr('typeof')=='raul:DynamicGroup'){
			if(newDiv==null){
				newDiv=jQuery('<div/>');
				newForm.append(newDiv);
				dynamicButtonTemplate=jQuery('a[class="button"]').first();
			}
			firstChild.attr('parentId',div.attr('id'));
			newDiv.append(firstChild);
			var lists=firstChild.find('span[property="raul:list"]');
			for(var i=0;i<lists.length;i++){
				newDiv.append(newForm.find('ol[about="'+jQuery(lists[i]).text()+'"]').first());
			}
			correspondingDGAbout[indexForDG++]=firstChild.attr('about');
		}else{
			div.children().each(function(index, el){
				var eleGroup=jQuery(el);
				if(eleGroup.children().first()[0].tagName.toLowerCase()=='input'){
					newDiv.append(eleGroup);
				}else{
					eleGroup.attr('parentId',div.attr('id'));
					//change the margin-top of fieldSet of subclass to 0px
					eleGroup.children().first().attr('style','margin-top:0px;padding:10px;');
					newDiv.append(eleGroup);
				}	
			});
		}
	});
	//remove the border of the fieldSet of first class
	var fieldsetOnWin=divForG.children().first();
	fieldsetOnWin.attr('style','border:0;margin-top:0px;margin-bottom:0px;padding:10px;');
	fieldsetOnWin.children().first().attr('style','display:none;');
	
	//extract header info
	var legend=fieldsetOnWin.children().first();
	var headerInfoProp=legend.children().first().text();
	var headerInfoInstance=legend.children().first().next().text();
	var lookupLink=legend.children().last().clone();
	var lookupLinkDiv=jQuery('<div style="clear:none;padding-right:16px;padding-top:2px;text-align:right;float:right;color:black;"/>');
	lookupLinkDiv.append(lookupLink);
	//lookupLink.wrap('<div style=""></div>');
	/*lookupLink.text('');
	lookupLink.attr('class','button');
	lookupLink.append('<span>Look up Existing '+headerInfoInstance+'</span>');
	lookupLink.wrap('<div></div>');
	fieldsetOnWin.find('div[typeof="raul:Textbox"]').first().before(lookupLink);*/
	
	//need to change because a class might have several subclasses
	indexForDG=0;
	if(dynamicButtonTemplate!=null){
		newDiv.find('fieldset').each(function(index,el){
			var div=jQuery(el).parent();
			var newAddButton=dynamicButtonTemplate.clone();
			var dynamicGroupName=correspondingDGAbout[indexForDG].split('#')[1];
			newAddButton.attr('id','addDynamicWidgets_'+dynamicGroupName);
			newAddButton.attr('group',correspondingDGAbout[indexForDG]);
			newAddButton.attr('name','addDynamicWidgets_'+dynamicGroupName);
			newAddButton.attr('style','clear:none;margin:14px 5px;');
			newAddButton.children().first().text(' + ');
			newAddButton.removeAttr('onclick');
			newAddButton.click(function(){
				dynamicWidgetsAdd(correspondingDGAbout[indexForDG],'addDynamicWidgets_'+dynamicGroupName,true);
			});
			div.after(newAddButton);
			indexForDG++;
		});
	}
	var backUpForm=newForm.clone(true);
	backUpForm.attr('id','backupForWindow');
	var addButton=backUpForm.find('a[id*="addDynamic"]').first();
	addButton.unbind('click');
	addButton.bind('click',{'pageId':addButton.attr('pageid'),'addButton':addButton.get()},function(event){
		dynamicComponentAdd(event.data.pageId,event.data.addButton);
	});
	backUpForm.find('a[id*="removeDynamic"]').each(function(index,el){
		var removeButton=jQuery(el);
		removeButton.unbind('click');
		removeButton.bind('click',{'pageId':removeButton.attr('pageid'),'removalButton':removeButton.get()},function(event){
			dynamicComponentRemove(event.data.pageId,event.data.removalButton);
		});
	});
	
	
	var window=new CusWindow({
		width:830
	});
	window.setForm(newForm);
	window.setHeaderbar('<div style="padding-left:16px;padding-top:6px;"><i>'+headerInfoProp+'</i>'+'&nbsp;&nbsp;'+'<b>'+headerInfoInstance+'</b></div>');
	window.setHeaderbarTool(lookupLinkDiv);
	if(button.children().first().text().indexOf('Update')!=-1){
	window.createNewWindow({
		'Update':function(){
			if(newDiv!=null){
				var lastDynamicGroup=null;
				var lastDynamicGroupParent=null;
				var parentGroup=null;
				newDiv.children().each(function(index,el){
					var div=jQuery(el);
					if(div.attr('typeof')=='raul:DynamicGroup')
						lastDynamicGroup=div;
					if(div[0].tagName.toLowerCase()=='ol')
						lastDynamicGroup.after(div);
					if(div[0].tagName.toLowerCase()=='input')
						return;
					if(div[0].tagName.toLowerCase()=='div'){
						var parent=null;
						if(div.children().first()[0].tagName.toLowerCase()=='a'){
							parentGroup.append(div);
							parentGroup=null;
						}else{
							if(div.attr('parentId')==null||div.attr('parentId')==undefined){
								var groupId=div.find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
								parent=jQuery('<div/>')
								parent.attr("id",groupId+'_temp');
								parent.attr("level",3);
								parent.append(div);
								lastDynamicGroupParent.after(parent);
							}else{
								parent=divForG.find('#'+div.attr('parentId')).first();
								parent.append(div);
							}
							parentGroup=parent;
							if(div.attr('typeof')=='raul:DynamicGroup')
								lastDynamicGroupParent=parent;
						}
					}
				});
			}
			fieldSet.find('#'+divForDG.attr('parentId')).first().append(divForDG);
			fieldSet.find('#'+divForDG.attr('parentId')).first().after(olForDG);
			fieldSet.find('#'+divForG.attr('parentId')).first().append(divForG);
			var buttonText=button.children().first().text();
			if(buttonText.indexOf('Update')==-1){
				buttonText='Update '+buttonText.split(" ")[1];
				button.children().first().text(buttonText);
				addNewGroup(divForDG.attr('about'),fieldSet,a);
			}
			backUpForm.remove();
			window.close();
		},
		'Close':function(){
			var needNewDiv=false;
			if(newDiv!=null&&newDiv!=undefined)
				needNewDiv=true;
			newForm.remove();
			jQuery('#winContent').append(backUpForm);
			divForDG=backUpForm.children().first();
			olForDG=divForDG.next();
			divForG=olForDG.next();
			if(needNewDiv==true)
				newDiv=backUpForm.children().last();
			if(newDiv!=null){
				var lastDynamicGroup=null;
				var lastDynamicGroupParent=null;
				var parentGroup=null;
				newDiv.children().each(function(index,el){
					var div=jQuery(el);
					if(div.attr('typeof')=='raul:DynamicGroup')
						lastDynamicGroup=div;
					if(div[0].tagName.toLowerCase()=='ol')
						lastDynamicGroup.after(div);
					if(div[0].tagName.toLowerCase()=='input')
						return;
					if(div[0].tagName.toLowerCase()=='div'){
						var parent=null;
						if(div.children().first()[0].tagName.toLowerCase()=='a'){
							parentGroup.append(div);
							parentGroup=null;
						}else{
							if(div.attr('parentId')==null||div.attr('parentId')==undefined){
								var groupId=div.find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
								parent=jQuery('<div/>')
								parent.attr("id",groupId+'_temp');
								parent.attr("level",3);
								//parent.attr('style','display:none;')
								parent.append(div);
								lastDynamicGroupParent.after(parent);
							}else{
								parent=divForG.find('#'+div.attr('parentId')).first();
								parent.append(div);
							}
							parentGroup=parent;
							if(div.attr('typeof')=='raul:DynamicGroup')
								lastDynamicGroupParent=parent;
						}
					}
				});
			}
			fieldSet.find('#'+divForDG.attr('parentId')).first().append(divForDG);
			fieldSet.find('#'+divForDG.attr('parentId')).first().after(olForDG);
			fieldSet.find('#'+divForG.attr('parentId')).first().append(divForG);
			backUpForm.remove();
			window.close();
		}
	});
	}else{
		window.createNewWindow({
		'Fill in':function(){
			if(newDiv!=null){
				//newDiv.find('input[class="dynamicButton"]').each(function(index,el){
					//jQuery(el).remove();
				//});
				var lastDynamicGroup=null;
				var lastDynamicGroupParent=null;
				var parentGroup=null;
				newDiv.children().each(function(index,el){
					var div=jQuery(el);
					if(div.attr('typeof')=='raul:DynamicGroup')
						lastDynamicGroup=div;
					if(div[0].tagName.toLowerCase()=='ol')
						lastDynamicGroup.after(div);
					if(div[0].tagName.toLowerCase()=='input')
						return;
					if(div[0].tagName.toLowerCase()=='div'){
						var parent=null;
						if(div.children().first()[0].tagName.toLowerCase()=='a'){
							parentGroup.append(div);
							parentGroup=null;
						}else{
							if(div.attr('parentId')==null||div.attr('parentId')==undefined){
								var groupId=div.find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
								parent=jQuery('<div/>')
								parent.attr("id",groupId+'_temp');
								parent.attr("level",3);
								//parent.attr('style','display:none;')
								parent.append(div);
								lastDynamicGroupParent.after(parent);
							}else{
								parent=divForG.find('#'+div.attr('parentId')).first();
								parent.append(div);
							}
							parentGroup=parent;
							if(div.attr('typeof')=='raul:DynamicGroup')
								lastDynamicGroupParent=parent;
						}
					}
				});
			}
			fieldSet.find('#'+divForDG.attr('parentId')).first().append(divForDG);
			fieldSet.find('#'+divForDG.attr('parentId')).first().after(olForDG);
			fieldSet.find('#'+divForG.attr('parentId')).first().append(divForG);
			var buttonText=button.children().first().text();
			if(buttonText.indexOf('Update')==-1){
				buttonText='Update '+buttonText.split(" ")[1];
				button.children().first().text(buttonText);
				addNewGroup(divForDG.attr('about'),fieldSet,button);
			}
			var property=divForG.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:comment"]').first();
			if(property.text()!='')
				property.text(property.text()+':instanceEdited');
			else
				property.text('instanceEdited');
			backUpForm.remove();
			window.close();
		},
		'Cancel':function(){
			var needNewDiv=false;
			if(newDiv!=null&&newDiv!=undefined)
				needNewDiv=true;
			newForm.remove();
			jQuery('#winContent').append(backUpForm);
			divForDG=backUpForm.children().first();
			olForDG=divForDG.next();
			divForG=olForDG.next();
			if(needNewDiv==true)
				newDiv=backUpForm.children().last();
			if(newDiv!=null&&newDiv!=undefined){
				var lastDynamicGroup=null;
				var lastDynamicGroupParent=null;
				var parentGroup=null;
				newDiv.children().each(function(index,el){
					var div=jQuery(el);
					if(div.attr('typeof')=='raul:DynamicGroup')
						lastDynamicGroup=div;
					if(div[0].tagName.toLowerCase()=='ol')
						lastDynamicGroup.after(div);
					if(div[0].tagName.toLowerCase()=='input')
						return;
					if(div[0].tagName.toLowerCase()=='div'){
						var parent=null;
						if(div.children().first()[0].tagName.toLowerCase()=='a'){
							parentGroup.append(div);
							parentGroup=null;
						}else{
							if(div.attr('parentId')==null||div.attr('parentId')==undefined){
								var groupId=div.find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
								parent=jQuery('<div/>')
								parent.attr("id",groupId+'_temp');
								parent.attr("level",3);
								//parent.attr('style','display:none;')
								parent.append(div);
								lastDynamicGroupParent.after(parent);
							}else{
								parent=divForG.find('#'+div.attr('parentId')).first();
								parent.append(div);
							}
							parentGroup=parent;
							if(div.attr('typeof')=='raul:DynamicGroup')
								lastDynamicGroupParent=parent;
						}
					}
				});
			}
			fieldSet.find('#'+divForDG.attr('parentId')).first().append(divForDG);
			fieldSet.find('#'+divForDG.attr('parentId')).first().after(olForDG);
			fieldSet.find('#'+divForG.attr('parentId')).first().append(divForG);
			backUpForm.remove();
			window.close();
		}
	});
	}
}

function addNewGroup(dynamicGroupAbout,fieldSet,button){
	var dynamicGroup=fieldSet.find('div[about="'+dynamicGroupAbout+'"]');
	var currentIndex=getDynamicIndex(dynamicGroupAbout);
	var nameArray=dynamicGroupAbout.split('_');
	var dynamicGroupAbout='';
	var index=-1;
	for(var i=nameArray.length-1;i>=0;i--){
		if(isNaN(nameArray[i])==true){
			index=i;
			break;
		}
	}
	var cloneListItem=jQuery('#templates').find('div[about*="'+nameArray[index]+'"]').first().find('span[property="raul:list"]').first();
	var newListItem=cloneListItem.clone();
	var text=newListItem.text();
	newListItem.text(text+'_'+currentIndex);
	dynamicGroup.find('span[property="raul:list"]').first().after(newListItem);
	var cloneoOldOl=jQuery('#templates').find('ol[about="'+cloneListItem.text()+'"]');
	var newOl=cloneoOldOl.clone();
	newOl.attr('about',text+'_'+currentIndex);
	newOl.children().first().attr('resource',cloneoOldOl.children().first().attr('resource')+'_'+currentIndex);
	dynamicGroup.parent().after(newOl);
	var groupAbout=cloneoOldOl.children().first().attr('resource');
	var templateFieldSet=jQuery('#templates').find('fieldset[group*="'+nameArray[index]+'"]').parent();
	var newGroupDiv=templateFieldSet.clone();
	newGroupDiv.children().first().removeAttr('group');
	treeWalkToChangeValue(newGroupDiv.children().first(),currentIndex);
	var div=fieldSet.find('div[about*="'+groupAbout+'"]').first().parent().parent().parent();
	var newParent=jQuery('<div/>');
	newParent.append(newGroupDiv);
	var id=newGroupDiv.find('div[typeof="raul:Group"]').first().find('span[property="raul:id"]').first().text();
	newParent.attr('id',id+'_temp');
	div.after(newParent);
	button.after(createRemoveBtn(button.next().attr('id')));
	newGroupDiv.attr('parentId',id+'_temp');
	newParent.attr('level',div.attr('level'));
	newParent.attr('style','display:none;');
	createNewAddButton(id+'_temp',button,newParent);
}

function createRemoveBtn(tempDivId){
	var btnHtml=jQuery('<a style="clear:none;margin:8px;" href="javascript:void(0)" onclick="removeAddedWidget(\''+tempDivId+'\',this)" class="button"><span>Remove</span></a>');
	return btnHtml;
}

function removeAddedWidget(tempDivId,btn){
	var fieldSet=jQuery(btn).parent();
	var comment=fieldSet.find('div[typeof="raul:Group"]').first().find('span[property="rdfs:comment"]').first().text();
	if(comment.indexOf('searched')!=-1)
		return;
	var tempDiv=fieldSet.find('#'+tempDivId).first();
	var groupAbout=tempDiv.find('div[typeof="raul:Group"]').first().attr('about');
	var ol=fieldSet.find('li[resource="'+groupAbout+'"]').first().parent();
	fieldSet.find('span[property="raul:list"]').each(function(index,el){
		var ele=jQuery(el);
		if(ele.text()==ol.attr('about'))
			ele.remove();
	});
	tempDiv.remove();
	ol.remove();
	jQuery(btn).prev().remove();
	jQuery(btn).prev().remove();
	jQuery(btn).remove();
}

function createNewAddButton(tempDivId,button,newlyAddedGroup){
	var addLabel=button.prev().clone();
	newlyAddedGroup.before(addLabel);
	var buttonText=button.children().first().text().split(" ")[1];
	var addBtnHtml=jQuery('<a style="clear:none;margin:8px;" href="javascript:void(0)" onclick="openSubClassWindow(\''+tempDivId+'\',this);" class="button"><span>Add '+buttonText+'</span></a>');
	newlyAddedGroup.before(addBtnHtml);
	return addBtnHtml;
}

//If a property's cardinality is greater than 1, the function is used to dynamically replicate the property and then 
// add it right beneath the orignial property which is the template for property replication.
//The function will be conditionally triggered by user clicking on the "plus" button next to the property which he/she
//wants to replicate
function dynamicComponentAdd(pageId,button,automatedFired){
	//if the property which will be replicated is in a group, we first get the <fieldset> of that group to facilitate
	//the subsequent operation.
	var fieldset=jQuery(button).parent().parent();
	//if the group form is disabled because of previous update, there will be no response to user's click on the "plus" button.
	if(jQuery(button).attr('class').indexOf('Disabled')!=-1)
		return;
	var buttonId=jQuery(button).attr('id');
	//retrieve the textbox which is used as the template for replication
	if(pageId==null)
		pageId=jQuery(button).attr('pageId');
	var textBox=jQuery(button).parent().parent().find('div[about="'+pageId+'"]');
	if(textBox.length==0){
		textBox=jQuery(button).parent().parent().find('div[about*="'+pageId+'"]').first();
		pageId=textBox.attr('about');
	}
	var ele=textBox;
	//get range of the textbox. new textbox should have the same ability to search the objects of the range class as
	// the template
	var range=textBox.find('span[property="rdfs:range"]').first().text();
	var currentIndex=getMultipleTextBoxIndex();
	var newPageId='';
	//assemble new Id for new textbox
	if(pageId.indexOf('_')==-1){
		newPageId=pageId+'_'+currentIndex;
	}else{
		var nameArray=pageId.split('_');
		if(isNaN(nameArray[nameArray.length-1])==true){
			newPageId=pageId+'_'+currentIndex;
		}else{
			for(var i=0;i<nameArray.length-1;i++){
				if(i==0)
					newPageId+=nameArray[i];
				else
					newPageId+='_'+nameArray[i];
			}
			newPageId+='_'+currentIndex;
		}
	}
	var added=false;
	var lastEle=jQuery(button).parent();
	var parentName=jQuery(button).parent().parent()[0].tagName.toLowerCase();
	//to replicate all the tags essential for a text box. The replication procedure starts from
	//the replication of the raul:textbox. The procedure ends after it has replicated the plus button for the template textbox.
	for(;;){
		//if the cursor meets the plus button, it will terminate the loop.
		if(ele.find('a[id="'+buttonId+'"]').length>0)
			break;
		var newEle=ele.clone();
		//replicate listitem for a new list box
		if(newEle.attr('typeof')!=undefined&&newEle.attr('typeof').toLowerCase()=='raul:listitem'){
			changeValue(newEle,currentIndex);
			lastEle.after(newEle);
			lastEle=newEle;
			ele=ele.next();
			continue;
		}
		if(newEle[0].tagName.toLowerCase()=='a'){
			changeInformationOfButton(newEle,newPageId,range);
		}else{
			changeValue(newEle,currentIndex);
			if(parentName=='fieldset')
				treeWalkToChangeValue(newEle,currentIndex);
			else
				treeWalkToChangeValue(newEle,currentIndex,true);
		}
		lastEle.after(newEle);
		lastEle=newEle;
		if(newEle.attr('typeof')!=undefined&&newEle.attr('typeof').toLowerCase()=='raul:listbox'){
			insertLi(ele.attr('about'),newPageId,button);
			insertLi(newPageId,newEle.attr('about'),button);
			added=true;
		}
		ele=ele.next();
	}
	if(added==false)
		insertLi(pageId,newPageId,button);
	if(textBox.find('span[property="rdfs:comment"]').length=0){
		textBox.find('span[style="display:none;"]').first().after('<span property="rdfs:comment">singleTextAdded</span>');
	}else{
		var commentBox=textBox.find('span[property="rdfs:comment"]').first();
		commentBox.text(commentBox.text()+':singleTextAdded');
	}
	lastEle.after(changeAndAddButton(jQuery(button),pageId,newPageId,currentIndex));
}

//insert a new li tag to ol tag. This is used when a new textbox/listbox is being replicated.
//Its information should be registered in the group/raul web form it belongs to.
function insertLi(pageId,newPageId,button){
	var parent=jQuery(button).parent().parent();
	var li=parent.find('li[resource="'+pageId+'"]');
	var index=parseInt(li.attr('rel').split('_')[1]);
	var newLi=li.clone();
	newLi.attr('resource',newPageId);
	newLi.attr('rel','rdf:_'+(index+1));
	li.after(newLi);
	var ol=li.parent();
	var lis=ol.children();
	for(var i=index+1;i<lis.length;i++){
		li=lis[i];
		jQuery(li).attr('rel','rdf:_'+(i+1));
	}
}

//The search button is replicated with different information,namly the new pageId of a new widget and the reference 
//of that widget
function changeInformationOfButton(button,newPageId,range){
	button.attr('field',newPageId);
	button.removeAttr('onclick');
	button.click(function(){
		openSearchWindow(range,newPageId,button);
	});
}

// get new index for any new widgets that are replicated by a existing one
// the new index is made of time
function getMultipleTextBoxIndex(){
	return new Date().getTime();
}

//After finishing the procedure of widget replication, the function of the plus button for the template
//will be changed to removing the widget instead of adding a new widget.
//Conversly, the button for the newly added widget will be given the function of adding new widget.
function changeAndAddButton(button,pageId,newPageId,currentIndex){
	var newAddButton=button.clone();
	button.children().first().text(' - ');
	var buttonIdAndName=button.attr('id');
	var text='removeDynamicComponent_'+currentIndex;
	button.attr('id',text);
	button.attr('name',text);
	button.removeAttr('onclick');
	button.unbind('click');
	button.attr('pageId',pageId);
	button.bind('click',{'pageId':pageId,'removalButton':button.get()},function(event){
		dynamicComponentRemove(event.data.pageId,event.data.removalButton);
	});
	
	var newText='addDynamicComponent_'+new Date().getTime();
	newAddButton.attr('id',newText);
	newAddButton.attr('name',newText);
	newAddButton.removeAttr('onclick');
	newAddButton.attr('pageId',newPageId);
	newAddButton.bind('click',{'newPageId':newPageId,'button':newAddButton.get()},function(event){
		dynamicComponentAdd(event.data.newPageId,event.data.button);
	});
	var newDiv=jQuery('<div/>');
	newDiv.append(newAddButton);
	return newDiv;
}

//To remove a widget which is created previously through clicking on the minus button beside the widget
function dynamicComponentRemove(pageId,removalButton){
	var fieldset=jQuery(removalButton).parent().parent();
	//When the group where target widget which are going to be removed reside is currently disabled
	//The removal procedure will stop.
	if(jQuery(removalButton).attr('class').indexOf('Disabled')!=-1)
		return;
	//get the fieldset tag or form tag, depending on whether the target widget
	// resides in a group or in a web form
	var parent=jQuery(removalButton).parent().parent();
	var id=jQuery(removalButton).attr('id');
	if(pageId==null)
		pageId=jQuery(removalButton).attr('pageId');
	var ele=parent.find('div[about="'+pageId+'"]');
	var li=parent.find('li[resource="'+pageId+'"]');
	var ol=li.parent();
	var liIndex=parseInt(li.attr('rel').split('_')[1]);
	//remove li tag under ol tag. The removed li tag contains the 
	//information about the target widget.
	li.remove();
	var lis=ol.children();
	//Because of the removal of a li tag, other remaining li tags' index
	//should be upgraded as well
	for(var i=liIndex-1;i<lis.length;i++){
		li=jQuery(lis[i]);
		li.attr('rel','rdf:_'+(i+1));
	}
	//remove the widget
	for(;;){
		if(ele.children().length>0&&ele.children().first().attr('id')==id){
			ele.remove();
			break;
		}
		var nextEle=ele.next();
		ele.remove();
		ele=nextEle;
	}
}