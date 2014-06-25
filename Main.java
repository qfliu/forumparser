import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class Main {
	public static String gettingHTMLSOURCE(String url)
	{
		String content="";
		Process p;
        boolean repeat = false;
        while(true) {
            try {
                Runtime rt = Runtime.getRuntime();
                p = rt.exec("../phantomjs-1.9.7-macosx/bin/phantomjs ../phantomjs-1.9.7-macosx/dom.js 2000 " + url);
                //p.waitFor();
                BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String s;
                while ((s = stdOut.readLine()) != null) {
                    if(s.contains("\"actual_url\""))
                    {
                        System.out.println(s);
                        int index = s.indexOf(": \"")+3;
                        System.out.println(index);
                        String actualURL = s.substring(index, s.indexOf("\"",index));
                        System.out.println(actualURL);
                        if(actualURL.compareTo(url)!=0) {
                            repeat = true;
                            url = actualURL;
                            break;
                        }
                    }
                    if (s.contains("\"xhtml\": ")) {
                        content = s;
                        repeat=false;
                        System.out.println("lala");
                        break;
                    }
                }
                if(repeat==false)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("finish");
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

    public static void parseAutoDeskLinks(String info)
    {
        int refstart = 0;
        int indicator = 0;
        ArrayList<String> postsURL=new ArrayList<String>();
        while (info.indexOf("id=\\\"topicMessageLink\\\"", indicator)!=-1)
        {
            indicator = info.indexOf("id=\\\"topicMessageLink\\\"", indicator);
            refstart=info.indexOf("href=\\\"", indicator)+7;
            String ref = info.substring(refstart, info.indexOf("\\\"", refstart));
            if(ref.contains("/t5/"))
                postsURL.add(ref);
            refstart=info.indexOf("\\\"", refstart);
            indicator = refstart;
        }
        for(int i=0;i<postsURL.size();i++)
        {
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
            boolean replier = false;
			boolean date = false;
            boolean findDate = false;
            boolean author = false;
            String authorName ="";
            String replyerInfo = "";
			String detailContent = "";
			String replyContent = "";
			String topicContent = "";
			String dateContent = "";
			String prev = "";
            String links = "";
            int counter = 0;
	        while ((line = br.readLine()) != null) 
	        {		
	        	if(prev.compareTo("")!=0)
	        		line=prev+line;

	        	if (topic)
	        	{
	        		int startIndex = line.indexOf('>')+1;
	        		int endIndex = line.indexOf('<',startIndex);
	        		topicContent += "\""+line.substring(startIndex, endIndex).replaceAll("\"","")+"\",";
	        		bw2.append(topicContent);
	        		
	        		topic = false;
	        		topicContent="";
	        	}
                else if(replier && line.contains("</a>"))
                {

                    replyerInfo = line.substring(line.indexOf(">")+1, line.indexOf("</a>"));
                    findDate = true;
                }
                else if(author && line.contains("</a>"))
                {
                    authorName = line.substring(line.indexOf(">")+1, line.indexOf("</a>"));
                    dateContent+= "\""+authorName+"\n"+line.substring(line.indexOf("</strong>")+9).replaceAll("\"","")+"\",";
                    bw2.append(dateContent);
                    date=false;
                    author=false;
                }
                else if(findDate && line.compareTo("")!=0 )
                {
                    replyerInfo+=", "+line.trim();
                    replier=false;
                    findDate = false;
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
                            links += (ref+" ");
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
                            links += (ref+" ");
	        				refstart=replyContent.indexOf("\"", refstart);
	        			}
	        		}
                    replyContent=replyContent.replaceAll("\"","");
	        		replyContent="\"[ "+replyerInfo+" ]"+"\n"+replyContent+"\",";
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
                            links += (ref+" ");
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
                            links += (ref+" ");
	        				refstart=detailContent.indexOf("\"", refstart);
	        			}
	        		}
                    detailContent=detailContent.replaceAll("\"","");
	        		detailContent="\""+detailContent+"\",";
	        		detailContent=detailContent.replaceAll("\\<.*?>", "");
	        		detailContent=detailContent.replaceAll("&.*?;", "");
	        		bw2.append(detailContent);
	        		detail=false;
	        		detailContent="";
	        		prev="";
	        	}
	        	else if(line.contains("h1")&&!line.contains("/h1"))
	        	{
	        		topic=true;
	        		topicContent="";
	        	}
	        	else if(line.contains("class=\"reply\""))
	        	{
	        		reply=true;
	        		replyContent = "";
	        	}
	        	else if(line.contains("class=\"j-original-message\""))
	        	{
	        		detail=true;
	        		detailContent = "";
	        	}
	        	else if(!reply && line.contains("class=\"j-post-author\""))
	        	{
                    author = true;
                    authorName = "";
                    dateContent="";
	        	}
                else if(reply && line.contains("class=\"j-post-author \""))
                {
                    replier = true;
                    replyerInfo = "";
                }
	        }
            if(links.compareTo("")!=0) {
                bw2.append("\"" + links + "\",");
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
	            System.out.println("Something is wrong");
	        }
	    }
	}

    public static void parseGoogleLinks(String link)
    {
        String source = gettingHTMLSOURCE(link);

        int tmp=0;
        ArrayList<String> postsURL=new ArrayList<String>();
        ArrayList<String> numPosts = new ArrayList<String>();
        int counter=0;
        while(source.indexOf("<a class=\\\"GFO--0QPL\\\"",tmp)!=-1)
        {
            tmp = source.indexOf("<a class=\\\"GFO--0QPL\\\"",tmp);
            tmp = source.indexOf("href=\\\"",tmp)+7;
            String url = source.substring(tmp, source.indexOf("\\\"",tmp));
            tmp = source.indexOf("\\\"",tmp);
            postsURL.add(url);

            tmp = source.indexOf("<span class=\\\"GFO--0QOQ\\\"",tmp);
            String num = source.substring(source.indexOf(">", tmp) + 1, source.indexOf(" post", tmp));
            numPosts.add(num);

            System.out.println(url+" "+num);
            counter++;
        }

        for(int i=0;i<postsURL.size();i++)
        {
            System.out.println("https://productforums.google.com/forum/"+postsURL.get(i)+"%5B1-"+numPosts.get(i)+"-false%5D");
            //parseGoogle("https://productforums.google.com/forum/"+postsURL.get(i)+"%5B1-"+numPosts.get(i)+"-false%5D");
        }

        System.out.println(counter);
    }

    public static void parseGoogle(String link)
    {
        String source = gettingHTMLSOURCE(link);
        boolean question = true;
        System.out.println(source);

        try{
            File output = new File("output-google.csv");
            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);

            int tmp=0;

            while(true) {
                //find topic
                if(question) {
                    tmp = source.indexOf("<span class=\\\"GFO--0QICC\\\"", tmp);
                    System.out.println(source.indexOf(">", tmp));
                    System.out.println(source.indexOf("</span>", tmp));
                    System.out.println(source.substring(tmp));
                    String topic = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</span>", tmp));
                    System.out.println("topic: " + topic);
                    question = false;
                }

                //find author
                tmp = source.indexOf("class=\\\"GFO--0QCVB\\\"", tmp);
                if(tmp==-1)
                    break;
                String author = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</span>", tmp));
                System.out.println("author: " + author);

                //find date
                tmp = source.indexOf("class=\\\"GFO--0QJFB", tmp);
                tmp = source.indexOf("title=\\\"", tmp);
                String date = source.substring(tmp + 8, source.indexOf("\\\"", tmp+8));
                System.out.println("date: " + date);

                //find question
                tmp = source.indexOf("class=\\\"GFO--0QIFB\\\"", tmp);
                tmp = source.indexOf("<div dir=\\\"ltr\\\"", tmp);
                String content = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("<a class=\\\"gwt-Anchor\\\"", tmp));
                content = content.replaceAll("<.*?>", "");
                System.out.println("content: " + content);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void parseAutoDesk(String link)
    {
        String source = gettingHTMLSOURCE(link);
        boolean question = true;

        try{
            File output = new File("output-autoDesk.csv");
            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);

            int tmp=0;

            while(true) {
                //find topic
                if(question) {
                    tmp = source.indexOf("<div class=\\\"first-message", tmp);
                    String topic = source.substring(source.indexOf("<h1>", tmp) + 3, source.indexOf("</h1>", tmp));
                    topic = topic.substring(topic.lastIndexOf(">")+1);
                    System.out.println("topic: " + topic);
                    question = false;
                }

                //find author
                tmp = source.indexOf("<span class=\\\"UserName lia-user-name",tmp);
                if(tmp==-1)
                    break;
                tmp = source.indexOf("<span class",tmp+22);
                String author = source.substring(source.indexOf(">",tmp)+1, source.indexOf("</span",tmp));
                System.out.println("author: "+author);

                //find time
                tmp = source.indexOf("<div class=\\\"autodesk-reply-time",tmp);
                String date = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</div>", tmp)+1);
                date = date.substring(0, date.indexOf("<"));
                System.out.println("date: " + date);

                //find content
                tmp = source.indexOf("<div class=\\\"lia-message-body-content",tmp);
                String content = source.substring(tmp,source.indexOf("</div>",tmp));
                content=content.replaceAll("<.*?>", "");
                content=content.replaceAll("&.*?;", "");
                System.out.println(content);

                System.out.println();
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void parseMicrosoft(String link)
    {
        String source = gettingHTMLSOURCE(link);
        boolean question = true;

        try{
            File output = new File("output-microsoft.csv");
            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);

            int tmp=0;

            while(true) {
                //find topic
                if(question) {
                    tmp = source.indexOf("<div class=\\\"first-message", tmp);
                    String topic = source.substring(source.indexOf("<h1>", tmp) + 3, source.indexOf("</h1>", tmp));
                    topic = topic.substring(topic.lastIndexOf(">")+1);
                    System.out.println("topic: " + topic);
                    question = false;
                }

                //find author
                tmp = source.indexOf("<span class=\\\"UserName lia-user-name",tmp);
                if(tmp==-1)
                    break;
                tmp = source.indexOf("<span class",tmp+22);
                String author = source.substring(source.indexOf(">",tmp)+1, source.indexOf("</span",tmp));
                System.out.println("author: "+author);

                //find time
                tmp = source.indexOf("<div class=\\\"autodesk-reply-time",tmp);
                String date = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</div>", tmp)+1);
                date = date.substring(0, date.indexOf("<"));
                System.out.println("date: " + date);

                //find content
                tmp = source.indexOf("<div class=\\\"lia-message-body-content",tmp);
                String content = source.substring(tmp,source.indexOf("</div>",tmp));
                content=content.replaceAll("<.*?>", "");
                content=content.replaceAll("&.*?;", "");
                System.out.println(content);

                System.out.println();
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		String forum = "Adobe";
		String url = "http://forums.autodesk.com/t5/Maya-General/Edge-visuals/td-p/5074840";
        //String content=gettingHTMLSOURCE(url);

        parseAutoDesk(url);

        //parseGoogleLinks(url);
        //parseAdobe("https://forums.adobe.com/message/5541525#5541525");

        //System.out.println(content);

        //parseGoogleLinks("https://productforums.google.com/forum/#!categories/docs");
        //parseAdobe("https://forums.adobe.com/message/4721586#4721586");
        //parseGoogle("https://productforums.google.com/forum/#!category-topic/docs/NtbYBtBYvX4%5B1-3-false%5D");

		//System.out.println(content);

        //parseAutoDeskLinks(content);
		//if(forum.compareTo("Adobe")==0)
		//{
		//	parseAdobeLinks(content);
		//}
        /*parseAdobe("https://forums.adobe.com/thread/1491903");
        System.out.println("Done");*/
	}
}
