package com.company;

import java.sql.*;
import java.util.ArrayList;

public class id3 {

    private static ArrayList attribut=new ArrayList(){{add("Visibilite");add("Température");add("Humidité");add("Vent");}};

    /**
     * Méthode permet de se connecter avec la base de donnée
     **/

    private static Connection connect() {
        String url ="jdbc:sqlite:id3.db";
        Connection c = null;
        try {
            c = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return c;
    }

    /**
     * Méthode permet de Calculer logarithme base 2
     **/

    private static double log_2(double X){
        double log;
        if (X==0){
            return 0;
        }else {
            log = ((Math.log10(X)) / (Math.log10(2)));
            return log;
        }
    }
    /**
     * Méthode qui calcule l'entropie de l'ensemble de données S [𝑬𝒏𝒕𝒓𝒐𝒑𝒊𝒆(𝑷)= −Σ𝒑𝒊 × 𝒍𝒐𝒈(𝒑𝒊)]
     **/
    private static double Entropie_S(String table) {
        double entropie = 0;
        try (Connection c = connect()) {
            PreparedStatement ps, ps1, ps2;
            double P1;
            double P2;
            ResultSet rc, rc1, rc2;
            String sql_all = "select count(class) from " + table;
            String sql_Yes = "select count(class) from " + table + " where class=\"Oui\"";
            String sql_No  = "select count(class) from " + table + " where class=\"Non\"";

            ps  = c.prepareStatement(sql_all);
            ps1 = c.prepareStatement(sql_Yes);
            ps2 = c.prepareStatement(sql_No);

            rc  = ps.executeQuery();
            rc1 = ps1.executeQuery();
            rc2 = ps2.executeQuery();

            int countAll = rc.getInt(1);
            int countYes = rc1.getInt(1);
            int countNo  = rc2.getInt(1);

            P1 = (double) (countYes) / (countAll);
            P2 = (double) (countNo) / (countAll);
            entropie = -P1 * log_2(P1) - P2 * log_2(P2);
            // System.out.println("L'entopie S est : "+entropie);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return entropie;
    }

    /**
     * Méthode qui permet de calculer l'entropie pour une valeur donnée
     */
    private double entropie(String attribut,String valeur,String table){
        double p1,p2,entropie =0;
        try (Connection c = connect()) {
            PreparedStatement ps_all, ps_yes, ps_no;
            ResultSet rc_all, rc_yes, rc_no;
            String sql_all = "select count(" + attribut + ") from " + table + " where " + attribut + "='" + valeur + "'";
            String sql_yes = "select count(" + attribut + ") from " + table + " where class=\"Oui\" and " + attribut + "='" + valeur + "'";
            String sql_no = "select count(" + attribut + ") from " + table + " where class=\"Non\" and " + attribut + "='" + valeur + "'";

            ps_all = c.prepareStatement(sql_all);
            ps_yes = c.prepareStatement(sql_yes);
            ps_no  = c.prepareStatement(sql_no);

            rc_all = ps_all.executeQuery();
            rc_yes = ps_yes.executeQuery();
            rc_no  = ps_no.executeQuery();

            int count_all = rc_all.getInt(1);
            int count_yes = rc_yes.getInt(1);
            int count_no = rc_no.getInt(1);

            p1 = (double) (count_yes) / (count_all);
            p2 = (double) (count_no) / (count_all);
            entropie = -p1 * log_2(p1) - p2 * log_2(p2);
            //System.out.println("L'entropie de " + valeur + " est:  " + entropie);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return entropie;
    }

    /**
     * Select les valeurs d'un attribut et la mettre dans array list
     **/
    private ArrayList<String> distinct(String table, String attribut) {
        //  String sql = "SELECT DISTINCT " + attribut + "from Jouer";
        ArrayList<String> valeurs = new ArrayList<>();
        Connection c= connect();
        try {
            Statement stmt = c.createStatement();

            String sql=" SELECT DISTINCT " + attribut + " from " + table ;
            ResultSet rc = stmt.executeQuery(sql);
            while (rc.next()){
                String list = rc.getString(1);
                valeurs.add(list);
            }

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        //System.out.println("la liste est "+ valeurs);
        return valeurs;
    }


    /**
     * Méthode qui permet de calculer le gain d'un attribut donnée
     */

    private double Gain(String attribut,ArrayList<String>valeurs,String table){
        double gain = 0;
        double entro = 0;
        double P;
        try (Connection c = connect()) {
            PreparedStatement ps1, ps2;
            ResultSet rc1, rc2;
            for (String valeur : valeurs) {
                String sql     = "select count(" + attribut + ") from " + table + " where " + attribut + "='" + valeur + "'";
                String sql_all = "select count(class) from " + table;

                ps1 = c.prepareStatement(sql);
                ps2 = c.prepareStatement(sql_all);

                rc1 = ps1.executeQuery();
                rc2 = ps2.executeQuery();

                int count = rc1.getInt(1);
                int count_all = rc2.getInt(1);

                P = (double) (count) / (count_all);
                entro -= (P * entropie(attribut, valeur, table));
                gain = Entropie_S(table) + entro;
            }
            //System.out.println("Le gain de " + attribut + " est=" + gain + "\n");
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return gain;
    }

    /**
     * Méthode qui permet de décider si on peut jouer ou non
     */
    private boolean JouerOrNot(String table) {
        boolean test = false;
        try (Connection c = connect()) {
            PreparedStatement ps_all, ps_yes, ps_no;
            ResultSet rc_all, rc_yes, rc_no;
            String sql_all = "select count(class) from " + table;
            String sql_yes = "select count(class) from " + table + " where class=\"Oui\" ";
            String sql_no = "select count(class) from " + table + " where class=\"Non\" ";

            ps_all = c.prepareStatement(sql_all);
            ps_yes = c.prepareStatement(sql_yes);
            ps_no  = c.prepareStatement(sql_no);

            rc_all = ps_all.executeQuery();
            rc_yes = ps_yes.executeQuery();
            rc_no  = ps_no.executeQuery();

            int count_all = rc_all.getInt(1);
            int count_yes = rc_yes.getInt(1);
            int count_no  = rc_no.getInt(1);

            double P_yes = (double) count_yes / count_all;
            double P_no  = (double) count_no / count_all;

            if (P_yes == 1) {
                test = true;
            } else if (P_no == 1) {
                test = false;
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return test;
    }

    /**
     * Méthode permet de retourner la valeur ou l'attribut Jouer égale a Oui d'un noeud donnée
     */
    private String selectValueYes(String table,String noeud) {

        String branch = null;
        try (Connection c = connect()) {
            PreparedStatement ps;
            ResultSet rc;
            String sql =  "select distinct " + noeud + " from " + table + " where class=\"Oui\" ";
            ps = c.prepareStatement(sql);
            rc = ps.executeQuery();
            while (rc.next()) {
                branch = rc.getString(noeud);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return branch;
    }

    /**
     * Méthode permet de retourner la valeur ou l'attribut Jouer égale a Non d'un noeud donnée
     */
    private String selectValueNo(String table,String noeud) {

        String branch = null;
        try (Connection conn = connect()) {
            PreparedStatement  ps;
            ResultSet  rc;
            String sql = "select distinct " + noeud + " from " + table + " where class=\"Non\" ";

            ps = conn.prepareStatement(sql);

            rc = ps.executeQuery();
            while (rc.next()) {
                branch = rc.getString(noeud);
            }

        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return branch;
    }

    /**
     * Méthode qui permet de retourner le noeud qui a le gain le plus élevé
     */

    private String bestGain(String table){
        double max=0;
        String node = null;
        for (int i=0 ; i<attribut.size() ;i++){
            double gain = Gain((String)attribut.get(i),distinct(table,(String)attribut.get(i)),table);
            if (max<gain) {
                max=gain;
                node= (String)attribut.get(i);
            }
        }
        //System.out.println("L'attribut "+node+" a le gain le plus élevé sa valeur est " + max);
        return node;
    }

    /**
     * Méthode permet de retourner la liste des banches d'une table donnée
     */
    private ArrayList listBranch(String table){
        String node=bestGain(table);
        ArrayList branches = null;
        int i=0;
        while (i < attribut.size()) {
            if(node == attribut.get(i)){
                branches= distinct(table,(String)attribut.get(i));
            }
            i++;
        }
        //System.out.println(branches);
        return branches;

    }


    /**
     * Méthode qui permet de creer une view
     */
    private void createView(String branche,String i,String table) {
        Connection c= connect();
        String racine=bestGain(table);

        try {
            Statement stmt = c.createStatement();
            String sql="create view if not exists '" + i + "' as select * from " + table + " where " + racine + "='" + branche + "'";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Méthode qui permet de classifier l'arbre de décision et exprimé sous forme de règles
     */
    private void classification(String table) {

        int i = 0;
        while (i< listBranch(table).size()) {
            createView((String) listBranch(table).get(i), String.valueOf(i), table);
            if (Entropie_S("'" + i + "'") == 0 && JouerOrNot("'" + i + "'")) {
                System.out.println("Si "+bestGain(" Play ")+"= "+listBranch(table).get(i)+" Alors Jouer=Oui");
            } else if (Entropie_S("'" + i + "'") == 0 && !JouerOrNot("'" + i + "'")) {
                System.out.println("Si "+bestGain(" Play ")+"= "+listBranch(table).get(i)+" Alors Jouer=Non");
            }else if (Entropie_S("'" + i + "'")!=0){
                bestGain("'" + i + "'");
                // System.out.println("le noeud est "+ gainMax("'" + i + "'"));

                System.out.println("Si " + bestGain(" Play ") + "= " +listBranch(table).get(i) + " et " + bestGain("'" + i + "'")+
                        " = " + selectValueYes("'" + i + "'",bestGain("'" + i + "'")) + " Alors Jouer = Oui" );

                System.out.println("Si "+bestGain(" Play ")+"= "+listBranch(table).get(i)+" et "+bestGain("'" + i + "'")+
                        " = " + selectValueNo("'" + i + "'",bestGain("'" + i + "'")) + " Alors Jouer = Non" );

            }
            i++;
        }
    }

    public static void main(String[] args) {

        id3 tree=new id3();
        System.out.println("L'entropie de l'ensemble de données S est : "+ Entropie_S("Play")+"\n");
        System.out.println("le noeud racine dans notre arbre de décision est : "+tree.bestGain("Play")+"\n");
        tree.classification("Play");


    }
}