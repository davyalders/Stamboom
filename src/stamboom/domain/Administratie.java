package stamboom.domain;

import java.io.Serializable;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Administratie implements Serializable {

    //************************datavelden*************************************
    private int nextGezinsNr;
    private int nextPersNr;
    private List<Persoon> personen;
    private List<Gezin> gezinnen;
    private transient ObservableList<Persoon> observablePersonen;
    private transient ObservableList<Gezin> observableGezinnen;

    //***********************constructoren***********************************
    /**
     * er wordt een lege administratie aangemaakt.
     * personen en gezinnen die in de toekomst zullen worden gecreeerd, worden
     * (apart) opvolgend genummerd vanaf 1
     */
    public Administratie() {
        //todo opgave 1 - Completed
        nextPersNr = 1;
        nextGezinsNr = 1;
        personen = new ArrayList<>();
        gezinnen = new ArrayList<>();
        setObservableLists();
    }
    
        public void setObservableLists()
    {
        observablePersonen = FXCollections.observableList(personen);
        observableGezinnen = FXCollections.observableList(gezinnen);
    }

    //**********************methoden****************************************
    /**
     * er wordt een persoon met de gegeven parameters aangemaakt; de persoon
     * krijgt een uniek nummer toegewezen, en de persoon is voortaan ook bij het
     * (eventuele) ouderlijk gezin bekend. Voor de voornamen, achternaam en
     * gebplaats geldt dat de eerste letter naar een hoofdletter en de resterende
     * letters naar kleine letters zijn geconverteerd; het tussenvoegsel is in
     * zijn geheel geconverteerd naar kleine letters; overbodige spaties zijn
     * verwijderd
     *
     * @param geslacht
     * @param vnamen vnamen.length>0; alle strings zijn niet leeg
     * @param anaam niet leeg
     * @param tvoegsel mag leeg zijn
     * @param gebdat
     * @param gebplaats niet leeg
     * @param ouderlijkGezin mag de waarde null (=onbekend) hebben
     *
     * @return de nieuwe persoon.
     * Als de persoon al bekend was (op basis van combinatie van getNaam(),
     * geboorteplaats en geboortedatum), wordt er null geretourneerd.
     */
    public Persoon addPersoon(Geslacht geslacht, String[] vnamen, String anaam,
            String tvoegsel, Calendar gebdat,
            String gebplaats, Gezin ouderlijkGezin) {

        if (vnamen.length == 0) {
            throw new IllegalArgumentException("ten minste 1 voornaam");
        }
        for (String voornaam : vnamen) {
            if (voornaam.trim().isEmpty()) {
                throw new IllegalArgumentException("lege voornaam is niet toegestaan");
            }
        }

        if (anaam.trim().isEmpty()) {
            throw new IllegalArgumentException("lege achternaam is niet toegestaan");
        }

        if (gebplaats.trim().isEmpty()) {
            throw new IllegalArgumentException("lege geboorteplaats is niet toegestaan");
        }
        
        //todo opgave 1
        Persoon newPersoon = new Persoon(nextPersNr, vnamen, anaam, tvoegsel, gebdat, gebplaats, geslacht, ouderlijkGezin);
        
        for(Persoon p : this.getPersonenMetAchternaam(newPersoon.getAchternaam()))
        {
            if(p.getNaam().equals(newPersoon.getNaam()) && p.getGebPlaats().equals((newPersoon.getGebPlaats())))
            {
                return null;
            }
        }
        this.personen.add(newPersoon);
        nextPersNr++;
        return newPersoon;
    }
      

    /**
     * er wordt, zo mogelijk (zie return) een (kinderloos) ongehuwd gezin met
     * ouder1 en ouder2 als ouders gecreeerd; de huwelijks- en scheidingsdatum
     * zijn onbekend (null); het gezin krijgt een uniek nummer toegewezen; dit
     * gezin wordt ook bij de afzonderlijke ouders geregistreerd;
     *
     * @param ouder1
     * @param ouder2 mag null zijn
     *
     * @return het nieuwe gezin. null als ouder1 = ouder2 of als een van de volgende
     * voorwaarden wordt overtreden:
     * 1) een van de ouders is op dit moment getrouwd
     * 2) het koppel vormt al een ander gezin
     */
    public Gezin addOngehuwdGezin(Persoon ouder1, Persoon ouder2) {
        if (ouder1 == ouder2) {
            return null;
        }

        if (ouder1.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }
        if (ouder2 != null && ouder2.getGebDat().compareTo(Calendar.getInstance()) > 0) {
            return null;
        }

        Calendar nu = Calendar.getInstance();
        if (ouder1.isGetrouwdOp(nu) || (ouder2 != null
                && ouder2.isGetrouwdOp(nu))
                || ongehuwdGezinBestaat(ouder1, ouder2)) {
            return null;
        }

        Gezin gezin = new Gezin(nextGezinsNr, ouder1, ouder2);
        nextGezinsNr++;
        gezinnen.add(gezin);

        ouder1.wordtOuderIn(gezin);
        if (ouder2 != null) {
            ouder2.wordtOuderIn(gezin);
        }
        return gezin;
    }

    /**
     * Als het ouderlijk gezin van persoon nog onbekend is dan wordt
     * persoon een kind van ouderlijkGezin, en tevens wordt persoon als kind
     * in dat gezin geregistreerd. Als de ouders bij aanroep al bekend zijn,
     * verandert er niets
     *
     * @param persoon
     * @param ouderlijkGezin
     * @return of ouderlijk gezin kon worden toegevoegd.
     */
    public boolean setOuders(Persoon persoon, Gezin ouderlijkGezin) {
        return persoon.setOuders(ouderlijkGezin);
    }

    /**
     * als de ouders van dit gezin gehuwd zijn en nog niet gescheiden en datum
     * na de huwelijksdatum ligt, wordt dit de scheidingsdatum. Anders gebeurt
     * er niets.
     *
     * @param gezin
     * @param datum
     * @return true als scheiding geaccepteerd, anders false
     */
    public boolean setScheiding(Gezin gezin, Calendar datum) {
        return gezin.setScheiding(datum);
    }

    /**
     * registreert het huwelijk, mits gezin nog geen huwelijk is en beide
     * ouders op deze datum mogen trouwen (pas op: het is niet toegestaan dat een
     * ouder met een toekomstige (andere) trouwdatum trouwt.)
     *
     * @param gezin
     * @param datum de huwelijksdatum
     * @return false als huwelijk niet mocht worden voltrokken, anders true
     */
    public boolean setHuwelijk(Gezin gezin, Calendar datum) {
        return gezin.setHuwelijk(datum);
    }

    /**
     *
     * @param ouder1
     * @param ouder2
     * @return true als dit koppel (ouder1,ouder2) al een ongehuwd gezin vormt
     */
    boolean ongehuwdGezinBestaat(Persoon ouder1, Persoon ouder2) {
        return ouder1.heeftOngehuwdGezinMet(ouder2) != null;
    }

    /**
     * als er al een ongehuwd gezin voor dit koppel bestaat, wordt het huwelijk
     * voltrokken, anders wordt er zo mogelijk (zie return) een (kinderloos)
     * gehuwd gezin met ouder1 en ouder2 als ouders gecreeerd; de
     * scheidingsdatum is onbekend (null); het gezin krijgt een uniek nummer
     * toegewezen; dit gezin wordt ook bij de afzonderlijke ouders
     * geregistreerd;
     *
     * @param ouder1
     * @param ouder2
     * @param huwdatum
     * @return null als ouder1 = ouder2 of als een van de ouders getrouwd is
     * anders het gehuwde gezin
     */
    public Gezin addHuwelijk(Persoon ouder1, Persoon ouder2, Calendar huwdatum) {
        //todo opgave 1
        Gezin newGezin = null;
        if (ouder1 == ouder2)
        {
            return null;
        }
        
        for(Gezin g : gezinnen)
        {
            if(g.getOuder1() == ouder1 || g.getOuder2() == ouder1)
            {
                if(g.getHuwelijksdatum() != null && (g.getScheidingsdatum() == null || huwdatum.before(g.getScheidingsdatum())))
                {
                    return null;
                }
            }
            if(g.getOuder1() == ouder2 || g.getOuder2() == ouder2)
            {
                if(g.getHuwelijksdatum() != null && (g.getScheidingsdatum() == null || huwdatum.before(g.getScheidingsdatum())))
                {
                    return null;
                }
            }
        }
        
        for(Persoon p : personen)
        {
            if(p.equals(ouder1))
            {
                newGezin = ouder1.heeftOngehuwdGezinMet(ouder2);
                Calendar gebDat = ouder1.getGebDat();
                int jaar = gebDat.get(Calendar.YEAR);
                if(newGezin != null)
                {

                    if(jaar < 1997)
                    {
                        ouder1.heeftOngehuwdGezinMet(ouder2).setHuwelijk(huwdatum);
                    }
                    else
                    {
                        throw new IllegalArgumentException("persoon is minderjarig");
                    }
                }
                else if(jaar < 1997)
                {
                    newGezin = new Gezin(this.nextGezinsNr, ouder1, ouder2);
                    newGezin.setHuwelijk(huwdatum);
                    ouder1.wordtOuderIn(newGezin);
                    ouder2.wordtOuderIn(newGezin);
                    this.gezinnen.add(newGezin);
                    this.nextGezinsNr++;
                }
            }
        }
        return newGezin;
    }

    /**
     *
     * @return het aantal geregistreerde personen
     */
    public int aantalGeregistreerdePersonen() {
        return nextPersNr;
    }

    /**
     *
     * @return het aantal geregistreerde gezinnen
     */
    public int aantalGeregistreerdeGezinnen() {
        return nextGezinsNr;
    }

    /**
     *
     * @param nr
     * @return de persoon met nummer nr, als die niet bekend is wordt er null
     * geretourneerd
     */
    public Persoon getPersoon(int nr) {
        //todo opgave 1
        //aanname: er worden geen personen verwijderd
       if (personen != null) 
       {
           for (Persoon persoon : personen)
        {
            if(persoon.getNr() == nr)
            {
                return persoon;
            }
        }
       }
        return null;
       
        
    }

    /**
     * @param achternaam
     * @return alle personen met een achternaam gelijk aan de meegegeven
     * achternaam (ongeacht hoofd- en kleine letters)
     */
    public ArrayList<Persoon> getPersonenMetAchternaam(String achternaam) {
        //todo opgave 1
        ArrayList<Persoon> list = new ArrayList<>();
       if (personen != null)
       {
           for(Persoon persoon : personen)
           {
               if(persoon.getAchternaam().toUpperCase().equals(achternaam.toUpperCase()))
               {
                   list.add(persoon);
               }
           }
       }     
       return list;
    }

    /**
     *
     * @return de geregistreerde personen
     */
    public List<Persoon> getPersonen() {
        //todo opgave 1 - Completed
        List<Persoon> personen = new ArrayList<>();
        personen.addAll(this.personen);
        return personen;
    }

    /**
     *
     * @param vnamen
     * @param anaam
     * @param tvoegsel
     * @param gebdat
     * @param gebplaats
     * @return de persoon met dezelfde initialen, tussenvoegsel, achternaam,
     * geboortedatum en -plaats mits bekend (ongeacht hoofd- en kleine letters),
     * anders null
     */
    public Persoon getPersoon(String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats) {
        //todo opgave 1
        Persoon retPerson = null;
        
        for (Persoon p : this.personen)
        {
            if(p.getAchternaam().toLowerCase().equals(anaam.toLowerCase()) &&
               p.getTussenvoegsel().toLowerCase().equals(tvoegsel.toLowerCase()) &&
               p.getInitialen().toLowerCase().equals(setInitialen(vnamen).toLowerCase()) &&
               (p.getGebDat().equals(gebdat) || p.getGebDat() == null) &&
               (p.getGebPlaats().toLowerCase().equals(gebplaats.toLowerCase()) || p.getGebPlaats() == null))
            {
                retPerson = p;
            }
        }
        return retPerson;
    }

    /**
     *
     * @return de geregistreerde gezinnen
     */
    public List<Gezin> getGezinnen() {
        //todo opgave 1 - Completed
        return (ObservableList<Gezin>)FXCollections.unmodifiableObservableList(observableGezinnen);
    }

    /**
     *
     * @param gezinsNr
     * @return het gezin met nummer nr. Als dat niet bekend is wordt er null
     * geretourneerd
     */
    public Gezin getGezin(int gezinsNr) {
        // aanname: er worden geen gezinnen verwijderd
        if (gezinnen != null && 1 <= gezinsNr && 1 <= gezinnen.size()) {
            return gezinnen.get(gezinsNr - 1);
        }
        return null;
    }
    
     /**
     *
     * @return de voorletters van de voornamen; elke voorletter wordt gevolgd
     * door een punt
     */
public String setInitialen(String[] voornamen) {
        //todo opgave 1
        String retInitialen = "";
        
        for (int i = 0; i < voornamen.length; i++)
        {
            retInitialen += voornamen[i].toUpperCase().charAt(0) + ".";
        }
        return retInitialen;
    }
}
