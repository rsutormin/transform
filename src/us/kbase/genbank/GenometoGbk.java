package us.kbase.genbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import us.kbase.common.service.Tuple4;
import us.kbase.kbasegenomes.Contig;
import us.kbase.kbasegenomes.ContigSet;
import us.kbase.kbasegenomes.Feature;
import us.kbase.kbasegenomes.Genome;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Marcin Joachimiak
 * User: marcin
 * Date: 12/17/14
 * Time: 9:41 PM
 */
public class GenometoGbk {

    //NC_009925.jsonp NC_009925_ContigSet.jsonp

    boolean isTest = true;

    Genome genome;
    ContigSet contigSet;

    final static String molecule_type_short = "DNA";
    final static String molecule_type_long = "genome DNA";

    /**
     * @param args
     * @throws Exception
     */
    public GenometoGbk(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        genome = mapper.readValue(args[0], Genome.class);
        //contigSet = mapper.readValue(args[1], ContigSet.class);


        System.out.println(genome.getTaxonomy());


        List<Contig> contigs = contigSet.getContigs();
        for (int j = 0; j < contigs.size(); j++) {

            Contig curcontig = contigs.get(j);
            String out = "";
            //out += "LOCUS       NC_005213             " + curcontig.getLength() + " bp    " + molecule_type_short + "     circular CON 10-JUN-2013\n";
            out += "LOCUS       " + "" + "             " + curcontig.getLength() + " bp    " + molecule_type_short + "     circular CON 10-JUN-2013\n";
            out += "DEFINITION  " + genome.getScientificName() + " chromosome, complete genome.\n";
            out += "ACCESSION   NC_005213\n";
            out += "VERSION     NC_005213.1  GI:38349555\n";
            //out += "DBLINK      Project: 58009\n";
            //out += "            BioProject: PRJNA58009\n";
            out += "KEYWORDS    .\n";
            out += "SOURCE      " + genome.getScientificName() + "\n";
            out += "  ORGANISM  " + genome.getScientificName() + "\n";
            out += "            Archaea; Nanoarchaeota; Nanoarchaeum.\n";


            /*TODO populate references in Genome objects */
            /*
             //typedef tuple<int id, string source_db, string article_title, string link, string pubdate, string authors, string journal_name> publication;
            List<Tuple7<Long, String, String, String, String, String, String>> pubs = genome.getPublications();

            for (int k = 0; k < pubs.size(); k++) {
                Tuple7<Long, String, String, String, String, String, String> curpub = pubs.get(k);

                System.out.println(genome.getTaxonomy());
                System.out.println(curpub.getE6());

                out += "REFERENCE   1  (bases " + 1 + " to " + curcontig.getLength() + ")\n";
                out += "  AUTHORS   ";//Waters,E., Hohn,M.J., Ahel,I., Graham,D.E., Adams,M.D.,\n";

                for(int m=0;m<(curpub.getE6()).length();m++) {
                out+=
                //out += "            Barnstead,M., Beeson,K.Y., Bibbs,L., Bolanos,R., Keller,M.,\n";//59
                }
                out += "  TITLE     "+curpub.getE3()+"\n";//64
                out += "  JOURNAL   "+curpub.getE7()+"\n";
                //TODO missing JOURNAL volume issue pages etc.
                //+" 100 (22), 12984-12988 (2003)\n";
                if (curpub.getE2().equalsIgnoreCase("PUBMED"))
                    out += "   PUBMED   " + curpub.getE1() + "\n";
            }
        */

            //out += "COMMENT     PROVISIONAL REFSEQ: This record has not yet been subject to final\n";
            //out += "            NCBI review. The reference sequence was derived from AE017199.\n";
            //out += "            COMPLETENESS: full length.\n";


            out += "FEATURES             Location/Qualifiers\n";
            out += "     source          1.." + curcontig.getLength() + "\n";
            out += "                     /organism=\"" + genome.getScientificName() + "\"\n";
            out += "                     /mol_type=\"" + molecule_type_long + "\"\n";
            //out += "                     /strain=\"\"\n";
            out += "                     /db_xref=\"taxon:" + genome.getSourceId() + "\"\n";

            List<Feature> features = genome.getFeatures();


            for (int i = 0; i < features.size(); i++) {
                Feature cur = features.get(i);
                List<Tuple4<java.lang.String, java.lang.Long, java.lang.String, java.lang.Long>> location = cur.getLocation();
                cur.getAliases();
                //out += "gene            complement(join(490883..490885,1..879))\n";
                //"location":[["kb|g.0.c.1",3378378,"+",1368]]

                out += "gene            ";
                out += getCDS(out, location);

                //out += "                     /locus_tag=\"NEQ001\"\n";
                //out += "                     /db_xref=\"GeneID:2732620\"\n";
                out += "     CDS             ";
                out += getCDS(out, location);

                //out += "                     /locus_tag=\"NEQ001\"\n";
                out += "                     /note=\"" + cur.getFunction() + "\"\n";
                //out += "                     /codon_start=1\n";
                //out += "                     /transl_table=11\n";
                //out += "                     /product=\"hypothetical protein\"\n";
                //out += "                     /protein_id=\"NP_963295.1\"\n";
                //out += "                     /db_xref=\"GI:41614797\"\n";
                //out += "                     /db_xref=\"GeneID:2732620\"\n";

                out += "                     /translation=\"" + formatString(cur.getProteinTranslation(), 44, 58) + "\n";
                //out += "                     /translation=\"MRLLLELKALNSIDKKQLSNYLIQGFIYNILKNTEYSWLHNWKK\n";
                //out += "                     EKYFNFTLIPKKDIIENKRYYLIISSPDKRFIEVLHNKIKDLDIITIGLAQFQLRKTK\"\n";//58
            }


            out += "ORIGIN\n";
            out += formatDNASequence(curcontig.getSequence(), 10, 60);
            //out += "        1 tctcgcagag ttcttttttg tattaacaaa cccaaaaccc atagaattta atgaacccaa\n";//10

            int start = Math.max(0, args[0].lastIndexOf("/"));
            int end = args[0].indexOf(".", start + 1);
            File outf = new File(args[0].substring(start, end) + "_fromKBaseGenome.gbk");
            PrintWriter pw = new PrintWriter(outf);
            pw.print(out);
            pw.close();

        }
    }

    private String getCDS(String out, List<Tuple4<String, Long, String, Long>> location) {
        int added = 0;
        boolean complement = false;
        for (int n = 0; n < location.size(); n++) {
            Tuple4<String, Long, String, Long> now4 = location.get(n);
            if (added == 0 && now4.getE3().equals("-")) {
                out += "complement(join(";
                complement = true;
            } else {
                out += "join(";
            }

            out += now4.getE2() + ".." + (now4.getE2() + (long) now4.getE4());

            if (added > 0)
                out += ",";
            added++;
        }
        if (complement)
            out += "))\n";
        else {
            out += ")\n";
        }
        return out;
    }


    /**
     * @param s
     * @return
     */
    public String formatString(String s, int one, int two) {
        String out = "";
        boolean first = true;
        int start = 0;
        for (int a = 0; a < s.length(); a++) {
            if (first) {
                out += s.substring(start, start + one);
                first = false;
                start += one;
            } else {
                out += s.substring(start, start + two);
                start += two;
            }
        }

        return out;
    }

    /**
     * @param s
     * @return
     */
    public String formatDNASequence(String s, int charnum, int linenum) {
        String out = "";

        int last = 0;
        //out += "        1 tctcgcagag ttcttttttg tattaacaaa cccaaaaccc atagaattta atgaacccaa\n";//10

        out += "        1 ";
        int index = 1;
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            int end = last + charnum;
            if (end > s.length())
                end = s.length();
            out += s.substring(last, end);
            last += charnum;
            counter++;
            if (counter == 6 && s.length() > end) {
                index += 60;
                String indexStr = "" + index;
                int len = indexStr.length();
                char[] ch = new char[9 - len];
                Arrays.fill(ch, ' ');
                String padStr = new String(ch);
                out += padStr + indexStr + " ";
                counter = 0;
            }
        }

        return out;
    }


    /**
     * @param args
     */
    public final static void main(String[] args) {
        if (args.length == 1 || args.length == 2) {
            try {
                GenometoGbk clt = new GenometoGbk(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("usage: java us.kbase.genbank.GenometoGbk <Genome .json (XXXX.json)> <ContigSet .json (XXXX_ContigSet.json)>");// <convert y/n> <save y/n>");
        }
    }

}


 /*
LOCUS       NC_005213             490885 bp    DNA     circular CON 10-JUN-2013
 DEFINITION  Nanoarchaeum equitans Kin4-M chromosome, complete genome.
 ACCESSION   NC_005213
 VERSION     NC_005213.1  GI:38349555
 DBLINK      Project: 58009
             BioProject: PRJNA58009
 KEYWORDS    .
 SOURCE      Nanoarchaeum equitans Kin4-M
   ORGANISM  Nanoarchaeum equitans Kin4-M
             Archaea; Nanoarchaeota; Nanoarchaeum.
 REFERENCE   1  (bases 1 to 490885)
   AUTHORS   Waters,E., Hohn,M.J., Ahel,I., Graham,D.E., Adams,M.D.,
             Barnstead,M., Beeson,K.Y., Bibbs,L., Bolanos,R., Keller,M.,
             Kretz,K., Lin,X., Mathur,E., Ni,J., Podar,M., Richardson,T.,
             Sutton,G.G., Simon,M., Soll,D., Stetter,K.O., Short,J.M. and
             Noordewier,M.
   TITLE     The genome of Nanoarchaeum equitans: insights into early archaeal
             evolution and derived parasitism
   JOURNAL   Proc. Natl. Acad. Sci. U.S.A. 100 (22), 12984-12988 (2003)
    PUBMED   14566062
 REFERENCE   2  (bases 1 to 490885)
   CONSRTM   NCBI Genome Project
   TITLE     Direct Submission
   JOURNAL   Submitted (17-NOV-2003) National Center for Biotechnology
             Information, NIH, Bethesda, MD 20894, USA
 REFERENCE   3  (bases 1 to 490885)
   CONSRTM   NCBI Microbial Genomes Annotation Project
   TITLE     Direct Submission
   JOURNAL   Submitted (25-JUN-2001) National Center for Biotechnology
             Information, NIH, Bethesda, MD 20894, USA
 COMMENT     PROVISIONAL REFSEQ: This record has not yet been subject to final
             NCBI review. The reference sequence was derived from AE017199.
             COMPLETENESS: full length.
 FEATURES             Location/Qualifiers
      source          1..490885
                      /organism="Nanoarchaeum equitans Kin4-M"
                      /mol_type="genomic DNA"
                      /strain="Kin4-M"
                      /db_xref="taxon:228908"
 gene            complement(486423..486962)
                 /locus_tag="NEQ550"
                 /db_xref="GeneID:2732580"
 CDS             complement(486423..486962)
                 /locus_tag="NEQ550"
                 /codon_start=1
                 /transl_table=11
                 /product="hypothetical protein"
                 /protein_id="NP_963830.1"
                 /db_xref="GI:41615332"
                 /db_xref="GeneID:2732580"
                 /translation="MLELLAGFKQSILYVLAQFKKPEYATSYTIKLVNPFYYISDSLN
                 VITSTKEDKVNYKVSLSDIAFDFPFKFPIVAIVEGKANREFTFIIDRQNKKLSYDLKK
                 GIIYIQDATIIPNGIKITVNGLAELKNIKINPNDPSITVQKVVGEQNTYIIKTSKDSV
                 KITISADFVVKAEKWLFIQ"
 promoter        486983..486988
                 /note="archaeal RNA pol III promoter consensus box A
                 motif"
 misc_feature    487009..487022
                 /locus_tag="NEQ_t33"
                 /note="reverse complementary sequence cleaved during
                 processing of trans-spliced tRNAs"
ORIGIN
    1 tctcgcagag ttcttttttg tattaacaaa cccaaaaccc atagaattta atgaacccaa
   61 accgcaatcg tacaaaaatt tgtaaaattc tctttcttct ttgtctaatt ttctataaac
  121 atttaactct ttccataatg tgcctatata tactgcttcc cctctgttaa ttcttattct
  */