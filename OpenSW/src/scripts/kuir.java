package scripts;

import java.io.IOException;

import javax.xml.transform.TransformerException;

public class kuir {

	public static void main(String[] args) throws TransformerException, IOException, ClassNotFoundException {
		String command = args[0];   
		String path = args[1];

		if(command.equals("-c")) {
			makeCollection collection = new makeCollection(path);
		}
		else if(command.equals("-k")) {
			makeKeyword keyword = new makeKeyword(path);
		}
		else if(command.equals("-i")) {
			indexer weight = new indexer(path);
		}

	}

}
