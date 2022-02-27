package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		
		File Fh = new File(docFile);
		if(!Fh.exists())
		{
			throw new FileNotFoundException();
		}
		Scanner sc = new Scanner(new File(docFile));
		HashMap<String,Occurrence> a = new HashMap<String,Occurrence>(1000,2.0f);
		
		while(sc.hasNext())
		{
			String word = sc.next();
			if(getKeyWord(word) == null)
			{
				continue;
			}
			String word2 = getKeyWord(word);
			if(a.containsKey(word2))
			{
				a.get(word2).frequency++;
			}
			else
			{
				Occurrence b = new Occurrence(docFile,1);
				a.put(word2,b );
			}
		}
		return a;	
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		Iterator<String> b = kws.keySet().iterator();
		while(b.hasNext())
		{
			String d = b.next();
			Occurrence occur = kws.get(d);
			if(keywordsIndex.containsKey(d))
			{
				ArrayList<Occurrence> e = keywordsIndex.get(d);
				e.add(occur);
				insertLastOccurrence(e);
			}
			else
			{
				ArrayList<Occurrence> j = new ArrayList<Occurrence>(10);
				j.add(occur);
				keywordsIndex.put(d, j);	
			}	
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		
		int z = word.length()- 1;
		if(word.length() == 1)
		{
			if(noiseWords.containsKey(word))
			{
				return null;
			}
			else
				return word;
		}
		while(!Character.isLetter(word.charAt(z)))
		{
			z--;
			if(z < 0)
			{
				break;
			}
		}
		String xy = word.substring(0,z+1);
		xy = xy.toLowerCase();
		for(int i = 0; i < xy.length(); i++)
		{
			if(Character.isLetter(word.charAt(i)))
			{
				
				continue;
			}
			return null;
		}
		if(noiseWords.containsKey(xy) == false)
		{
			return xy;
		}
		else
			return null;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) 
	{
		if(occs.size() <= 1)
		{
			return null;
		}
		
		ArrayList<Integer> x = new ArrayList<Integer>(1);
		Occurrence b = occs.get(occs.size()-1);
		
		int max = occs.size()-2;
		int min = 0;
		int mid = (max + min)/2;
		
		while(max >= min)
		{
			mid = (max+min)/2;
			if(b.frequency < occs.get(mid).frequency)
			{
				min = mid + 1;
				x.add(mid);
			}
			
			else if(b.frequency > occs.get(mid).frequency)
			{
				max = mid - 1;
				x.add(mid);
			}
			
			else 
			{
				x.add(mid);
				break;
			}
		}
		
		
		if(b.frequency < occs.get(mid).frequency)
		{
			occs.remove(occs.size()-1);
			occs.add(mid+1, b);
		}
		
		else
		{
			occs.remove(occs.size()-1);
			occs.add(mid, b);
		}
		
		if(x.size()==1)
		{
			return null;
		}
		return x;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
			ArrayList<Occurrence> a = keywordsIndex.get(kw1);
			ArrayList<Occurrence> b = keywordsIndex.get(kw2);
			if(a == null &&  b == null)
			{
				return null;
			}
			
			if(a == null)
			{
				a = new ArrayList<Occurrence>(10);
			}
			
			if(b == null)
			{
				b = new ArrayList<Occurrence>(10);
			}

			
			int p1 = 0;
			int p2 = 0;
			
			ArrayList<String> result = new ArrayList<String>();
			while(result.size() < 5)
			{
				if(p1 >= a.size() && p2 >= b.size())
				{
					break;
				}
				else if(p1 >= a.size())
				{
					if(!result.contains(b.get(p2).document))
					{
						result.add(b.get(p2).document);
					}
					p2++;
				}
				else if(p2 >= b.size())
				{
					if(!result.contains(a.get(p1).document))
					{
						result.add(a.get(p1).document);
						
					}
					p1++;
				}
				
				else if(a.get(p1).frequency > b.get(p2).frequency)
				{
					if(!result.contains(a.get(p1).document))
					{
						result.add(a.get(p1).document);
					
					}
					
					p1++;
				}
				
				else if(b.get(p2).frequency > a.get(p1).frequency)
				{
					if(!result.contains(b.get(p2).document))
					{
						result.add(b.get(p2).document);
						
					}
					p2++;
				}
				else
				{
					if(!result.contains(a.get(p1).document))
					{
						result.add(a.get(p1).document);
					}
					p1++;
					if(result.size() < 5)
					{
						if(!result.contains(b.get(p2).document))
						{
							result.add(b.get(p2).document);
							
						}
					}
					p2++;
			    }	
			}	
			if(result.size() == 0)
			{
				return null;
			}
			return result;
	}
}
