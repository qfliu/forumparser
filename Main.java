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
                //Thread.sleep(20000);
                //p.waitFor();
                BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String s;
                while ((s = stdOut.readLine()) != null) {
                    if(s.contains("\"actual_url\""))
                    {
                        //System.out.println(s);
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
                       // System.out.println("lala");
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
	
	public static void parseAdobeLinks(String link)
	{
        String info = gettingHTMLSOURCE(link);

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
            System.out.println(i+" "+postsURL.get(i));
			parseAdobe(postsURL.get(i));
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
	        
	        File output = new File("output-adobe.csv");
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

    public static void parseGoogleLinks(String link) throws InterruptedException
    {
        String source = gettingHTMLSOURCE(link);
        System.out.print("");

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

            counter++;
        }

        for(int i=0;i<postsURL.size();i++)
        {
            //System.out.println("https://productforums.google.com/forum/"+postsURL.get(i)+"%5B1-"+numPosts.get(i)+"-false%5D");
            parseGoogle("https://productforums.google.com/forum/"+postsURL.get(i));
        }

        System.out.println(counter);
    }

    public static void parseGoogle(String link) throws InterruptedException
    {
        String source = gettingHTMLSOURCE(link);
        //System.out.println(source);
        boolean question = true;

        System.out.print("");
        System.out.print("");

        try{
            File output = new File("output-google.csv");
            boolean existBefore = true;
            if (!output.exists()) {
                existBefore = false;
            }

            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            if(!existBefore)
            {
                bw.append("URL, Question Title, Time Stamp, Initial Question, Responses");
                bw.append("\n");
            }

            bw.append(link+",");

            int tmp=0;
            String allLinks="";

            while(true) {
                String curReplyLinks ="";
                //find topic
                if(question) {
                    tmp = source.indexOf("<span class=\\\"GFO--0QHCC\\\"", tmp);
                    String topic = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</span>", tmp));
                    //System.out.println("topic: " + topic);
                    topic=topic.replaceAll("\"","");
                    bw.append("\""+topic+"\",");
                }

                //find author
                tmp = source.indexOf("class=\\\"GFO--0QCVB", tmp);
                if(tmp==-1)
                    break;
                String author = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</span>", tmp));
                //System.out.println("author: " + author);

                //find date
                tmp = source.indexOf("class=\\\"GFO--0QJFB", tmp);
                tmp = source.indexOf("title=\\\"", tmp);
                String date = source.substring(tmp + 8, source.indexOf("\\\"", tmp+8));
                //System.out.println("date: " + date);

                if(question)
                {
                    bw.append("\""+author+"\n"+date+"\",");
                    question = false;
                }

                //find question
                tmp = source.indexOf("class=\\\"GFO--0QIFB", tmp);
                //tmp = source.indexOf("<div dir=\\\"ltr\\\"", tmp);
                String content = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("<a class=\\\"gwt-Anchor\\\"", tmp));
                tmp=source.indexOf("<a class=\\\"gwt-Anchor\\\"", tmp);

                int tmpHref = 0;
                while( true )
                {
                    tmpHref = content.indexOf("href=\\\"",tmpHref)+7;
                    if(tmpHref < 7)
                        break;

                    int endOfLink = content.indexOf("\\\"",tmpHref);
                    String curlink = content.substring(tmpHref,endOfLink);
                    curReplyLinks = curReplyLinks+" "+curlink;
                    tmpHref = endOfLink;
                }

                int tmpVideo = 0;
                while( true )
                {
                    tmpVideo = content.indexOf("<iframe",tmpVideo);
                    if(tmpVideo < 0)
                        break;

                    throw new IOException("Video Found but does not handle yet");
                }

                //find attachments
                String attachments = "";
                int tmpAttach = tmp;
                tmpAttach = source.indexOf("<div class=\\\"GFO--0QNDB",tmpAttach)+4;
                //System.out.println(tmpAttach);
                int nextReplyIndex = source.indexOf("class=\\\"GFO--0QCVB", tmp);
                if(nextReplyIndex==-1)
                    nextReplyIndex = source.indexOf("<div class=\\\"GFO--0QHAC",tmp);
                if(tmpAttach>=4 && tmpAttach<nextReplyIndex)
                {
                    String prev = "";
                    while(source.indexOf("href=\\\"",tmpAttach)<nextReplyIndex)
                    {
                        tmpAttach = source.indexOf("href=\\\"",tmpAttach)+7;
                        if(tmpAttach<7)
                            break;

                        String curAttach = source.substring(tmpAttach,source.indexOf("\\\"",tmpAttach));

                        if(prev.compareTo(curAttach)!=0)
                            attachments = attachments+" "+source.substring(tmpAttach,source.indexOf("\\\"",tmpAttach));

                        prev = curAttach;
                    }
                }

                content=content.replaceAll("\"","");
                content = content.replaceAll("<.*?>", "");
                content=content.replaceAll("&.*?;","");
                content=content+curReplyLinks+" ["+attachments+"]";
                //System.out.println("content: " + content);
                bw.write("\"[" + author + ", " + date + "]\n" + content + "\",");

                allLinks = allLinks+curReplyLinks+attachments;

                //System.out.println();
            }
            bw.write("\""+allLinks+"\",");
            bw.append("\n");
            bw.flush();
            bw.close();
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void parseAutoDeskLinks()
    {
        /*String info = gettingHTMLSOURCE(link);
        System.out.println(info);

        int refstart = 0;
        int indicator = 0;
        ArrayList<String> postsURL=new ArrayList<String>();
        while (info.indexOf("id=\\\"topicMessageLink", indicator)!=-1)
        {
            indicator = info.indexOf("id=\\\"topicMessageLink", indicator);
            refstart=info.indexOf("href=\\\"", indicator)+7;
            String ref = info.substring(refstart, info.indexOf("\\\"", refstart));
            if(ref.contains("/t5/"))
                postsURL.add(ref);
            refstart=info.indexOf("\\\"", refstart);
            indicator = refstart;
        }*/

        ArrayList<String> postsURL=new ArrayList<String>();

        try {
            File file = new File("input_autodesk.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                postsURL.add(line);
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        for(int i=0;i<postsURL.size();i++)
        {
            System.out.println(i+" "+postsURL.get(i));
            parseAutoDesk(postsURL.get(i));
        }
    }

    public static void parseAutoDesk(String link)
    {
        String source = gettingHTMLSOURCE(link);
        boolean question = true;

        try{
            File output = new File("output-autoDesk.csv");
            boolean existBefore = true;
            if (!output.exists()) {
                existBefore = false;
            }

            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            if(!existBefore)
            {
                bw.append("URL, Question Title, Time Stamp, Initial Question, Responses");
                bw.append("\n");
            }

            bw.append(link+",");

            String allLinks="";
            int tmp=0;

            while(true) {
                //find author
                tmp = source.indexOf("<span class=\\\"UserName lia-user-name",tmp);
                if(tmp==-1)
                    break;
                tmp = source.indexOf("<span class",tmp+4);
                String author = source.substring(source.indexOf(">",tmp)+1, source.indexOf("</span",tmp));
                author=author.replaceAll("<.*?>", "");

                //find topic
                if(question) {
                    tmp = source.indexOf("<div class=\\\"first-message", tmp);
                    tmp = source.indexOf("<div class=\\\"lia-message-subject",tmp);
                    String topic = source.substring(source.indexOf("<h1", tmp), source.indexOf("</h1>", tmp));
                    topic=topic.replaceAll("<.*?>", "");
                    topic=topic.replaceAll("\"","");
                    bw.append("\""+topic+"\",");
                }

                //find time
                tmp = source.indexOf("<div class=\\\"autodesk-reply-time",tmp);
                String date = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</div>", tmp)+1);
                date = date.substring(0, date.indexOf("<"));
                date=date.replaceAll("\\\\.","");

                if(question)
                {
                    bw.append("\""+author+"\n"+date+"\",");
                    question = false;
                }

                //find content
                tmp = source.indexOf("<div class=\\\"lia-message-body-content",tmp)+10;
                int contentEnd=source.indexOf("</div>",tmp);
                String content = source.substring(source.indexOf(">",tmp)+1,contentEnd);
                while(content.indexOf("<div class=\\\"username_area")>0)
                {
                    content=content.replace("class=\\\"username_area","");

                    content=content+source.substring(contentEnd,source.indexOf("</div>",contentEnd+3));
                    contentEnd=source.indexOf("</div>",contentEnd+3);
                }
                tmp=contentEnd;

                String curReplyLinks = "";
                int tmpImg = 0;
                while( true )
                {
                    tmpImg = content.indexOf("<img", tmpImg)+3;
                    if(tmpImg < 3)
                        break;

                    if( content.indexOf("class=\\\"", tmpImg)>-1 && content.indexOf("class=\\\"", tmpImg)<content.indexOf(">",tmpImg))
                        continue;

                    tmpImg = content.indexOf("src=\\\"", tmpImg)+6;

                    int endOfLink = content.indexOf("\\\"",tmpImg);
                    String curlink = content.substring(tmpImg,endOfLink);
                    if(curlink.contains("ADSK_Expert_Elite_Icon_S_Color_Blk.png") ||
                       curlink.contains("autodesk_logo_signature.png"))
                        continue;
                    curReplyLinks = curReplyLinks+" http://forums.autodesk.com"+curlink;
                    tmpImg = endOfLink;
                }

                int tmpHref = 0;
                while( true )
                {
                    tmpHref = content.indexOf("href=\\\"",tmpHref)+7;
                    if(tmpHref < 7)
                        break;

                    int endOfLink = content.indexOf("\\\"",tmpHref);
                    String curlink = content.substring(tmpHref,endOfLink);
                    curReplyLinks = curReplyLinks+" "+curlink;
                    tmpHref = endOfLink;
                }

                int tmpVideo = 0;
                while( true )
                {
                    tmpVideo = content.indexOf("<iframe",tmpVideo)+3;
                    if(tmpVideo < 3)
                        break;

                    tmpVideo = content.indexOf("src=\\\"", tmpVideo)+6;

                    int endOfLink = content.indexOf("\\\"",tmpVideo);
                    String curlink = content.substring(tmpVideo,endOfLink);

                    curReplyLinks = curReplyLinks+" "+curlink;
                    tmpImg = endOfLink;
                }

                //find attachments
                String attachments = "";
                int tmpAttach = tmp;
                tmpAttach = source.indexOf("<div class=\\\"Attachments",tmpAttach)+5;
                //System.out.println(tmpAttach);
                int nextReplyIndex = source.indexOf("<div class=\\\"lia-message-body-content", tmp);

                //TODO: later on check if this works
                if(nextReplyIndex==-1)
                    nextReplyIndex = source.indexOf("<div class=\\\"lia-quilt-row lia-quilt-row-forum-message-footer", tmp);
                if(tmpAttach>4 && tmpAttach<nextReplyIndex)
                {
                    while(source.indexOf("<div class=\\\"attachment-",tmpAttach)<nextReplyIndex)
                    {
                        tmpAttach = source.indexOf("<div class=\\\"attachment-",tmpAttach);
                        if(tmpAttach<0)
                            break;

                        tmpAttach = source.indexOf("href=\\\"",tmpAttach)+7;
                        attachments = attachments+" http://forums.autodesk.com"+source.substring(tmpAttach,source.indexOf("\"",tmpAttach));
                  }
                }

                content=content.replaceAll("\"","");
                content=content.replaceAll("<.*?>", "");
                content=content.replaceAll("&.*?;","");
                content=content.replaceAll("\\\\.", "");
                content=content.trim().replaceAll(" +", " ");
                content=content+curReplyLinks+" ["+attachments+"]";
                bw.write("\"[" + author + ", " + date + "]\n" + content + "\",");

                allLinks = allLinks+curReplyLinks+attachments;
            }
            bw.write("\""+allLinks+"\",");
            bw.append("\n");
            bw.flush();
            bw.close();
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void parseMicrosoftLinks(String link)
    {
        String content=gettingHTMLSOURCE(link);
        int refstart = 0;
        ArrayList<String> postsURL=new ArrayList<String>();
        while (content.indexOf("class=\\\"threadlistTitle", refstart)!=-1)
        {
            refstart=content.indexOf("class=\\\"threadlistTitle", refstart)+5;
            refstart=content.indexOf("href=\\\"", refstart)+7;
            String ref = content.substring(refstart, content.indexOf("\\\"", refstart));
            postsURL.add(ref);
            refstart=content.indexOf("\\\"", refstart);
        }
        for(int i=0;i<postsURL.size();i++)
        {
            System.out.println(i+" "+postsURL.get(i));
            parseMicrosoft(postsURL.get(i));
        }
    }

    public static void parseMicrosoft(String link)
    {
        String source = gettingHTMLSOURCE(link);
        boolean question = true;
        boolean skip = true;
        String allLinks = "";

        try{
            File output = new File("output-microsoft.csv");
            boolean existBefore = true;
            if (!output.exists()) {
                existBefore = false;
            }

            FileWriter fw = new FileWriter(output.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            if(!existBefore)
            {
                bw.append("URL, Question Title, Time Stamp, Initial Question, Responses");
                bw.append("\n");
            }

            bw.append(link+",");

            int tmp=0;
            while(true) {
                tmp=source.indexOf("<div class=\\\"message\\\"",tmp)+1;
                if(tmp==0)
                    break;

                //find user
                tmp = source.indexOf("<div class=\\\"userInfo",tmp);
                String username = source.substring(tmp,source.indexOf("</a>",tmp));
                username=username.substring(username.lastIndexOf(">")+1);

                //find date
                tmp = source.indexOf("aria-label=\\\"Date\\\"",tmp);
                String date = source.substring(source.indexOf(">",tmp)+1, source.indexOf("<img",tmp));

                //find topic
                if(question) {
                    tmp = source.indexOf("id=\\\"threadTitle", tmp);
                    String topic = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("</h1>", tmp));
                    topic=topic.replaceAll("\"","");
                    topic=topic.replaceAll("&.*?;","");
                    bw.append("\""+topic+"\",\""+username+"\n"+date+"\",");
                    question = false;
                }

                //find content
                if(skip && source.indexOf("class=\\\"messageAnswer",tmp)!=-1 &&source.indexOf("class=\\\"messageAnswer",tmp)<source.indexOf("<div class=\\\"message\\\"",tmp))
                {
                    skip = false;
                    continue;
                }

                tmp=source.indexOf("<div class=\\\"msgBody wrapWord fullMessage",tmp)+3;
                String reply = source.substring(source.indexOf(">", tmp) + 1, source.indexOf("<ul class=", tmp));

                String curReplyLinks = "";

                int tmpImg = 0;
                while( true )
                {
                    tmpImg = reply.indexOf("<img src=\\\"",tmpImg)+11;
                    if(tmpImg < 11)
                        break;

                    int endOfLink = reply.indexOf("\\\"",tmpImg);
                    String curlink = reply.substring(tmpImg,endOfLink);
                    curReplyLinks = curReplyLinks+" "+curlink;
                    tmpImg = endOfLink;
                }

                int tmpHref = 0;
                while( true )
                {
                    tmpHref = reply.indexOf("href=\\\"",tmpHref)+7;
                    if(tmpHref < 7)
                        break;

                    int endOfLink = reply.indexOf("\\\"",tmpHref);
                    String curlink = reply.substring(tmpHref,endOfLink);
                    curReplyLinks = curReplyLinks+" "+curlink;
                    tmpHref = endOfLink;
                }

                int tmpVideo = 0;
                while( true )
                {
                    tmpVideo = reply.indexOf("<iframe",tmpVideo);
                    if(tmpVideo < 0)
                        break;

                    throw new IOException("Video Found but does not handle yet");
                }

                reply=reply.replaceAll("\"","");
                reply=reply.replaceAll("<.*?>", "");
                reply=reply.replaceAll("&.*?;","");
                reply=reply.replaceAll("\\\\.","");
                reply=reply.trim().replaceAll(" +"," ");
                reply = reply+ ""+curReplyLinks;

                bw.write("\"["+username+", "+date+"]\n"+reply+"\",");

                allLinks = allLinks+curReplyLinks;
            }
            bw.write("\""+allLinks+"\",");
            bw.append("\n");
            bw.flush();
            bw.close();
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
		/*String autoDeskUrl[] = new String[]{
                "http://forums.autodesk.com/t5/Inventor-General/How-do-I-twist-an-object/m-p/4294589/highlight/true#M473325",
                "http://forums.autodesk.com/t5/AutoCAD-2010-2011-2012/How-do-I-draw-this-cloud-picture-to-demonstrate/m-p/3039678/highlight/true#M52169",
                "http://forums.autodesk.com/t5/Inventor-General/HOw-do-you-make-a-gear-in-Autodesk/m-p/3285539/highlight/true#M422581",
                "http://forums.autodesk.com/t5/AutoCAD-Civil-3D-General/Pressure-Pipe-How-do-I-create-new-fittings/m-p/3417429/highlight/true#M175067",
                "http://forums.autodesk.com/t5/AutoCAD-Plant-3D-General/How-do-I-open-the-catalog/m-p/4875522/highlight/true#M13551",
                "http://forums.autodesk.com/t5/Inventor-Customization/How-do-i-get-the-bom-referenced-view-vb-net/m-p/4897616/highlight/true#M49059",
                "http://forums.autodesk.com/t5/AutoCAD-for-Mac-General/How-do-I-copy-objects-to-a-new-layer-or-duplicate-a-layer/m-p/5019030/highlight/true#M11702",
                "http://forums.autodesk.com/t5/3ds-Max-3ds-Max-Design-General/How-do-I-use-UVW-mapping-channels-to-control-different-maps/m-p/5054808/highlight/true#M94299",
                "http://forums.autodesk.com/t5/Inventor-General/How-do-I-reverse-the-scroll-wheel-intellizoom/m-p/3504942/highlight/true#M438980",
                "http://forums.autodesk.com/t5/Revit-MEP/How-do-you-select-equipment-for-parallel-systems/m-p/4786017/highlight/true#M22001",
                "http://forums.autodesk.com/t5/Installation-Licensing/Failed-Installation-Error-1603-How-do-I-fix-it/m-p/5066122/highlight/true#M87838",
                "http://forums.autodesk.com/t5/AutoCAD-2010-2011-2012/How-do-you/m-p/3234696/highlight/true#M8257",
                "http://forums.autodesk.com/t5/AutoCAD-Civil-3D-General/How-do-I-remove-curves-from-my-alignment/m-p/3417907/highlight/true#M175118",
                "http://forums.autodesk.com/t5/AutoCAD-2010-2011-2012/How-do-I-rotate-a-3D-model-in-model-space/m-p/3413203/highlight/true#M15063",
                "http://forums.autodesk.com/t5/AutoCAD-2013-2014-2015/How-to-open-dxb/m-p/5049988/highlight/true#M52147",
                "http://forums.autodesk.com/t5/NET/How-To-Resize-Palettes/m-p/5000116/highlight/true#M40368",
                "http://forums.autodesk.com/t5/Inventor-General/How-to-add-layers-for-flat-pattern/m-p/5046294/highlight/true#M509305",
                "http://forums.autodesk.com/t5/AutoCAD-Civil-3D-General/how-to-grade-intersecting-swales/m-p/4881500/highlight/true#M243687",
                "http://forums.autodesk.com/t5/Modeling/Sweep-how-to-acoid-overlap/m-p/4994012/highlight/true#M16370"

        };*/

        String url = "https://productforums.google.com/forum/#!category-topic/chromebook-central/uP85PbK9Rt4";
        url = "http://forums.autodesk.com/t5/forums/searchpage/tab/message?filter=labels&q=how+to";
        //parseAutoDesk("http://forums.autodesk.com/t5/Installation-Licensing/Failed-Installation-Error-1603-How-do-I-fix-it/m-p/5066122/highlight/true#M87838");
        //String url = "http://answers.microsoft.com/en-us/bing/forum/bing_gettingstarted-bing_social/i-dont-want-bing-connected-to-facebook-want-to-use/a5ac348b-9480-4b4d-855a-85d32d96919d";
        //String content=gettingHTMLSOURCE(url);
        //System.out.println(content);
        parseAutoDeskLinks();
        //parseMicrosoftLinks(url);
        /*for (int i=0;i<autoDeskUrl.length;i++)
        {
            System.out.println(autoDeskUrl[i]);
            parseAutoDesk(autoDeskUrl[i]);
        }*/
        //parseAutoDesk(url);

        //parseGoogleLinks(url);

        /*parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How+to%22+%7C%7C+%22How+do%22&x=0&y=0&CurrentScope.ForumName=&CurrentScope.Filter=&ContentTypeScope=");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=2");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=3");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=4");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=5");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=6");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=7");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=8");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=9");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22%20%7C%7C%20%22How%20do%22&page=10");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How+to%22&x=0&y=0&CurrentScope.ForumName=&CurrentScope.Filter=&ContentTypeScope=");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22&page=2");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20to%22&page=3");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How+do%22&x=0&y=0&CurrentScope.ForumName=&CurrentScope.Filter=&ContentTypeScope=");
        parseMicrosoftLinks("http://answers.microsoft.com/en-us/search/search?SearchTerm=%22How%20do%22&page=2");*/
        //parseGoogle("https://productforums.google.com/forum/#!category-topic/chromebook-central/kBbnNk138P8");

		//if(forum.compareTo("Adobe")==0)
		//{
		//	parseAdobeLinks(url);
		//}
        /*parseAdobe("https://forums.adobe.com/thread/1491903");
        System.out.println("Done");*/
	}
}
