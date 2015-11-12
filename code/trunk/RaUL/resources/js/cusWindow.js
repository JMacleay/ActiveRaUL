function CusWindow(obj){
	//this.content=innerhtmlContent;
	this.parameters=obj;
	this.buttons=null;
	this.id=this.parameters['id'];
	if(this.id==undefined)
		this.id='';
}

CusWindow.prototype.createNewWindow=function(buttons){
	var buttonStr='';
	var customizedCloseButton=false;
	for(var buttonName in buttons){
		if(buttonName=='Close'||buttonName=='Cancel'){
			customizedCloseButton=true;
			continue;
		}
		buttonStr+='<span id="id_'+buttonName+'" class="close">'+buttonName+'</span>';
	}
	var id=this.id;
	var windowBody='<div class="overlay-container" style="display: block;">'+
	'<div class="window-container zoom window-container-visible">'+
	'<div id="headerBar'+id+'" style="clear:both;padding:0px 0px 0px 0px;text-align:left;font-size:normal;background-color: ActiveCaption;"></div>'+
	'<div id="winContent'+id+'" style="clear:both;margin:0px 0px 0px 20px;"></div>'+
	'<br>'+
	//'<div id="errorMsgsArea" style="clear:both;padding:5px 0px 0px 0px;text-align:center;font-family:Helvetica, sans-serif;font-size:12px;"></div>'+
	'<br>'+
	'<div id="footbar'+id+'" style="clear:both;padding:5px 0px 0px 0px;text-align:right;font-family:Helvetica, sans-serif;font-size:small;margin-left:18px;"></div>'+
	'<div id="buttonArea'+id+'" style="clear:both;padding:30px 0px 20px 0px;">'+
	buttonStr+
	'<span id="closeButton'+id+'" class="close">Close</span></div>'+
	'</div>'+
	'</div>';
	jQuery('body').children().last().after(windowBody);
	if(this.parameters['width']!=undefined)
		jQuery('div[class="window-container zoom window-container-visible"]').attr('style','width:'+this.parameters['width']+'px');
	else
		jQuery('div[class="window-container zoom window-container-visible"]').attr('style','width:560px');
	this.entity=jQuery('body').children().last();
	var window=this;
	if(buttons['Close']!=null||buttons['Close']!=undefined){
		this.entity.find('span[id="closeButton'+id+'"]').click(buttons['Close']);
		delete buttons['Close'];
	}else if(buttons['Cancel']!=null||buttons['Cancel']!=undefined){
		this.entity.find('span[id="closeButton'+id+'"]').click(buttons['Cancel']);
		this.entity.find('span[id="closeButton'+id+'"]').text('Cancel');
		delete buttons['Cancel'];
	}
	else{
		this.entity.find('span[id="closeButton'+id+'"]').click(function(){
			window.close();
		});
	}
	for(var buttonName in buttons){
		this.bindEvent(buttonName,buttons[buttonName]);
	}
	//this.renderTable();
	//this.renderForm();
	this.render();
	this.renderHeaderbar();
	this.renderFootbar();
	jQuery('body').attr('style','overflow:hidden;');
	var self=this;
	jQuery('div[class="overlay-container"]').first().bind('click',{},function(event){
		var windowDiv=jQuery('div[class*="window-container"]').first();
		var x=windowDiv.offset().left;
		var y=windowDiv.offset().top;
		var innerHeight=windowDiv.innerHeight();
		var farX=x+560;
		if(self.parameters['width']!=undefined)
			farX=x+self.parameters['width'];
		var farY=y+innerHeight;
		var eventTarget=event.target;
		if(jQuery(eventTarget)[0].tagName.toLowerCase()=='option')
			return;
		if((event.pageX<x||event.pageX>farX)){
			if(customizedCloseButton==true){
				self.entity.find('span[id="closeButton'+id+'"]').click();
			}else
				self.close();
		}else if(event.pageY<y||event.pageY>farY){
			if(customizedCloseButton==true){
				self.entity.find('span[id="closeButton'+id+'"]').click();
			}else
				self.close();
		}
	});
	
	return jQuery(jQuery.find('#winContent'+id));
}

CusWindow.prototype.close=function(){
	this.entity.remove();
	jQuery('body').attr('style','overflow:visible;');
}

CusWindow.prototype.bindEvent=function(name,func){
	this.entity.find('span[id="id_'+name+'"]').click(func);
}

CusWindow.prototype.getValues=function(){
	var fieldValues={};
	jQuery('#winform').children().each(function(index,el){
		var element=jQuery(el);
		if(element[0].tagName.toLowerCase()=='input'||element[0].tagName.toLowerCase()=='select'){
			fieldValues[element.attr('name')]=element.val();
		}
	});
	return fieldValues;
}

CusWindow.prototype.setForm=function(form){
	this.content=form;
}

CusWindow.prototype.renderForm=function(){
	jQuery('#winContent'+this.id+'').append('<div id="winform'+this.id+'"></div>');
	for(var i=0;i<this.content.length;i++){
		jQuery('#winform'+this.id+'').append(this.content[i]);
	}
}

CusWindow.prototype.setTable=function(table){
	this.tableContent=tableHtml;
}

CusWindow.prototype.renderTable=function(){
	jQuery('#winContent'+this.id+'').append(this.tableContent);
}

CusWindow.prototype.setContent=function(content){
	this.content=content;
}

CusWindow.prototype.render=function(){
	jQuery('#winContent'+this.id+'').append(this.content);
}

CusWindow.prototype.setFootbar=function(footbarContent){
	this.footbarContent=footbarContent;
}

CusWindow.prototype.renderFootbar=function(){
	jQuery('#footbar'+this.id+'').append(this.footbarContent);
}

CusWindow.prototype.setHeaderbar=function(headerbarContent){
	this.headerbarContent=headerbarContent;
}

CusWindow.prototype.setHeaderbarTool=function(tool){
	this.headerbarTool=tool;
}

CusWindow.prototype.renderHeaderbar=function(){
	if(this.headerbarTool!=null&&this.headerbarTool!=undefined){
		jQuery('#headerBar'+this.id+'').append(this.headerbarTool);
	}
	jQuery('#headerBar'+this.id+'').append(this.headerbarContent);
}

CusWindow.prototype.validateUri=function(){
	jQuery('#errorMsgsArea').empty();
	var errorMsg='';
	var needVerification=false;
	var label='';
	jQuery('#winform').children().each(function(index,el){
		var div=jQuery(el);
		if(div.find('span[property="rdfs:comment"]').length>0){
			var type=div.find('span[property="rdfs:comment"]').first().text();
			if(type=='ObjectProperty'){
				needVerification=true;
				label=div.find('span[property="raul:label"]').first().text();
			}
			return;
		}
		
		if(div[0].tagName.toLowerCase()=='input'&&needVerification==true){
			var textbox=div;
			textbox.removeAttr('style');
			var value=textbox.val();
			if(/(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(value)==false){
				errorMsg+='<p style="color:	#FF0000;vertical-align: middle;margin-left: auto;margin-right: auto;">value of "'+label+'" is not a valid URI"</p><br>';
				textbox.attr('style','background-color:#C80000;');
			}
			needVerification=false;
			label='';
		}
	});
	if(errorMsg!=''){
		jQuery('#errorMsgsArea').append(errorMsg);
		return false;
	}
	else
		return true;
}

function Page(obj){
	//how many records will be displayed on one page
	this.recordSize=obj['recordSize'];
	//url for fetching the information that the page wants
	this.activeUrl=obj['activeUrl'];
	//how many pages will be shown on the footbar, the default value is 6
	this.num=-1;
	this.data=obj['data'];
	this.getTotalPages();
	this.about=obj['about'];
	this.link=obj['link'];
	this.window=obj['window'];
}

Page.prototype.getTotalPages=function(){
	var totalNumberOfRecords=jQuery(this.data).children().length;
	this.num=parseInt((totalNumberOfRecords-1)/this.recordSize+1);
}

Page.prototype.getDataSegement=function(pageNumber){
	var startNumberOfRecord=(pageNumber-1)*this.recordSize;
	var endNumberOfRecrod=startNumberOfRecord+this.recordSize-1;
	var content=jQuery('<div/>');
	var self=this;
	jQuery(this.data).children().each(function(index,el){
		if(index>=startNumberOfRecord&&index<=endNumberOfRecrod){
			var ele=jQuery(el);
			ele.unbind('click');
			ele.find('a').first().bind('click',{'instanceUrl':ele,'page':self},function(event){
				fill(event.data.instanceUrl,event.data.page);
			});
			content.append(ele);
		}
	});
	return content;
}

Page.prototype.createPage=function(){
	if(this.num==undefined)
		this.num=5;
	this.previousPage=0;
	var pagination=jQuery('<ul id="pagination-digg"/>');
	pagination.append('<li class="previous-off" id="previous">«Previous</li><li class="active" id="1">1</li>');
	//var htmlForPagination='<ul id="pagination-digg"><li class="previous-off" id="previous">«Previous</li><li class="active" id="1">1</li>';
	var lastPageThisBatch=this.num;
	var classForNext='next';
	if(lastPageThisBatch==1){
		classForNext='next-off';
	}
	for(var i=2;i<=lastPageThisBatch;i++){
		//htmlForPagination+='<li id="'+i+'"><a href="javascript:void(0)" onclick="changePage(this);">'+i+'</a></li>';
		var li=jQuery('<li id="'+i+'"/>');
		var a=jQuery('<a href="javascript:void(0)">'+i+'</a>');
		a.bind('click',{'a':a,'pageComp':this},function(event){
			changePage(event.data.a,event.data.pageComp);
		});
		li.append(a);
		pagination.append(li);
	}
	if(classForNext=='next-off')
		//htmlForPagination+='<li class="'+classForNext+'" id="next">Next »</li></ul>';
		pagination.append('<li class="'+classForNext+'" id="next">Next »</li></ul>');
	else{
		//htmlForPagination+='<li class="'+classForNext+'" id="next"><a href="javascript:void(0)" onclick="forward(this);">Next »</a></li></ul>';
		var classForNextLi=jQuery('<li class="'+classForNext+'" id="next"/>');
		var a=jQuery('<a href="javascript:void(0)">Next »</a>');
		a.bind('click',{'a':a,'pageComp':this},function(event){
			forward(event.data.a,event.data.pageComp);
		});
		classForNextLi.append(a);
		pagination.append(classForNextLi);
	}
	//return htmlForPagination;
	return pagination;
}

Page.prototype.setWindowBody=function(windowBody){
	this.windowBody=windowBody;
}

/*Page.prototype.updatePage=function(currentPage,direction){
	jQuery('#pagination-digg').empty();
	if(direction=='previous'){
		var startPage=currentPage-this.num+1;
		this.previousPage=startPage-1;
		this.nextPage=currentPage+1;
		if(startPage==1)	
			jQuery('#pagination-digg').append('<li class="previous-off">«Previous</li>');
		else
			jQuery('#pagination-digg').append('<li class="previous"><a href="javascript:void(0)" onclick="">«Previous</a></li>');
		for(var i=startPage;i<currentPage;i++){
			jQuery('#pagination-digg').append('<li><a href="javascript:void(0)" onclick="">'+i+'</a></li>');
		}
		jQuery('#pagination-digg').append('<li class="active">'+currentPage+'</a></li>');
		jQuery('#pagination-digg').append('<li class="next"><a href="javascript:void(0)" onclick="">Next »</a></li></ul>');
	}else{
		var startPage=currentPage;
		this.previousPage=currentPage-1;
		var lastPageThisBatch=this.num;
		if(lastPageThisBatch>this.lastPage){
			lastPageThisBatch=this.lastPage;
			classForNext='next-off';
		}
		jQuery('#pagination-digg').append('<li class="previous"><a href="javascript:void(0)" onclick="">«Previous</a></li><li class="active">'+startPage+'</li>');
		for(var i=startPage+1;i<=lastPageThisBatch;i++)
			jQuery('#pagination-digg').append('<li><a href="javascript:void(0)" onclick="">'+i+'</a></li>');
		if(classForNext=='next-off')
			htmlForPagination+='<li class="'+classForNext+'">Next »</li></ul>';
		else
			htmlForPagination+='<li class="'+classForNext+'"><a href="javascript:void(0)" onclick="">Next »</a></li></ul>';
		this.nextPage=lastPageThisBatch+1;
	}
}*/

/*Page.prototype.setTotalRecordNumber=function(totalRecordNum){
	this.totalRecordNum=totalRecordNum;
	if(totalRecordNum%recordSize==0)
		this.lastPage=totalRecordNum/recordSize;
	else
		this.lastPage=totalRecordNum/recordSize+1;
}*/

function fillValue(link){
	var retVal = confirm("Are you sure to select this instance ?");
	if( retVal == false ){
	  return;
   }
	jQuery('body').children().last().remove();
	var ele=jQuery('div[about="'+demoEle+'"]');
		for(;;){
			var eleTagName=ele[0].tagName.toLowerCase();
			if(eleTagName=='a'&&ele.attr('class')=='button')
				break;
			if(eleTagName=='div'){
				ele.find('input').each(function(index,el){
					jQuery(el).removeAttr('disabled');
					jQuery(el).val('http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#2months');
					
				});
			}
			ele=ele.next();
		}
}


function changePage(a,pageComp){
	var pageId=jQuery(a).parent().attr('id');
	var ul=jQuery(a).parent().parent();
	var previous=ul.find('#previous');
	var next=ul.find('#next');
	if(previous.attr('class')=='previous-off'){
		previous.attr('class','previous');
		previous.text('');
		var previousNewA=jQuery('<a href="javascript:void(0)">«Previous</a>');
		previousNewA.bind('click',{'a':previousNewA,'pageComp':pageComp},function(event){
			backward(event.data.a,event.data.pageComp);
		});
		previous.append(previousNewA);
	}
	if(next.attr('class')=='next-off'){
		next.attr('class','next');
		next.text('');
		var nextNewA=jQuery('<a href="javascript:void(0)">Next»</a>');
		nextNewA.bind('click',{'a':nextNewA,'pageComp':pageComp},function(event){
			forward(event.data.a,event.data.pageComp);
		});
		next.append(nextNewA);
	}
	if(pageId=='1'){
		var previous=ul.find('#previous');
		if(previous.children().length>0)
			previous.empty();
		previous.attr('class','previous-off');
		previous.text('«Previous');
	}
	if(pageId==pageComp.num+''){
		var next=ul.find('#next');
		if(next.children().length>0)
			next.empty();
		next.attr('class','next-off');
		next.text('Next »');
	}
	var previousPage=ul.find('li[class="active"]').first();
	previousPage.removeAttr('class');
	previousPage.text('');
	var newA=jQuery('<a href="javascript:void(0)">'+previousPage.attr('id')+'</a>');
	newA.bind('click',{'a':newA,'pageComp':pageComp},function(event){
		changePage(event.data.a,event.data.pageComp);
	});
	previousPage.append(newA);
	//previousPage.append('<a href="javascript:void(0)">'+previousPage.attr('id')+'</a>');
	var li=jQuery(a).parent();
	li.empty();
	li.text(li.attr('id'));
	li.attr('class','active');
	var data=pageComp.getDataSegement(parseInt(li.attr('id')));
	pageComp.windowBody.empty();
	pageComp.windowBody.append(data);
}

function forward(a,pageComp){
	var ul=jQuery(a).parent().parent();
	var previous=ul.find('#previous');
	if(previous.attr('class')=='previous-off'){
		previous.attr('class','previous');
		previous.text('');
		var previousNewA=jQuery('<a href="javascript:void(0)">«Previous</a>');
		previousNewA.bind('click',{'a':previousNewA,'pageComp':pageComp},function(event){
			backward(event.data.a,event.data.pageComp);
		});
		previous.append(previousNewA);
	}
	var currentPage=ul.find('li[class="active"]').first();
	var currentPageId=currentPage.attr('id');
	var nextPageId=''+(parseInt(currentPageId)+1);
	if(nextPageId==pageComp.num+''){
		var next=ul.find('#next');
		if(next.children().length>0)
			next.empty();
		next.attr('class','next-off');
		next.text('Next »');
	}
	currentPage.removeAttr('class');
	currentPage.text('');
	var currentNewA=jQuery('<a href="javascript:void(0)">'+currentPage.attr('id')+'</a>');
	currentNewA.bind('click',{'a':currentNewA,'pageComp':pageComp},function(event){
		changePage(event.data.a,event.data.pageComp);
	});
	currentPage.append(currentNewA);
	var nextPage=ul.find('#'+nextPageId);
	nextPage.empty();
	nextPage.text(nextPage.attr('id'));
	nextPage.attr('class','active');
	var data=pageComp.getDataSegement(parseInt(nextPage.attr('id')));
	pageComp.windowBody.empty();
	pageComp.windowBody.append(data);
}

function backward(a,pageComp){
	var ul=jQuery(a).parent().parent();
	var next=ul.find('#next');
	if(next.attr('class')=='next-off'){
		next.attr('class','next');
		next.text('');
		var nextNewA=jQuery('<a href="javascript:void(0)">Next»</a>');
		nextNewA.bind('click',{'a':nextNewA,'pageComp':pageComp},function(event){
			forward(event.data.a,event.data.pageComp);
		});
		next.append(nextNewA);
	}
	var currentPage=ul.find('li[class="active"]').first();
	var currentPageId=currentPage.attr('id');
	var previousPageId=''+(parseInt(currentPageId)-1);
	if(previousPageId=='1'){
		var previous=ul.find('#previous');
		if(previous.children().length>0)
			previous.empty();
		previous.attr('class','previous-off');
		previous.text('«Previous');
	}
	currentPage.removeAttr('class');
	currentPage.text('');
	var currentNewA=jQuery('<a href="javascript:void(0)">'+currentPage.attr('id')+'</a>');
	currentNewA.bind('click',{'a':currentNewA,'pageComp':pageComp},function(event){
		changePage(event.data.a,event.data.pageComp);
	});
	currentPage.append(currentNewA);
	var previousPage=ul.find('#'+previousPageId);
	previousPage.empty();
	previousPage.text(previousPage.attr('id'));
	previousPage.attr('class','active');
	var data=pageComp.getDataSegement(parseInt(previousPage.attr('id')));
	pageComp.windowBody.empty();
	pageComp.windowBody.append(data);
}

function chooseExisting(){
	
}

var tableHtml='<div id="result"><div id="result1" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI" 				   onclick="fillValue(this)">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#2months</a><p class="resultDesc">is property of : 1 months</p></div>'+
			  '<div id="result2" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#6months</a><p class="resultDesc">is property of : 2 months</p></div>'+
			  '<div id="result3" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#8months</a><p class="resultDesc">is property of : 3 months</p></div>'+
			  '<div id="result4" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#12months</a><p class="resultDesc">is property of : 4 months</p></div>'+
			  '<div id="result5" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 5 months</p></div>'+
			  '<div id="result6" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 6 months</p></div>'+
			  '<div id="result7" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 7 months</p></div>'+
			  '<div id="result8" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 8 months</p></div>'+
			  '<div id="result9" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 9 months</p></div>'+
		      '<div id="result10" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#10months</a><p class="resultDesc">is property of : 10 months</p></div></div>';
			  
/*var tableHtml='<div id="result"><div id="result1" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI" 				   onclick="fillValue(this)">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#2months</a><p class="resultDesc">is property of : 1 months</p></div>'+
			  '<div id="result2" style="float:left;clear:both;padding:0px 0px 16px 0px;"><a href="javascript:void(0)" class="resultURI">http://w3c.org.au/raul/service/public/forms/ssnSurvivalProperty#6months</a><p class="resultDesc">is property of : 2 months</p></div></div>';*/
			  					  