package collection01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Collection {

	public static void main(String[] args) throws IOException, TransformerException {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document document = (Document) docBuilder.newDocument();
			
			Element docs = document.createElement("docs");
			document.appendChild(docs);                //docs element가 document에 있다.
			
			String path="C:\\Users\\dydrmsl\\Desktop\\opensw";
			File[] file=makeFileList(path);
			
			for(int i=0; i<5; i++) {
				org.jsoup.nodes.Document html = Jsoup.parse(file[i], "UTF-8");
				String titleData = html.title();       //각 파일의 <title> data 추출
				String bodyData = html.body().text();  //각 파일의 <body> data 추출
				
				Element doc = document.createElement("doc");  
				docs.appendChild(doc);                 //doc element가 docs에 있다.
				String id = Integer.toString(i);
				doc.setAttribute("id", id);            //doc element에 id 속성을 파일 순서별로 0부터 부여
				
				Element title = document.createElement("title");
				title.appendChild(document.createTextNode(titleData));
				doc.appendChild(title);                //title element가 doc에 있다.
				
				Element body = document.createElement("body");			
				KeywordExtractor ke = new KeywordExtractor();      //kkma 형태소 분석기 사용
				KeywordList kl = ke.extractKeyword(bodyData, true);
				
				for(int j=0; j<kl.size(); j++) {
					Keyword kwrd = kl.get(j);
					body.appendChild(document.createTextNode(kwrd.getString()+":"+kwrd.getCnt()+"#"));
				}
				
				doc.appendChild(body);                 //body element가 doc에 있다.
				
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  //인코딩 타입은 UTF-8
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(new File("index.xml")));
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");   //들여쓰기
			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
	}
	
	public static File[] makeFileList(String path) {    //5개의 파일을 읽어들인다.
		File dir = new File(path);
		return dir.listFiles();
	}

}
