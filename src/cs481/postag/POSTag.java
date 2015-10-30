package cs481.postag;

import cs481.token.*;
import cs481.util.*;

import java.io.*;
import java.util.*;

/**
 * Determines the part of speech tags based on Viterbi.
 *
 * <pre>
 * Typical use:
 * POSTag pt = new POSTag();
 * pt.train(training);
 * pt.tag(testing);
 * </pre>
 *
 * Run from the commandline.
 *
 * @author Sterling Stuart Stein
 * @author Shlomo Argamon
 */
public class POSTag
{
  
  /**
   *Special start tag
   */
    public static String StartTag = "*START*";
  
    /**
     * Small probability for when not found
     */
  public static float epsilon = -10000000f; //Will not use anymore since I will be implementing plus-one smoothing
  
  
  
  /**
   * Smoothing constant to add
   */
  public static float smoothing = 1;//+ n smoothing
    /**
     * Array of all tags
     */
    protected String[] tags;
  
  /**
     * Probability of tags given specific words
     */
  protected HashMap pTagWord;
  
    /**
     * Probability of individual tags (i.e., P(tag)
     */
  protected HashMap pTag;	
  
  /**
     * Hashmap of all known words
     */
    protected HashMap allWords;;	
  
  
    /**
     *Probability of tag given previous tag
     */
    protected HashMap pPrevTag;
  
    /**
     * Make an untrained part of speech tagger.
     */
    public POSTag()
    {
	pTagWord    = new HashMap();
	pTag        = new HashMap();
	allWords    = new HashMap();
        pPrevTag    = new HashMap(); //initiating new HashMap to count tag groupings together
    }
  
    /**
     * Remove all training information.
     */
    public void clear()
    {
	pTag.clear();
	pTagWord.clear();
	allWords.clear();
        pPrevTag.clear(); //adding clearing function to PreviousTagHashmap
	tags = null;
    }
  
    /**
     * Increment the count in a HashMap for t.
     *
     * @param h1 The HashMap to be modified
     * @param t  The key of the field to increment
     */
    protected void inc1(HashMap h1, String t)
    {
	if(h1.containsKey(t))
	    {
		int[] ip = (int[])h1.get(t);  //Used as int *
		ip[0]++;
	    }
	else
	    {
		int[] ip = new int[1];
		ip[0] = 1;
		h1.put(t, ip);
	    }
    }
  
    /**
     * Increment the count in a HashMap for [t1,t2].
     *
     * @param h2 The HashMap to be modified
     * @param t1 The 1st part of the key of the field to increment
     * @param t2 The 2nd part of the key of the field to increment
     */
    protected void inc2(HashMap h2, String t1, String t2)
    {
	//Have to use Vector because arrays aren't hashable
	Vector key = new Vector(2);
	key.setSize(2);
	key.set(0, t1);
	key.set(1, t2);
        
	if(h2.containsKey(key)) {
		int[] ip = (int[])h2.get(key);  //Used as int *
		ip[0]++;
	} else {
		int[] ip = new int[1];
		ip[0] = 1;
		h2.put(key, ip);
	    }
    }
  
    /**
     * Train the part of speech tagger.
     *
     * @param training A vector of paragraphs which have tokens with the attribute &quot;pos&quot;.
     */
    public void train(Vector training)
    {
	int cTokens = 0;
	HashMap cWord    = new HashMap();  //total words
	HashMap cTag     = new HashMap();  //all tags
	HashMap cTagWord = new HashMap();
        HashMap cPrevTag = new HashMap(); //adding prevtag hasMap for training section
	boolean[] bTrue = new boolean[1];
	bTrue[0] = true;	
	clear();
        
        
        
	//Count word and tag occurrences
	for(Iterator i = training.iterator(); i.hasNext();) {
		Vector para = (Vector)i.next();
		//division into paragraphs
		for(Iterator j = para.iterator(); j.hasNext();) {
			Vector sent    = (Vector)j.next();
			String curtag  = StartTag;
			inc1(cTag, curtag);
			//division into sentences
			for(Iterator k = sent.iterator(); k.hasNext(); ) {
				Token tok = (Token)k.next();
				String prevtag = curtag;                       //setting the previous tag to the start tag
				curtag = (String)tok.getAttrib("pos");        //sets the current tag
				inc1(cTag, curtag);
                                inc2(cPrevTag, prevtag, curtag);             //adds the group of the previous tag and current tag to the hashmap
				String name = tok.getName().toLowerCase();
				inc1(cWord, name);                             
				allWords.put(name, bTrue);
				inc2(cTagWord, curtag, name);
				cTokens++;
			    }
		    }
	    }
        
	//Find probabilities from counts
	for(Iterator i = cTag.keySet().iterator(); i.hasNext();) {
	    String key   = (String)i.next();
	    int[]  count = (int[])cTag.get(key);
	    pTag.put(key, new Float(Math.log(((float)(count[0])+smoothing) / (float)cTokens)));//added one to count for plus-one smoothing
	}
        
	for(Iterator i = cTagWord.keySet().iterator(); i.hasNext();) {
	    Vector key   = (Vector)i.next();
	    int[]  count = (int[])cTagWord.get(key);
	    int[]  total = (int[])cWord.get(key.get(1));
            
	    pTagWord.put(key, new Float(Math.log(((float)(count[0]+smoothing)) / ((float)total[0]))));//added one to count for plus-one smoothing
	}
        
        //MAKING PROBABILTIES OF TAG TO TAG HASHMAPS       
        for(Iterator i = cPrevTag.keySet().iterator(); i.hasNext();){
            Vector key  = (Vector)i.next();
            int[] count = (int[])cPrevTag.get(key);
            int[] total = (int[])cTag.get(key.get(0));
            
            pPrevTag.put(key, new Float(Math.log(((float)(count[0]+smoothing)) / ((float)total[0]))));//added one to count for plus-one smoothing
        }
	//Make list of all possible tags
	tags = (String[])cTag.keySet().toArray(new String[0]);
    }
  
    /**
     * Print out a HashMap<Vector,int[1]>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashInt(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    Vector key = (Vector)i.next();
	    int[]  ip  = (int[])h.get(key);
            
	    for(int j = 0; j < key.size(); j++) {
		System.out.print(", " + key.get(j));
	    }
            
	    System.out.println(": " + ip[0]);
	}
    }
  
    /**
     * Print out a HashMap<Vector,Float>.
     *
     * @param h The HashMap to be printed.
     */
    protected void debugPrintHashFloat(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
		Vector key = (Vector)i.next();
		float  f   = ((Float)h.get(key)).floatValue();
                
		for(int j = 0; j < key.size(); j++) {
			System.out.print(", " + key.get(j));
		    }
                
		System.out.println(": " + f);
	    }
    }
  
    protected void debugPrintHashKeys(HashMap h) {
	for(Iterator i = h.keySet().iterator(); i.hasNext();) {
	    String key = ((String)i.next());
	    System.out.println(": " + key);
	}
    }
  
  
    /**
     * Tags a sentence by setting the &quot;pos&quot; attribute in the Tokens.
     *
     * @param sent The sentence to be tagged.
     */
    public void tagSentence(Vector sent) {
	int len     = sent.size();
	if (len == 0) {
	    return;
	}
        
	int numtags = tags.length;//the number of tags is based on our string size generated from learning
        
	Vector twkey = new Vector(2);//tag-word key to get probabilities
	twkey.setSize(2);//setting the size of twkey to 2
        
        Vector ttkey = new Vector(2);//Here is the tag-tag key that we will be using to get probabilities
        ttkey.setSize(2);//setting size of tag-tag key to 2
        
	//Probability of best path to word with tag
	float[][] pathprob = new float[len + 1][numtags]; 
        
	//  Edge to best path to word with tag
	int[][]   backedge = new int[len + 1][numtags];
        
        // 2d array which has the connection at that link to the previous tag that was used to make the respective probability
        int[][] connection = new int[len +1][numtags];
        
	//For words in sentence
	for(int i = 0; i < pathprob.length - 1; i++) {
	    String word = ((Token)sent.get(i)).getName().toLowerCase();//sets the word we will be working with out of the sentence
	    twkey.set(1, word);//sets part of the vector to the word that we will be working with
            
	    //Loop over tags for this word
	    for(int j = 0; j < numtags; j++) {
		String thistag = tags[j];                     //thistag is the tag we will be focusing on out of tags list
		Float tagProb1 = (Float)pTag.get(thistag);    //tagprob1 is the probability that the tag occurs
                //initially it uses the epsilon value but we have now implemented plus-n smoothing
		float tagProb = (tagProb1 == null) ? (float)(Math.log(smoothing/(float)pTag.size())) : tagProb1.floatValue(); //if tag doesn't occur use smoothing value. 
		twkey.set(0, thistag);    //sets the first part of the vector to the focused tag
                
		boolean[] knownWord = (boolean[])allWords.get(word); //checking to see if it is a known word
		Float twp1 = (Float)pTagWord.get(twkey);       //probability of tag given words
		float twp  = (((knownWord == null)||(knownWord[0] != true)) ?
			      tagProb : 
			      ((twp1 == null) ?
                               //initially it used epsilon which gave a low proabability but now we have implemented plus-n smoothing
			       (float)(Math.log(smoothing/(float)pTagWord.size())) :
			       twp1.floatValue())); //smoothing if word-tag vector is not known
                
                
                
                
                //If i ==0 that means we are at the start of the sentence, hence we use the StartTag as the previous tag for the key for the hashmap
                if (i == 0){
                  String previoustag = StartTag;
                  ttkey.set(0,previoustag);
                  ttkey.set(1,thistag);
                  Float ttp1 = (Float)pPrevTag.get(ttkey);
                  float ttp = (ttp1 == null) ? (float)(Math.log(smoothing/(float)pPrevTag.size())) : ttp1.floatValue();
                  int startTagLoc = 0;
                  int l=0;
                  while( l < numtags){
                    if (tags[l]==previoustag){
                      startTagLoc = l;
                      l=numtags+1;
                    }
                    l++;
                  }
                  connection[i][j]=startTagLoc;
                  twp = ttp + twp;//summing the probability of the tag-tag with the tag-word
                }
                else{
                  float ttp = epsilon;
                  float temp = epsilon;
                  for(int k=0; k < numtags; k++){
                    String previoustag = tags[k];
                    ttkey.set(0, previoustag);
                    ttkey.set(1, thistag);
                    Float ttp1 = (Float)pPrevTag.get(ttkey);
                    // float temp = (ttp1 == null)? (float)(Math.log(smoothing/(float)pPrevTag.size())) : ttp1.floatValue();
                    if (ttp1 == null){
                         temp = (float)(Math.log(smoothing/(float)pPrevTag.size())) + pathprob[i-1][k];
                    }else{
                         temp = ttp1.floatValue() + pathprob[i-1][k];
                    }
                    //chooses the max probability out of all the tags-tags and stores it in the tag-tag prob
                    if (temp > ttp){
                      ttp = temp;
                      connection[i][j] = k;
                    }
                  }
                  twp = pathprob[i-1][connection[i][j]] + ttp + twp;
                  
                }
                // In a unigram model, only the current probability matters but we now have added the probability including tag - tag. 
		pathprob[i][j]    = twp;  //sets the current probability of that tag
                
		// Now create the back link to the max prob tag at the previous stage
		// If we are at the second word or further
                /*	if (i > 0) {
		    int   back = 0;
		    float max  = -100000000f;
                    
		    //Loop over previous tags
		    for(int k = 0; k < numtags; k++) {
			String prevtag = tags[k];
                        
			// Probability for path->prevtag k + thistag j->word i
			float test = pathprob[i-1][k];
                        
			String prevword = ((Token)sent.get(i-1)).getName().toLowerCase();
                        
			if (test > max) {
			    max     = test;
			    back    = k;
			}
		    }
		    backedge[i][j]    = back;
		}
*/
	    }
	}
        
        int bestpath = 0;
	//Trace back finding most probable path
	{
	    float max    = -100000000f;
	    //int   prevtag = 0;
            
	    //Find final tag
	    for(int i = 0; i < numtags; i++) {
		float test = pathprob[len-1][i];
                
		if(max < test) {
		    max       = test;
		    bestpath  = i;
		}
	    }
            
	    //Follow back edges to start tag and set tags on words
	    for(int i = len-1; i >= 0; i--) {
		Token tok = (Token)sent.get(i);
		tok.putAttrib("pos", tags[bestpath]);
		bestpath = connection[i][bestpath];
	    }
	}
    }
  
    /**
     * Tags a Vector of paragraphs by setting the &quot;pos&quot; attribute in the Tokens.
     *
     * @param testing The paragraphs to be tagged.
     */
    public void tag(Vector testing) {
	for(Iterator i = testing.iterator(); i.hasNext();) {
	    Vector para = (Vector)i.next();
            
	    for(Iterator j = para.iterator(); j.hasNext();) {
		Vector sent = (Vector)j.next();
		tagSentence(sent);
                //System.out.println("tagged a sentence");
	    }
	}
    }
  
    /**
     * Train on             the 1st XML file,
     * tag                  the 2nd XML file,
     * write the results in the 3rd XML file.
     *
     * @param argv An array of 3 XML file names.
     */
    public static void main(String[] argv) throws Exception
    {
	if(argv.length != 3) {
	    System.err.println("Wrong number of arguments.");
	    System.err.println("Format:  java cs481.postag.POSTag <train XML> <test XML> <output XML>");
	    System.err.println("Example: java cs481.postag.POSTag train.xml untagged.xml nowtagged.xml");
	    System.exit(1);
	}
        
	Vector training = Token.readXML(new BufferedInputStream(
								new FileInputStream(argv[0])));
	System.out.println("Read training file.");
        
	POSTag pt = new POSTag();
	pt.train(training);
	System.out.println("Trained.");
	training = null;  //Done with it, so let garbage collector reclaim
        
	Vector testing = Token.readXML(new BufferedInputStream(
							       new FileInputStream(argv[1])));
	System.out.println("Read testing file.");
	pt.tag(testing);
	System.out.println("Tagged.");
	Token.writeXML(testing,
		       new BufferedOutputStream(new FileOutputStream(argv[2])));
        
    }
}
