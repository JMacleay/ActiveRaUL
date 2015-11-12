//global window variable used when resizing the window during the first search
var okno=null;
//search type
var sindceSearchTypeGlobal='keywordSearch';

var sindiceResultID="";

/* 
Action performed when user presses a link from search resaults in Sindice search window
 */
function sindiceSearchBoxLinkSelected(link){
	//set JDOM input type object with link text
	//document.searchForm.sindiceResult.value=link;
	
	
	link = link.replace("field=explicit_content&output=json&", "");
	link = link.replace("v3", "v2");
	document.getElementById(sindiceResultID).value = link;
	jQuery.ajax({
		beforeSend: function(req){req.setRequestHeader("Accept", "application/rdf+xml");},
		type: 'GET',
		url: link,
		dataType: 'xml',
		success: function(data, textStatus, xhr){						
				var rdfString = (new XMLSerializer()).serializeToString(data);
				parseData(rdfString, "");
				rdfString = formatXml(rdfString);
				editor.setValue(rdfString);
				hideProcessingInfo();
		}
	});
}

/*
 Sindice search window initial render and fill with static content
 @searchType indicates the search types available from the widget, possible options: 'keywordSearch' , 'uriSearch', 'tab'
 @param positionTop y coord of the window 
 @param positionLeft x coord of the window
 */
//function sindiceSearchWindow(searchType, positionTop, positionLeft)
function sindiceSearchWindow(searchType, resultID, positionTop, positionLeft)
{
	sindiceResultID = resultID;
	if (positionTop==null) positionTop=10;
	if (positionLeft==null) positionLeft=400;
	var win = null;
	if (document.getElementById('sindiceSearchWindow')!=null) return;
	//	alert(document.getElementById('sindiceSearchWindow'));
	//test window (different size)
	if (searchType==null || (searchType!=null && searchType=='all'))
		win = new Window("sindiceSearchWindow",{className: "mac_os_x_custom", width:450, height:67, zIndex: 100, maxHeight:500, resizable: true, title: "Sindice search", showEffect: Element.show, hideEffect: Element.hide, draggable:true, wiredDrag: false}); 
 	else 
 		win = new Window("sindiceSearchWindow",{className: "mac_os_x_custom", width:450, height:61, zIndex: 100, maxHeight:500, resizable: true, title: "Sindice search", showEffect: Element.show, hideEffect: Element.hide, draggable:true, wiredDrag: false}); 
		//if (searchType!=null && searchType=='uri')
	//put some initial content
	win.getContent().innerHTML= "<div id='sindiceSearchBox' style='padding:10px'></div>" 
	//check if search input field is set (in case window was previously renderd and is only refreshed now)
		
	//var searchText = document.searchForm.sindiceResult.value;
	var searchText = document.getElementById(sindiceResultID).value;
	
	
	if (document.getElementById("sindiceSearchText")!=null)
		searchText = document.getElementById("sindiceSearchText").value;
	//fill div tag with static search window content (search form + logo)
	var static_content="";
	static_content=static_content+"<FORM name=\"sindiceSearchForm\" id=\"sindiceSearchForm\" method=\"POST\" action=\"\" onsubmit=\"embedSindiceDynamicTag('keywordSearch'); return false;\" style='margin-bottom: 3px; margin-top:3px;'>";
	static_content=static_content+"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+"<td rowspan=\"2\" valign=\"bottom\"><img src=\"sindice/pic/sindice_logo.gif\" width=\"70\" height=\"25\"/>&nbsp; &nbsp;</td>";
	static_content=static_content+"<td align=\"left\">";
	if (searchType==null || (searchType!=null && searchType=='all'))
	{
		static_content=static_content+"<ul class='basictab'>"
								+"<li id='sindiceSearchBoxKeywordTab' class='selected'>"
								+"<a href=\"#\" onclick=\"sindiceChangeSearchType('keyword');return false;\">keyword</a>"
								+"</li>"
    							+"<li id='sindiceSearchBoxUriTab'>"      
    							+"<a href=\"#\" onclick=\"sindiceChangeSearchType('uri');return false;\">uri</a>"
    							+"</li>"
    							+"</ul>";
	}
    static_content=static_content+"</td><td></td></tr>";
	static_content=static_content+"<tr>"
    				+ "<td><input type=\"text\" id=\"sindiceSearchText\" name=\"sindiceSearchText\" size=\"25\" value=\""+searchText+"\"></td>" 
    				+ "<td>&nbsp; <input type=\"submit\" id=\"sindiceStartSearchButton\" value=\"Search\"></td>"
 					+ "</tr></table></FORM>"
 					+"<div id='sindiceSearchBoxDynamicContent'>"
 					+"</div>";
	document.getElementById('sindiceSearchBox').innerHTML=static_content;
	//show and position window
	win.setStatusBar("Type in the search string");
	win.setDestroyOnClose();
	win.showCenter(false,positionTop,positionLeft);
	okno=win;
}

/*
 AJAX callback function, this is executed when the JSON response is appended into the webpage
 */
function set_search_win_content(jSON)
{
var dynamic_content="";
if (jSON!=null)
{
  dynamic_content="<hr/>";
  if (jSON.totalResults==0)
		dynamic_content=dynamic_content+"</br>"
									   +"<b>Didn't find any document for "+document.getElementById("sindiceSearchText").value+"</b>";
  var linkName="";
  for (var i=0;i<jSON.entries.length;i++)
  {
  	var entry = jSON.entries[i];
	
  	var linkName = entry.link;
  	
  	if(entry.title && entry.title.length>0){
  		linkName = "";
	  	for(var j=0;j<entry.title.length;j++){
	  		var t = entry.title[j];
	  		linkName += " "+t.value;
	  	}
  	}
	dynamic_content=dynamic_content+"<a href=\"#\" onclick=\"sindiceSearchBoxLinkSelected('"+ entry.cache +"');return false;\">"+linkName+"</a> <br/>"
								   +"<div style='margin-left: 1em; margin-bottom: .3em'>"
								   +"<div style='color: #5e5e5e; font-size:medium'>"
    							   +entry.updated
    							   +"&nbsp;&ndash;&nbsp;"
    							   +entry.explicit_content_size+" triples"
    							   +"&nbsp;&ndash;&nbsp;"
    							   +entry.explicit_content_length+" b"
               					   +"</div>"
              					   +"<div style='color: green'>"
              					   +entry.link
              					   +"</div>"
              					   +"</div>";
  }
 }
 document.getElementById('sindiceSearchBoxDynamicContent').innerHTML=dynamic_content;
}

/* 
 Query sindice search engine. Perfoms interface operations that have to be done before 
 the search(extend window) and embeds json response from sindice into the webpage 
 */
function embedSindiceDynamicTag(searchType)
{
	//var searchType=""; 
	
	searchPrefix="http://api.sindice.com/v3/search?a_t=old-sindice-widget&q="; //begining of the search string , it can very in case of keyword/URI/property search
	
	var searchString = null;
	if (document.getElementById("sindiceSearchText")!=null)
		searchString = document.getElementById("sindiceSearchText").value;

	if (searchString!=null && searchString!="" && searchString!=" ")
	{
	    var request = searchPrefix+escape(searchString)+"&format=json&callback=sindice";
	    //alert(request);
    	var head = document.getElementsByTagName("head").item(0);
	    var script = document.createElement("script");
    	script.setAttribute("type", "text/javascript");
	    script.setAttribute("src", request);
    	head.appendChild(script);
    	//resize search window to make room for search results
    	new Effect.ResizeWindow(okno, okno.getLocation().top,okno.getLocation().left, 450, 500, {duration: 2});
    	//insert a horizontal line and loading animation until search results are shown
    	document.getElementById('sindiceSearchBoxDynamicContent').innerHTML="<hr/>"
    							+"<br/>"
    							+"<br/>"
    							+"<br/>"
    							+"<br/>"
    							+"<br/>"
    							+"<br/>"
    							+"<center id=\"loadingSection\" style=\"display: none;\">"
								+"<img src=\"sindice/pic/load.gif\" width=\"24\" height=\"24\"/>"
								+"</br>"
								+"<div style='color: #5e5e5e; font-size:medium'>Loading</div>"
    							+"</center>";
		Effect.Appear('loadingSection');
		Effect.Appear('loadingSection', { duration: 2.0 });    							
	}
	else 
	{ //if the search string wasnt provided by the user, reset the results
		if (document.getElementById('sindiceSearchBoxDynamicContent').innerHTML!="")
			document.getElementById('sindiceSearchBoxDynamicContent').innerHTML="<hr/>";

	}
	//end sindice
}

/* 
 * Action performed when user presses a tab to choose search type between keyword and uri search (if activated)
 */
function sindiceChangeSearchType(searchType)
{
	if (searchType=='uri')
	{
		document.getElementById('sindiceSearchBoxKeywordTab').className='';
		document.getElementById('sindiceSearchBoxUriTab').className='selected';
		//change actions for button and enter event in search box
		//document.getElementById('sindiceStartSearchButton').onclick=function() { embedSindiceDynamicTag('uriSearch');return false; }; //search button
		//submit event for the entire form
		document.getElementById('sindiceSearchForm').onsubmit=function() { embedSindiceDynamicTag('uriSearch');return false; };
	}
	else
	{
		document.getElementById('sindiceSearchBoxUriTab').className='';
		document.getElementById('sindiceSearchBoxKeywordTab').className='selected';
		//change actions for button and enter event in search box
		//document.getElementById('sindiceStartSearchButton').onclick=function() { embedSindiceDynamicTag('keywordSearch');return false; };
		//submit event for the entire form
		document.getElementById('sindiceSearchForm').onsubmit=function() { embedSindiceDynamicTag('keywordSearch');return false; };
	}
}

