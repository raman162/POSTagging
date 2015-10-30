Raman Walwyn-Venugopal - A20296946
CS 481
POS Tagging - HW 2



The code provided in this zip is a unigram tagger modified to a bigram tagger with the addition of +n smoothing. This modification was done initially by generating a Tag->Tag HashMap during the training session. During the tagging session, the tag->tag probabilities are used to help determine which is the best POS tag for each of the words. Based on the log-probability of the previous tag, multiple paths are generated based on the number of tags there are. The last word determines which is the best path, whichever tag has the maximum log-probability, chooses the path which will assign the respective tags.


1. The acccuracy of small train on :
   test_1.xml is 93%
   test_2.xml is 5%

The reason for the low acccuracy between them is probably due to the fact that the unigram tagger did not have any limited horizon and just tagged words given the probability of the tag given that word along with the probability of the given tag. 
After observing the output of the tagger, the reasons stated above seem to be right, words with different meanings were now used in different contexts, and a unigram tagger is not capable to account for such encounters.

As instructed to do, improving the tagger's performance would be to make it consider previous tags, as well as tags that occur afterwards. Smoothing will also assist with data sparseness. You can make the tagger smarter by implementing enhancements such as having words ending with ing being verbs or having only nouns and adjectives occur after a determiner. 

2. Modified POSTag.java - > addded the tag->tag hashmap along with plus-k smoothing.

3. Since it now considers the tag->tag probabilities the run time of the algorithm has increased along with the accuracy, the accuracy boosted from 5% -> 67%. I suspect that the accuracy can be improved even greater once the enhancements have been added.  
