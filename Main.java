import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class Main {
	public static String gettingHTMLSOURCE(String url)
	{
		String content="";
		Process p;
		try
		{
			Runtime rt = Runtime.getRuntime();
			p=rt.exec("F:\\university\\URA\\phantomjs-1.9.7-windows\\phantomjs.exe F:\\university\\URA\\phantomjs-1.9.7-windows\\dom.js 2000 "+url);
			//p.waitFor();
			BufferedReader stdOut=new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String s;
	        while((s=stdOut.readLine())!=null){
	        	if(s.contains("\"xhtml\": "))
		        {
		        	content = s;
		        	break;
		        }
	        }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return content;
	}
	
	public static void parseAdobeLinks(String info)
	{
		int refstart = 0;
		ArrayList<String> postsURL=new ArrayList<String>();
		while (info.indexOf("href=\\\"", refstart)!=-1)
		{
			refstart=info.indexOf("href=\\\"", refstart)+7;
			String ref = info.substring(refstart, info.indexOf("\\\"", refstart));
			if(ref.contains("https")&&ref.contains("message"))
				postsURL.add(ref);
			refstart=info.indexOf("\\\"", refstart);
		}
		for(int i=0;i<postsURL.size();i++)
		{
			parseAdobe(postsURL.get(i));
			System.out.println(postsURL.get(i));
		}
	}
	
	public static void parseAdobe(String link)
	{
		URL url;
	    InputStream is = null;
	    BufferedReader br;
	    String line;

	    try {
	        url = new URL(link);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));
	        
	        File output = new File("output.csv");
	        boolean existBefore = true;
			if (!output.exists()) {
				existBefore = false;
			}
			
			FileWriter fw2 = new FileWriter(output.getAbsoluteFile(),true);
			BufferedWriter bw2 = new BufferedWriter(fw2);
	      
			if(!existBefore)
			{
				bw2.append("URL, Question Title, Time Stamp, Initial Question, Responses");
				bw2.append("\n");
			}
			
			bw2.append(link+",");
			
			boolean topic = false;
			boolean detail = false;
			boolean reply = false;
			boolean date = false;
			String detailContent = "";
			String replyContent = "";
			String topicContent = "";
			String dateContent = "";
			String prev = "";
	        while ((line = br.readLine()) != null) 
	        {		
	        	if(prev.compareTo("")!=0)
	        		line=prev+line;
	        	
	        	if (topic)
	        	{
	        		int startIndex = line.indexOf('>')+1;
	        		int endIndex = line.indexOf('<',startIndex);
	        		topicContent += line.substring(startIndex, endIndex)+"\",";
	        		bw2.append(topicContent);
	        		
	        		topic = false;
	        		topicContent="";
	        	}
	        	else if(reply && line.contains("<p>"))
	        	{
	        		if(line.contains("<iframe")&&prev.compareTo("")==0)
	        		{
	        			prev=line;
	        			continue;
	        		}
	        		int startIndex=0; 
	        		int endIndex=0;
	        		
	        		while(true)
	        		{
	        			startIndex= line.indexOf("<p>",endIndex)+3;
	        			endIndex = line.indexOf("</p>",startIndex);
	        			if(startIndex-3==-1 )
	        				break;
	        			replyContent+=line.substring(startIndex, endIndex);
	        		}
	        		if(replyContent.contains("href=\""))
	        		{
	        			int refstart = 0;
	        			while (replyContent.indexOf("href=\"", refstart)!=-1)
	        			{
	        				refstart=replyContent.indexOf("href=\"", refstart)+6;
	        				String ref = replyContent.substring(refstart, replyContent.indexOf("\"", refstart));
	        				replyContent=replyContent+" "+ref;
	        				refstart=replyContent.indexOf("\"", refstart);
	        			}
	        		}
	        		if(replyContent.contains("<iframe"))
	        		{
	        			int refstart = replyContent.indexOf("<iframe", 0);
	        			while (replyContent.indexOf("src=\"", refstart)!=-1)
	        			{
	        				refstart=replyContent.indexOf("src=\"", refstart)+5;
	        				String ref = replyContent.substring(refstart, replyContent.indexOf("\"", refstart));
	        				replyContent=replyContent+" "+ref;
	        				refstart=replyContent.indexOf("\"", refstart);
	        			}
	        		}
	        		replyContent+="\",";
	        		replyContent=replyContent.replaceAll("<.*?>", "");
	        		replyContent=replyContent.replaceAll("&.*?;", "");
	        		bw2.append(replyContent);
	        		reply=false;
	        		replyContent="";
	        		prev="";
	        	}
	        	else if(detail && line.contains("<p>"))
	        	{
	        		if(line.contains("<iframe")&&prev.compareTo("")==0)
	        		{
	        			prev=line;
	        			continue;
	        		}
	        		
	        		int startIndex=0; 
	        		int endIndex=0;
	        		while(true)
	        		{
	        			startIndex= line.indexOf("<p>",endIndex)+3;
	        			endIndex = line.indexOf("</p>",startIndex);
	        			if(startIndex-3==-1)
	        				break;
	        			detailContent+=line.substring(startIndex, endIndex);
	        		}
	        		if(detailContent.contains("href=\""))
	        		{
	        			int refstart = 0;
	        			while (detailContent.indexOf("href=\"", refstart)!=-1)
	        			{
	        				refstart=detailContent.indexOf("href=\"", refstart)+6;
	        				String ref = detailContent.substring(refstart, detailContent.indexOf("\"", refstart));
	        				detailContent=detailContent+" "+ref;
	        				refstart=detailContent.indexOf("\"", refstart);
	        			}
	        		}
	        		if(detailContent.contains("<iframe"))
	        		{
	        			int refstart = detailContent.indexOf("<iframe", 0);
	        			while (detailContent.indexOf("src=\"", refstart)!=-1)
	        			{
	        				refstart=detailContent.indexOf("src=\"", refstart)+5;
	        				String ref = detailContent.substring(refstart, detailContent.indexOf("\"", refstart));
	        				detailContent=detailContent+" "+ref;
	        				refstart=detailContent.indexOf("\"", refstart);
	        			}
	        		}
	        		detailContent+="\",";
	        		detailContent=detailContent.replaceAll("\\<.*?>", "");
	        		detailContent=detailContent.replaceAll("&.*?;", "");
	        		bw2.append(detailContent);
	        		detail=false;
	        		detailContent="";
	        		prev="";
	        	}
	        	else if(date && line.contains("</strong>"))
	        	{
	        		dateContent+=line.substring(line.indexOf("</strong>")+9)+"\",";
	        		bw2.append(dateContent);
	        		date=false;
	        	}
	        	else if(line.contains("h1")&&!line.contains("/h1"))
	        	{
	        		topic=true;
	        		topicContent="\"";
	        	}
	        	else if(line.contains("class=\"reply\""))
	        	{
	        		reply=true;
	        		replyContent = "\"";
	        	}
	        	else if(line.contains("class=\"j-original-message\""))
	        	{
	        		detail=true;
	        		detailContent = "\"";
	        	}
	        	else if(line.contains("class=\"j-post-author\""))
	        	{
	        		date= true;
	        		dateContent = "\"";
	        	}
	        }
	        bw2.append("\n");
			bw2.flush();
			bw2.close();
 
	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	         ioe.printStackTrace();
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	    }
	}
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		String forum = "Adobe";
		String url = "https://forums.adobe.com/search.jspa?q=screenshot";
		String content=gettingHTMLSOURCE(url);
		
		System.out.println(content);
		
		if(forum.compareTo("Adobe")==0)
		{
			parseAdobeLinks(content);
		}
	}
}
