package scripts;

/**
 * 5주차 과제
 * query를 입력 받아 내적을 기반으로 5개 문서와의 유사도를 계산해 1~3위 문서의 title을 출력
 * input : index.post, query
 * 출력 시 유사도가 같은 경우 id가 크면 순위가 낮다
 * 유사도가 0이면 출력하지 않는다.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;

public class searcher {
	private String input_file;
	private String input_word;

	public searcher(String input_file, String q, String input_word) throws IOException, ClassNotFoundException {
		this.input_file = input_file;
		this.input_word = input_word;

		// 입력 받은 query를 kkma형태소 분석기로 keyword와 TF로 분류한다.
		KeywordExtractor ke = new KeywordExtractor();
		KeywordList kl = ke.extractKeyword(input_word, true);

		String[] query = new String[kl.size()];
		int[] TF = new int[kl.size()];
		for (int i = 0; i < kl.size(); i++) {
			Keyword kwrd = kl.get(i);
			query[i] = kwrd.getString();
			TF[i] = kwrd.getCnt();
		}

		// index.post를 입력 받는다.
		FileInputStream fin = new FileInputStream(input_file);
		ObjectInputStream objectInputStream = new ObjectInputStream(fin);

		Object object = objectInputStream.readObject();
		objectInputStream.close();

		HashMap hashMap = (HashMap) object;
		Iterator<String> it = hashMap.keySet().iterator();

		List<String> keyList = new ArrayList<String>();
		List<String> valueList = new ArrayList<String>();

		// query의 keyword를 index.post에서 찾아내 keyword와 value를 뽑아낸다.
		while (it.hasNext()) {
			String key = it.next();
			for (int i = 0; i < query.length; i++) {
				if (query[i].equals(key)) {
					keyList.add(query[i]);
					valueList.add((String) hashMap.get(key));
					break;
				}
			}
		}

		// CalcSim 함수로 유사도를 계산하고 순위를 매긴다. 유사도가 0인 것은 제외하고 유사도가 같은 경우 id가 클수록 순위가 낮다.
		double[] result = CalcSim(query, TF, keyList, valueList);

		int[] rank = { 1, 1, 1, 1, 1 };
		for (int i = 0; i < 5; i++) {
			if (result[i] == 0.0) {
				rank[i] = 0;
			} else {
				rank[i] = 1;
				for (int j = 0; j < 5; j++) {
					if (i == j)
						continue;
					if (result[i] < result[j])
						rank[i] += 1;
					else if (result[i] == result[j]) {
						if (i > j)
							rank[i] += 1;
					}
				}
			}
		}

		// 유사도가 높은 문서의 id를 1~3위 순서로 저장한다.
		int firstIdx = -1;
		int secondIdx = -1;
		int thirdIdx = -1;
		for (int i = 0; i < 5; i++) {
			if (rank[i] == 1)
				firstIdx = i;
			else if (rank[i] == 2)
				secondIdx = i;
			else if (rank[i] == 3)
				thirdIdx = i;
		}

		// collection.xml에서 title data를 가져온다.
		String collectionPath = "./collection.xml";
		File f = new File(collectionPath);
		org.jsoup.nodes.Document html = Jsoup.parse(f, "UTF-8", "", Parser.xmlParser());
		String[] title = new String[5];
		for (int i = 0; i < 5; i++) {
			Integer id = i;
			title[i] = html.getElementById(id.toString()).getElementsByTag("title").text();
		}

		// 유사도 1~3위를 출력한다.
		if (firstIdx == -1)
			System.out.println("유사한 문서가 없습니다.");
		else if (secondIdx == -1)
			System.out.println("1위 : " + title[firstIdx] + "(유사도 : " + result[firstIdx] + ")");
		else if (thirdIdx == -1) {
			System.out.println("1위 : " + title[firstIdx] + "(유사도 : " + result[firstIdx] + ")");
			System.out.println("2위 : " + title[secondIdx] + "(유사도 : " + result[secondIdx] + ")");
		} else {
			System.out.println("1위 : " + title[firstIdx] + "(유사도 : " + result[firstIdx] + ")");
			System.out.println("2위 : " + title[secondIdx] + "(유사도 : " + result[secondIdx] + ")");
			System.out.println("3위 : " + title[thirdIdx] + "(유사도 : " + result[thirdIdx] + ")");
		}
	}

	// 유사도 계산 함수. 5개 문서와의 유사도를 result에 담아 반환한다.
	public double[] CalcSim(String[] query, int[] TF, List<String> keyList, List<String> valueList) {
		double[] result = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		for (int i = 0; i < keyList.size(); i++) {
			for (int j = 0; j < query.length; j++) {
				if (keyList.get(i).equals(query[j])) {
					String tmp1 = query[i];
					query[i] = query[j];
					query[j] = tmp1;
					int tmp2 = TF[i];
					TF[i] = TF[j];
					TF[j] = tmp2;
					break;
				}
			}
		}

		String[] valueArr = new String[valueList.size()];
		for (int i = 0; i < valueList.size(); i++) {
			valueArr[i] = valueList.get(i);
		}

		double[][] weight = new double[query.length][5];
		for (int i = 0; i < TF.length; i++) {
			String[] tmp = valueArr[i].split(" ");
			for (int n = 0; n < 5; n++) {
				weight[i][n] = Double.parseDouble(tmp[2 * n + 1]);
			}
		}

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < TF.length; j++) {
				result[i] += weight[j][i] * (double) TF[j];
			}
		}

		return result;
	}

}
