package scripts;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;

public class MidTerm {
private String input_file;
	
	public MidTerm(String input_file, String q, String input_word) throws IOException {
		this.input_file = input_file;
		File f = new File(input_file);
		KeywordExtractor ke = new KeywordExtractor();
		KeywordList kl = ke.extractKeyword(input_word, true);
		
		// 입력 받은 문자열에서 중복되는 keyword는 제거해서 key배열에 담는다.
		String[] query = new String[kl.size()];
		for (int i = 0; i < kl.size(); i++) {
			Keyword kwrd = kl.get(i);
			query[i] = kwrd.getString();
		}
		
		for(int i=0; i<query.length-1; i++) {
			for(int j=i+1; j<query.length; j++) {
				if(query[i].equals(query[j])) {
					query[j]="";
				}
			}
		}
		int cnt=0;
		for(int i=0; i<query.length; i++) {
			if(query[i]!="")
				cnt++;
		}
		int a=0;
		
		String[] key = new String[cnt];
		for(int i=0; i<query.length; i++) {
			if(query[i]!="") {
				key[a]=query[i];
				a++;
			}
		}
		
		// collection.xml을 받고 title과 body 데이터를 추출
		org.jsoup.nodes.Document html = Jsoup.parse(f, "UTF-8", "", Parser.xmlParser());
		for(int i=0; i<5; i++) {
			Integer id=i;
			String titleData = html.getElementById(id.toString()).getElementsByTag("title").text();
			String bodyData = html.getElementById(id.toString()).getElementsByTag("body").text();
			
		}
	}
	
	// showSnippet 함수
	public void showSnippet(String[] key, int id, String bodyData) {
		
	}
}
