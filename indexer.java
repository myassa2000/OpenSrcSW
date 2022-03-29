package scripts;

/**
 * 4주차 과제
 * index.xml을 불러와 keyword들의 가중치(weight=TF*IDF)를 구해 hashMap에 저장.
 * input : index.xml
 * output : index.post
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

public class indexer {
	private String input_file;
	private String output_file = "index.post";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public indexer(String file) throws IOException, ClassNotFoundException {
		this.input_file = file;
		File f = new File(input_file);

		org.jsoup.nodes.Document html = Jsoup.parse(f, "UTF-8", "", Parser.xmlParser());
		List<List<String>> wordList = new ArrayList<List<String>>();
		List<List<Integer>> TFList = new ArrayList<List<Integer>>();
		List<List<Integer>> DFList = new ArrayList<List<Integer>>();
		List<List<Double>> weightList = new ArrayList<List<Double>>();

		for (int i = 0; i < 5; i++) {
			List<String> keyWord = new ArrayList<String>(); // id=0~4의 keyword를 임시로 저장
			List<Integer> TF = new ArrayList<Integer>(); // 각 키워드들의 빈도수(TF)값을 임시로 저장
			Integer id = i;
			String bodyData = html.getElementById(id.toString()).getElementsByTag("body").text();
			String[] data = bodyData.split("#");

			for (int j = 0; j < data.length; j++) {
				String[] tmp = data[j].split(":");
				keyWord.add(tmp[0]);
				TF.add(Integer.valueOf(tmp[1]));
			}
			wordList.add(keyWord); // 임시로 저장된 keyword를 id=0부터 순서대로 List에 저장
			TFList.add(TF); // 임시로 저장된 TF를 순서대로 List에 저장
		}

		// DF를 구하는 과정
		for (int i = 0; i < 5; i++) {
			List<Integer> DF = new ArrayList<Integer>();
			List<String> keyData = wordList.get(i);
			for (int j = 0; j < keyData.size(); j++) {
				String word = keyData.get(j);
				int count = 0;
				for (List<String> list : wordList) {
					for (String check : list) {
						if (word.equals(check)) {
							count++;
							break;
						}
					}
				}
				DF.add(Integer.valueOf(count));
			}
			DFList.add(DF);
		}

		// weight를 구하는 과정. weight는 소수점 둘째 자리까지 나타낸다.
		for (int i = 0; i < 5; i++) {
			List<Integer> TFi = TFList.get(i);
			List<Integer> DFi = DFList.get(i);
			List<Double> weight = new ArrayList<Double>();
			for (int j = 0; j < TFi.size(); j++) {
				double tmp = Math.round((double) TFi.get(j) * (double) (Math.log((double) 5 / DFi.get(j))) * 100)
						/ 100.0;
				weight.add(tmp);
			}
			weightList.add(weight);
		}

		// 여러 문서에 나타난 keyword들은 처음 나타난 한 개만 남기고 지운다. 각 문서에서의 weight를 저장한다.
		List<List<Double>> keyWeightList = new ArrayList<List<Double>>();

		for (int i = 0; i < 5; i++) {
			List<String> words = wordList.get(i);
			int ws = words.size();
			for (int j = 0; j < ws; j++) {
				String word = words.get(j);
				List<Double> keyWeight = new ArrayList<Double>();
				for (int p = 0; p < 5; p++) {
					if (wordList.get(p).contains(word)) {
						if (p > i) {
							int idx = wordList.get(p).indexOf(word);
							keyWeight.add(weightList.get(p).get(idx));
							wordList.get(p).remove(idx);
							weightList.get(p).remove(idx);
						} else
							keyWeight.add(weightList.get(i).get(j));
					} else {
						keyWeight.add(0.0);
					}
				}
				keyWeightList.add(keyWeight);
			}
		}

		List<String> keyWordList = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			List<String> tmp = wordList.get(i);
			for (int j = 0; j < tmp.size(); j++)
				keyWordList.add(tmp.get(j));
		}

		// hashMap에 key, 문서의 id weight 형식으로 저장한다.
		FileOutputStream fileStream = new FileOutputStream(output_file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileStream);

		HashMap weightMap = new HashMap();

		for (int i = 0; i < keyWordList.size(); i++) {
			String value = "0 " + keyWeightList.get(i).get(0) + " 1 " + keyWeightList.get(i).get(1) + " 2 "
					+ keyWeightList.get(i).get(2) + " 3 " + keyWeightList.get(i).get(3) + " 4 "
					+ keyWeightList.get(i).get(4);
			weightMap.put(keyWordList.get(i), value);
		}

		objectOutputStream.writeObject(weightMap);
		objectOutputStream.close();

		// hashMap을 읽어온다.
		FileInputStream fin = new FileInputStream(output_file);
		ObjectInputStream objectInputStream = new ObjectInputStream(fin);

		Object object = objectInputStream.readObject();
		objectInputStream.close();

		HashMap hashMap = (HashMap) object;
		Iterator<String> it = hashMap.keySet().iterator();

		while (it.hasNext()) {
			String key = it.next();
			String value = (String) hashMap.get(key);
			System.out.println(key + " -> " + value);
		}
	}

}
