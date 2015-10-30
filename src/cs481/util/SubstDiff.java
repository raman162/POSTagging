package cs481.util;

import java.util.*;

/**
 * Find the difference between 2 arrays
 * by doing a simple item-by-item comparison.
 * This is equivalent to plain diff where only substitutions count.
 *
 * <pre>
 * Typical use:
 * Object[] o1 = {new Character('a')};
 * Object[] o2 = {new Character('b')};
 * System.out.println("Diff = " + SubstDiff.diff(o1, o2));
 * Diff.printdiff(o1, o2, SubstDiff.difflist(o1, o2));
 * </pre>
 *
 * @author Sterling Stuart Stein
 */
public class SubstDiff
{
   /**
    * Find the number of differences between 2 arrays counting only substitutions.
    *
    * @param a The first array
    * @param b The second array
    * @return The number of changes to convert a to b
    */
   public static int diff(Object[] a, Object[] b)
   {
      int len   = Math.min(a.length, b.length);
      int count = a.length + b.length - 2 * len;

      for(int i = 0; i < len; i++)
      {
         if(!a[i].equals(b[i]))
         {
            count++;
         }
      }

      return count;
   }

   /**
    * Find the differences between 2 arrays counting only substitutions.
    *
    * @param a The first array
    * @param b The second array
    * @return An array of pairs of the index of the elements from a and b with -1 representing not present (delete/insert)
    */
   public static int[] difflist(Object[] a, Object[] b)
   {
      IntVector d   = new IntVector();
      int       len = Math.min(a.length, b.length);
      int       i;

      for(i = 0; i < len; i++)
      {
         if(!a[i].equals(b[i]))
         {
            d.add(i);
            d.add(i);
         }
      }

      for(; i < a.length; i++)
      {
         d.add(i);
         d.add(-1);
      }

      for(; i < b.length; i++)
      {
         d.add(-1);
         d.add(i);
      }

      return d.toArray();
   }

   /**
    * Show differences between 2 Strings, for debugging.
    *
    * @param argv The 2 Strings
    */
   public static void main(String[] argv)
   {
      if(argv.length != 2)
      {
         System.err.println("Error: 2 arguments needed");

         return;
      }

      Character[] x = Diff.conv(argv[0]);
      Character[] y = Diff.conv(argv[1]);

      System.out.println("Diff = " + diff(x, y));
      Diff.printdiff(x, y, difflist(x, y));
   }
}
