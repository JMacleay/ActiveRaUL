package ie.deri.raul.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/util")
public class UtilResource {

	protected Log log=LogFactory.getLog(UtilResource.class);
	
	public UtilResource(){
		log.info("initialize utility resource finished.");
	}
	
	@GET
	@Path("/extraInfo")
	@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getTestHtml(@QueryParam("url")String url){
		String responseBody=null;
		JSONObject object=new JSONObject();
		if(url!=null){
			try {
				responseBody=HttpClientUtil.getRemoteContent(url);
				object.put("message","success");
				object.put("body", responseBody);
				return Response.ok(object.toString()).type(MediaType.APPLICATION_JSON)
						.build();
			} catch (Exception e) {
				e.printStackTrace();
				log.error(this, e);
				object.put("message", "error");
				return Response.ok(object.toString()).type(MediaType.APPLICATION_JSON)
						.build();
			}
		}else{
			//for searching the local repo to find out user-desired triples
			object.put("message","success");
			return Response.ok(object.toString()).type(MediaType.APPLICATION_JSON)
					.build();
		}
		//return Response.ok(object.toString()).type(MediaType.APPLICATION_JSON).build();
	}
	
}
