$(document).ready(
	function(){
			$('#formcreate').click(function() {
				$.ajax({					
					type: 'POST',												  
					url: 'http://w3c.org.au/raul/service/public/forms',
					processData: false,
					contentType: 'application/rdf+xml',					
					data: document.getElementById('formdef').value,
	
					success: function(data, textStatus, xhr){
								$("input#delformuri").val(xhr.getResponseHeader('Location'));
								$("input#getformuri").val(xhr.getResponseHeader('Location'));
								$("input#postdatauri").val(xhr.getResponseHeader('Location'));
								document.getElementById('datapost').innerHTML = "Post data to " + xhr.getResponseHeader('Location');
								
								$.ajax({
									beforeSend: function(req){
													req.setRequestHeader("Accept", "application/xhtml+xml");
												},
									type: 'GET',
									url: xhr.getResponseHeader('Location'),
									dataType: 'html',
									success: function(data, textStatus, xhr){
												document.getElementById('content').innerHTML = data;
												document.getElementById("datapost").disabled = false;
											}
									});
							}
				});
			});
						
			$('#formdelete').click(function() {
				$.ajax({					
					type: 'DELETE',												  
					url: $("input#delformuri").val(),
					processData: false,
					contentType: 'application/rdf+xml',					
					
					success: function(data, textStatus, xhr){
								alert($("input#delformuri").val() + " was deleted.");
							}
				});
			});
			
			$('#formget').click(function(){
				alert("get " + $("input#getformuri").val());
				
				$.ajax({
						beforeSend: function(req){
										req.setRequestHeader("Accept", "application/xhtml+xml");
									},
						type: 'GET',
						url: $("input#getformuri").val(),
						dataType: 'html',
						success: function(data, textStatus, xhr){
									document.getElementById('content').innerHTML = data;
								}
				});
			});
			
			$('#datapost').click(function(){
				alert("Post data to " + $("input#postdatauri").val());
				
				$.ajax({
						type: 'POST',												  
						url: $("input#postdatauri").val(),
						processData: false,
						contentType: 'application/rdf+xml',					
						data: document.getElementById('formdef').value,
	
						success: function(data, textStatus, xhr){
								
								$.ajax({
									beforeSend: function(req){
													req.setRequestHeader("Accept", "application/xhtml+xml");
												},
									type: 'GET',
									url: xhr.getResponseHeader('Location'),
									dataType: 'html',
									success: function(data, textStatus, xhr){
												document.getElementById('content').innerHTML = data;
											}
								});
						}
				});
			});
		
      		$('#submit').click(function() {
			var rdf = parseDom();
			log.info(rdf.databank.dump({format:'application/rdf+xml', serialize: true}));
			// $("#frame").append("<div>"+rdf.databank.dump({format:'application/rdf+xml', serialize: true})+"</div>");
			
			/*
			var methodName="", methodType="";
			methodName = $("span[property='raul:method']").text();
			methodType = $("div[about='"+ methodName + "']").attr("typeof");

			if (methodType == "raul:CREATEOperation") {
				methodType = "POST";
			} else if (methodType == "raul:READOperation") {
				methodType = "GET";
			} else if (methodType == "raul:UPDATEOperation") {
				methodType = "PUT";
			} else if (methodType == "raul:DELETEOperation") {
				methodType = "DELETE";
			}
			*/

			//alert($('form').attr('method'));	
			//alert(methodType);
			
			
			$.ajax({
				//type: methodType,
				type: 'POST',												  
				url: 'http://w3c.org.au/raul/service/public/forms',
				processData: false,
				contentType: 'application/rdf+xml',
				data: rdf.databank.dump({format:'application/rdf+xml', serialize: true}),

				success: function(data, textStatus, xhr){
					// alert('Success:\ntextStatus: ' + textStatus + '\nxhr: ' + xhr.getAllResponseHeaders());
					// $('body').append('<div id="ajaxResponse">'+xhr.getAllResponseHeaders()+'</div>');
					// document.getElementById('frame').innerHTML = xhr.getResponseHeader('Location');

					$.ajax({
							beforeSend: function(req){
									req.setRequestHeader("Accept", "application/xhtml+xml");
							},
							type: 'GET',
							url: xhr.getResponseHeader('Location'),
							dataType: 'html',
							success: function(data, textStatus, xhr){
									// alert("Data Loaded: " + data);
									document.getElementById('content').innerHTML = data;
									// document.getElementById('frame').innerHTML = (new XMLSerializer()).serializeToString(data);
								}
						});

					}
				});
	
			return false;
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
