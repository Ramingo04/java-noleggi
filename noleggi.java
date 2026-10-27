package com.mycompany.mavenproject2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

// ============================================================
// CLASSE MAIN
// ============================================================

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // ====================================================
        // PARTE 1: PROVA DELLA GESTIONE DEI VEICOLI
        // ====================================================

        ArchivioVeicoli archivioVeicoli = new ArchivioVeicoli();
        ArchivioUtenti archivioUtenti = new ArchivioUtenti();

        // Inseriamo alcuni veicoli di esempio.
        // Il VIN è un intero, come nella struttura iniziale.

        archivioVeicoli.inserisciVeicolo(
                new Auto(
                        "AB123CD",
                        5,
                        1600,
                        45.0,
                        "Fiat",
                        101,
                        LocalDate.of(2022, 5, 10)
                )
        );

        archivioVeicoli.inserisciVeicolo(
                new Bici(
                        "city",
                        "Bianchi",
                        102,
                        LocalDate.of(2023, 3, 15)
                )
        );

        archivioVeicoli.inserisciVeicolo(
                new Motocicletta(
                        "XY456ZT",
                        35.0,
                        "Honda",
                        103,
                        LocalDate.of(2021, 8, 20)
                )
        );

        archivioVeicoli.inserisciVeicolo(
                new FurgoneMerci(
                        "EF789GH",
                        12,
                        "Iveco",
                        104,
                        LocalDate.of(2020, 6, 12)
                )
        );

        archivioVeicoli.inserisciVeicolo(
                new Pulmino(
                        "JK012LM",
                        9,
                        "Ford",
                        105,
                        LocalDate.of(2022, 9, 5)
                )
        );

        // Visualizziamo tutti i veicoli.
        System.out.println("===== PARCO VEICOLI =====");
        archivioVeicoli.stampaVeicoli();

        // Ricerca delle auto con una determinata cilindrata
        // e un costo giornaliero non superiore al limite.
        System.out.println("\n===== RICERCA AUTO =====");

        ArrayList<Auto> autoTrovate =
                archivioVeicoli.ricercaAuto(1600, 50.0);

        for (Auto auto : autoTrovate) {
            System.out.println(auto);
        }

        // Creiamo un utente che noleggia un'automobile.
        Utente utente = new Utente(
                "Mario Rossi",
                "mario@gmail.com"
        );

        System.out.println("\n===== NOLEGGIO =====");

        // Mario noleggia il veicolo con VIN 101 per 3 giorni.
        archivioVeicoli.noleggiaVeicolo(
                101,
                utente,
                3,
                archivioUtenti
        );

        // Paghiamo il noleggio del veicolo.
        Veicolo autoNoleggiata =
                archivioVeicoli.cercaVeicolo(101);

        autoNoleggiata.pagaNoleggio();

        // Visualizziamo i noleggi registrati.
        System.out.println("\n===== ARCHIVIO UTENTI =====");
        archivioUtenti.stampaNoleggi();

        // Visualizziamo i soli veicoli noleggiati.
        System.out.println("\n===== VEICOLI NOLEGGIATI =====");
        archivioVeicoli.stampaVeicoliNoleggiati();

        // Mario restituisce l'automobile.
        System.out.println("\n===== RESTITUZIONE =====");

        archivioVeicoli.restituisciVeicolo(
                101,
                archivioUtenti
        );

        System.out.println("\n===== NOLEGGI DOPO LA RESTITUZIONE =====");
        archivioUtenti.stampaNoleggi();

        // ====================================================
        // PARTE 2: ESERCITAZIONE SULLA CONCORRENZA
        // ====================================================

        /*
         * Questo archivio è separato da quello utilizzato prima.
         *
         * Serve a provare il funzionamento concorrente di:
         * - Produttore: inserisce noleggi;
         * - Consumatore: rimuove noleggi;
         * - Lettore: legge lo stato dell'archivio.
         *
         * La simulazione non modifica lo stato reale dei veicoli.
         */

        System.out.println("\n===== TEST THREAD =====");

        ArchivioUtenti archivioThread = new ArchivioUtenti();

        int SIZE = 10;
        int numeroOperazioni = 12;

        Produttore produttore = new Produttore(
                archivioThread,
                numeroOperazioni,
                SIZE
        );

        Consumatore consumatore = new Consumatore(
                archivioThread,
                numeroOperazioni
        );

        Lettore lettore = new Lettore(
                archivioThread,
                15
        );

        // Avviamo i tre thread.
        produttore.start();
        consumatore.start();
        lettore.start();

        // Aspettiamo che tutti e tre abbiano terminato.
        produttore.join();
        consumatore.join();
        lettore.join();

        System.out.println("\n===== FINE PROGRAMMA =====");
        archivioThread.stampaNoleggi();
    }
}


// ============================================================
// INTERFACCIA NOLEGGIABILE
// ============================================================

interface Noleggiabile {

    // Prova a noleggiare il veicolo.
    // Restituisce false se è già noleggiato.
    boolean noleggiaVeicolo(Utente utente, int giorniDiNoleggio);

    // Restituisce il noleggio attuale oppure null.
    Noleggio statoNoleggio();

    // Registra il pagamento del noleggio.
    void pagaNoleggio();

    // Restituisce il veicolo e azzera il noleggio attuale.
    void restituisciVeicolo();
}


// ============================================================
// INTERFACCIA TRASPORTO PASSEGGERI
// ============================================================

interface TrasportoPasseggeri {

    // Restituisce la capienza di passeggeri prevista dal veicolo.
    int capienza_passeggeri();
}


// ============================================================
// CLASSE UTENTE
// ============================================================

class Utente {

    private String nome;
    private String email;

    public Utente(String nome, String email) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome non valido.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail non valida.");
        }

        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Utente{nome=" + nome
                + ", email=" + email + "}";
    }
}


// ============================================================
// CLASSE NOLEGGIO
// ============================================================

class Noleggio {

    private int numGiorni;
    private Utente utente;
    private double costo;
    private int vin;
    private boolean pagato;

    public Noleggio(
            int numGiorni,
            Utente utente,
            double costo,
            int vin) {

        if (numGiorni <= 0) {
            throw new IllegalArgumentException(
                    "Il numero di giorni deve essere positivo."
            );
        }

        if (utente == null) {
            throw new IllegalArgumentException(
                    "L'utente non può essere null."
            );
        }

        if (costo < 0) {
            throw new IllegalArgumentException(
                    "Il costo non può essere negativo."
            );
        }

        if (vin <= 0) {
            throw new IllegalArgumentException(
                    "VIN non valido."
            );
        }

        this.numGiorni = numGiorni;
        this.utente = utente;
        this.costo = costo;
        this.vin = vin;

        // Un noleggio appena creato non è ancora pagato.
        this.pagato = false;
    }

    public int getNumGiorni() {
        return numGiorni;
    }

    public Utente getUtente() {
        return utente;
    }

    public double getCosto() {
        return costo;
    }

    public int getVin() {
        return vin;
    }

    public boolean isPagato() {
        return pagato;
    }

    public void segnaPagato() {
        pagato = true;
    }

    @Override
    public String toString() {
        return "Noleggio{VIN=" + vin
                + ", giorni=" + numGiorni
                + ", utente=" + utente
                + ", costo totale=" + costo
                + ", pagato=" + pagato
                + "}";
    }
}


// ============================================================
// CLASSE ASTRATTA VEICOLO
// ============================================================

abstract class Veicolo implements Noleggiabile {

    private String marca;
    private final int vin;
    private LocalDate dataEmissione;

    /*
     * Il noleggio appartiene al veicolo.
     *
     * Se noleggioAttuale vale null, il veicolo è libero.
     * Se contiene un oggetto Noleggio, il veicolo è noleggiato.
     */
    private Noleggio noleggioAttuale;

    public Veicolo(
            String marca,
            int vin,
            LocalDate dataEmissione) {

        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Marca non valida."
            );
        }

        if (vin <= 0) {
            throw new IllegalArgumentException(
                    "VIN non valido."
            );
        }

        if (dataEmissione == null) {
            throw new IllegalArgumentException(
                    "La data non può essere null."
            );
        }

        this.marca = marca;
        this.vin = vin;
        this.dataEmissione = dataEmissione;
        this.noleggioAttuale = null;
    }

    public String getMarca() {
        return marca;
    }

    public int getVin() {
        return vin;
    }

    public LocalDate getDataEmissione() {
        return dataEmissione;
    }

    /*
     * Ogni tipologia di veicolo calcola il proprio costo
     * giornaliero secondo le regole specifiche della traccia.
     */
    public abstract double getCostoGiornaliero();

    @Override
    public boolean noleggiaVeicolo(
            Utente utente,
            int giorniDiNoleggio) {

        if (utente == null) {
            throw new IllegalArgumentException(
                    "Utente non valido."
            );
        }

        if (giorniDiNoleggio <= 0) {
            throw new IllegalArgumentException(
                    "I giorni devono essere maggiori di zero."
            );
        }

        // Se esiste già un noleggio, il veicolo non è disponibile.
        if (noleggioAttuale != null) {
            return false;
        }

        // Il costo totale è il costo giornaliero per i giorni.
        double costoTotale =
                getCostoGiornaliero() * giorniDiNoleggio;

        // Creiamo il noleggio e lo associamo al veicolo.
        noleggioAttuale = new Noleggio(
                giorniDiNoleggio,
                utente,
                costoTotale,
                vin
        );

        return true;
    }

    @Override
    public Noleggio statoNoleggio() {
        return noleggioAttuale;
    }

    @Override
    public void pagaNoleggio() {

        if (noleggioAttuale == null) {
            throw new IllegalStateException(
                    "Il veicolo non è noleggiato."
            );
        }

        noleggioAttuale.segnaPagato();

        System.out.println(
                "Pagamento registrato. Totale: "
                + noleggioAttuale.getCosto() + " euro."
        );
    }

    @Override
    public void restituisciVeicolo() {

        if (noleggioAttuale == null) {
            throw new IllegalStateException(
                    "Il veicolo non è attualmente noleggiato."
            );
        }

        // Azzerando il noleggio, il veicolo torna disponibile.
        noleggioAttuale = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{marca=" + marca
                + ", VIN=" + vin
                + ", dataEmissione=" + dataEmissione
                + "}";
    }
}


// ============================================================
// CLASSE BICI
// ============================================================

class Bici extends Veicolo {

    private String modello;

    public Bici(
            String modello,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(marca, vin, dataEmissione);

        if (modello == null) {
            throw new IllegalArgumentException(
                    "Modello della bici non valido."
            );
        }

        // Uniformiamo maiuscole e spazi per confrontare il modello.
        modello = modello.trim().toLowerCase();

        if (!modello.equals("city")
                && !modello.equals("mountain bike")
                && !modello.equals("elettrica")) {

            throw new IllegalArgumentException(
                    "Modello bici non riconosciuto."
            );
        }

        this.modello = modello;
    }

    public String getModello() {
        return modello;
    }

    /*
     * Il costo dipende dal modello:
     * city = 20 euro al giorno;
     * mountain bike ed elettrica = 30 euro al giorno.
     */
    @Override
    public double getCostoGiornaliero() {

        if (modello.equals("city")) {
            return 20.0;
        } else if (modello.equals("mountain bike")) {
            return 30.0;
        } else {
            return 30.0;
        }
    }

    @Override
    public String toString() {
        return super.toString()
                + ", modello=" + modello
                + ", costo giornaliero="
                + getCostoGiornaliero() + " euro";
    }
}


// ============================================================
// CLASSE AUTO
// ============================================================

class Auto extends Veicolo implements TrasportoPasseggeri {

    private String targa;
    private int numPosti;
    private double cilindrata;
    private double costoGiornaliero;

    public Auto(
            String targa,
            int numPosti,
            double cilindrata,
            double costoGiornaliero,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(marca, vin, dataEmissione);

        if (targa == null || targa.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Targa non valida."
            );
        }

        if (numPosti <= 0 || cilindrata <= 0
                || costoGiornaliero <= 0) {
            throw new IllegalArgumentException(
                    "Gli attributi dell'auto non sono validi."
            );
        }

        this.targa = targa;
        this.numPosti = numPosti;
        this.cilindrata = cilindrata;
        this.costoGiornaliero = costoGiornaliero;
    }

    public String getTarga() {
        return targa;
    }

    public int getNumPosti() {
        return numPosti;
    }

    public double getCilindrata() {
        return cilindrata;
    }

    @Override
    public double getCostoGiornaliero() {
        return costoGiornaliero;
    }

    @Override
    public int capienza_passeggeri() {
        return numPosti;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", targa=" + targa
                + ", numPosti=" + numPosti
                + ", cilindrata=" + cilindrata
                + ", costo giornaliero=" + costoGiornaliero
                + " euro";
    }
}


// ============================================================
// CLASSE MOTOCICLETTA
// ============================================================

class Motocicletta extends Veicolo {

    private String targa;
    private double costoGiornaliero;

    public Motocicletta(
            String targa,
            double costoGiornaliero,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(marca, vin, dataEmissione);

        if (targa == null || targa.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Targa non valida."
            );
        }

        if (costoGiornaliero <= 0) {
            throw new IllegalArgumentException(
                    "Costo giornaliero non valido."
            );
        }

        this.targa = targa;
        this.costoGiornaliero = costoGiornaliero;
    }

    public String getTarga() {
        return targa;
    }

    @Override
    public double getCostoGiornaliero() {
        return costoGiornaliero;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", targa=" + targa
                + ", costo giornaliero=" + costoGiornaliero
                + " euro";
    }
}


// ============================================================
// CLASSE ASTRATTA FURGONE
// ============================================================

abstract class Furgone extends Veicolo {

    private String targa;

    public Furgone(
            String targa,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(marca, vin, dataEmissione);

        if (targa == null || targa.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Targa non valida."
            );
        }

        this.targa = targa;
    }

    public String getTarga() {
        return targa;
    }
}


// ============================================================
// CLASSE FURGONE PER TRASPORTO MERCI
// ============================================================

class FurgoneMerci extends Furgone {

    private int cubatura;

    public FurgoneMerci(
            String targa,
            int cubatura,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(targa, marca, vin, dataEmissione);

        if (cubatura <= 0) {
            throw new IllegalArgumentException(
                    "La cubatura deve essere positiva."
            );
        }

        this.cubatura = cubatura;
    }

    public int getCubatura() {
        return cubatura;
    }

    /*
     * La traccia stabilisce:
     * costo giornaliero = cubatura * 7 euro.
     */
    @Override
    public double getCostoGiornaliero() {
        return cubatura * 7.0;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", cubatura=" + cubatura
                + ", costo giornaliero="
                + getCostoGiornaliero() + " euro";
    }
}


// ============================================================
// CLASSE PULMINO PER TRASPORTO PERSONE
// ============================================================

class Pulmino extends Furgone implements TrasportoPasseggeri {

    private int numPosti;

    public Pulmino(
            String targa,
            int numPosti,
            String marca,
            int vin,
            LocalDate dataEmissione) {

        super(targa, marca, vin, dataEmissione);

        if (numPosti <= 0) {
            throw new IllegalArgumentException(
                    "Il numero di posti deve essere positivo."
            );
        }

        this.numPosti = numPosti;
    }

    public int getNumPosti() {
        return numPosti;
    }

    @Override
    public int capienza_passeggeri() {
        return numPosti;
    }

    /*
     * La traccia stabilisce:
     * costo giornaliero = numero posti * 10 euro.
     */
    @Override
    public double getCostoGiornaliero() {
        return numPosti * 10.0;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", numPosti=" + numPosti
                + ", costo giornaliero="
                + getCostoGiornaliero() + " euro";
    }
}


// ============================================================
// CLASSE ARCHIVIO VEICOLI
// ============================================================

class ArchivioVeicoli {

    /*
     * L'archivio contiene tutti i veicoli dell'agenzia,
     * non soltanto quelli attualmente noleggiati.
     */
    private ArrayList<Veicolo> veicoli;

    public ArchivioVeicoli() {
        veicoli = new ArrayList<>();
    }

    // Inserisce un veicolo, evitando VIN duplicati.
    public void inserisciVeicolo(Veicolo veicolo) {

        if (veicolo == null) {
            throw new IllegalArgumentException(
                    "Il veicolo non può essere null."
            );
        }

        if (cercaVeicolo(veicolo.getVin()) != null) {
            throw new IllegalArgumentException(
                    "Esiste già un veicolo con questo VIN."
            );
        }

        veicoli.add(veicolo);
    }

    /*
     * Ricerca sequenziale:
     * si scorre la lista e si confronta il VIN.
     *
     * Restituisce null se non trova il veicolo.
     */
    public Veicolo cercaVeicolo(int vin) {

        for (Veicolo veicolo : veicoli) {

            if (veicolo.getVin() == vin) {
                return veicolo;
            }
        }

        return null;
    }

    // Stampa tutti i veicoli.
    public void stampaVeicoli() {

        for (Veicolo veicolo : veicoli) {
            System.out.println(veicolo);
        }
    }

    /*
     * Cerca le auto che hanno la cilindrata richiesta
     * e un costo giornaliero non superiore al massimo.
     *
     * instanceof permette di selezionare soltanto le Auto
     * dall'elenco di oggetti Veicolo.
     */
    public ArrayList<Auto> ricercaAuto(
            double cilindrata,
            double costoMassimoGiornaliero) {

        ArrayList<Auto> risultato = new ArrayList<>();

        for (Veicolo veicolo : veicoli) {

            if (veicolo instanceof Auto) {

                Auto auto = (Auto) veicolo;

                if (auto.getCilindrata() == cilindrata
                        && auto.getCostoGiornaliero()
                        <= costoMassimoGiornaliero) {

                    risultato.add(auto);
                }
            }
        }

        return risultato;
    }

    /*
     * Noleggia il veicolo e registra il noleggio
     * nell'archivio degli utenti.
     */
    public double noleggiaVeicolo(
            int vin,
            Utente utente,
            int giorni,
            ArchivioUtenti archivioUtenti) {

        Veicolo veicolo = cercaVeicolo(vin);

        if (veicolo == null) {
            throw new NoSuchElementException(
                    "Veicolo non trovato."
            );
        }

        if (utente == null || archivioUtenti == null) {
            throw new IllegalArgumentException(
                    "Utente o archivio non valido."
            );
        }

        // Nella variante a liste, impediamo che lo stesso utente
        // abbia più noleggi contemporaneamente.
        if (archivioUtenti.contieneEmail(utente.getEmail())) {
            throw new IllegalStateException(
                    "L'utente ha già un noleggio attivo."
            );
        }

        // Se è già noleggiato, il metodo restituisce false.
        boolean riuscito =
                veicolo.noleggiaVeicolo(utente, giorni);

        if (!riuscito) {
            throw new IllegalStateException(
                    "Il veicolo è già noleggiato."
            );
        }

        Noleggio noleggio = veicolo.statoNoleggio();

        /*
         * Se la registrazione fallisce, ripristiniamo lo stato
         * del veicolo per non lasciarlo occupato senza registrazione.
         */
        try {
            archivioUtenti.registraNoleggio(noleggio);
        } catch (RuntimeException e) {
            veicolo.restituisciVeicolo();
            throw e;
        }

        System.out.println(
                "Noleggio registrato. Costo totale: "
                + noleggio.getCosto() + " euro."
        );

        return noleggio.getCosto();
    }

    /*
     * Restituisce un veicolo:
     * 1. verifica che esista;
     * 2. verifica che sia noleggiato;
     * 3. rimuove il noleggio dall'archivio utenti;
     * 4. azzera il noleggio nel veicolo.
     */
    public void restituisciVeicolo(
            int vin,
            ArchivioUtenti archivioUtenti) {

        Veicolo veicolo = cercaVeicolo(vin);

        if (veicolo == null) {
            throw new NoSuchElementException(
                    "Veicolo non trovato."
            );
        }

        if (veicolo.statoNoleggio() == null) {
            throw new IllegalStateException(
                    "Il veicolo non è noleggiato."
            );
        }

        Noleggio noleggio =
                archivioUtenti.rimuoviNoleggioPerVin(vin);

        if (noleggio == null) {
            throw new IllegalStateException(
                    "Noleggio non presente nell'archivio utenti."
            );
        }

        veicolo.restituisciVeicolo();

        System.out.println(
                "Veicolo con VIN " + vin
                + " restituito correttamente."
        );
    }

    // Visualizza soltanto i veicoli attualmente noleggiati.
    public void stampaVeicoliNoleggiati() {

        for (Veicolo veicolo : veicoli) {

            if (veicolo.statoNoleggio() != null) {
                System.out.println(veicolo);
            }
        }
    }

    // Restituisce una copia della lista.
    public ArrayList<Veicolo> getVeicoli() {
        return new ArrayList<>(veicoli);
    }
}


// ============================================================
// CLASSE ARCHIVIO UTENTI
// ============================================================

class ArchivioUtenti {

    /*
     * Questa variante utilizza una lista di Noleggio.
     * Ogni elemento contiene l'utente, il VIN e i dati
     * del noleggio.
     */
    private ArrayList<Noleggio> noleggi;

    private Random random;

    public ArchivioUtenti() {
        noleggi = new ArrayList<>();
        random = new Random();
    }

    /*
     * I metodi che accedono alla lista sono synchronized.
     *
     * In questo modo un thread per volta può eseguire
     * un'operazione sincronizzata su questo stesso archivio.
     */

    public synchronized void registraNoleggio(Noleggio noleggio) {

        if (noleggio == null) {
            throw new IllegalArgumentException(
                    "Il noleggio non può essere null."
            );
        }

        if (contieneEmailInterno(
                noleggio.getUtente().getEmail())) {

            throw new IllegalStateException(
                    "L'utente ha già un noleggio attivo."
            );
        }

        if (contieneVINInterno(noleggio.getVin())) {

            throw new IllegalStateException(
                    "Il veicolo è già presente tra i noleggi."
            );
        }

        noleggi.add(noleggio);

        // Risveglia eventuali thread in attesa.
        notifyAll();
    }

    public synchronized Noleggio cercaPerEmail(String email) {

        for (Noleggio noleggio : noleggi) {

            if (noleggio.getUtente().getEmail().equals(email)) {
                return noleggio;
            }
        }

        return null;
    }

    public synchronized Noleggio cercaPerVin(int vin) {

        for (Noleggio noleggio : noleggi) {

            if (noleggio.getVin() == vin) {
                return noleggio;
            }
        }

        return null;
    }

    public synchronized boolean contieneEmail(String email) {
        return contieneEmailInterno(email);
    }

    public synchronized boolean contieneVIN(int vin) {
        return contieneVINInterno(vin);
    }

    /*
     * Questi due metodi sono usati internamente dai metodi
     * synchronized. Non devono acquisire un'altra struttura:
     * accedono direttamente alla lista già protetta.
     */
    private boolean contieneEmailInterno(String email) {

        for (Noleggio noleggio : noleggi) {

            if (noleggio.getUtente().getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }

    private boolean contieneVINInterno(int vin) {

        for (Noleggio noleggio : noleggi) {

            if (noleggio.getVin() == vin) {
                return true;
            }
        }

        return false;
    }

    /*
     * Rimuove il noleggio corrispondente al VIN.
     * Restituisce null se non è presente.
     */
    public synchronized Noleggio rimuoviNoleggioPerVin(int vin) {

        for (int i = 0; i < noleggi.size(); i++) {

            if (noleggi.get(i).getVin() == vin) {

                Noleggio rimosso = noleggi.remove(i);

                // Il VIN è nuovamente disponibile.
                notifyAll();

                return rimosso;
            }
        }

        return null;
    }

    /*
     * Metodo utilizzato dal Consumatore.
     *
     * Se la lista è vuota, il Consumatore aspetta.
     * Quando il Produttore inserisce un noleggio, notifyAll()
     * permette al Consumatore di riprendere l'esecuzione.
     */
    public synchronized Noleggio rimuoviPrimoNoleggio()
            throws InterruptedException {

        while (noleggi.isEmpty()) {
            wait();
        }

        Noleggio rimosso = noleggi.remove(0);

        // Comunica agli altri thread che la lista è cambiata.
        notifyAll();

        return rimosso;
    }

    /*
     * Il Produttore sceglie un VIN casuale da 1 a SIZE.
     *
     * Se il VIN è già presente, ne estrae un altro.
     * Se tutti i VIN disponibili sono in uso, aspetta che
     * il Consumatore liberi un posto.
     */
    public synchronized int scegliVINCasualeLibero(int size)
            throws InterruptedException {

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "SIZE deve essere positivo."
            );
        }

        while (true) {

            /*
             * Poiché ogni noleggio attivo deve avere un VIN
             * distinto nell'intervallo 1..SIZE, se tutti
             * i VIN sono in uso bisogna attendere.
             */
            while (noleggi.size() >= size) {
                wait();
            }

            int vin = random.nextInt(size) + 1;

            // Verifichiamo se il VIN estratto è già presente.
            if (!contieneVINInterno(vin)) {
                return vin;
            }

            // Se è presente, il ciclo estrae un nuovo valore.
        }
    }

    /*
     * Restituiamo una copia della lista.
     *
     * Così il Lettore può scorrere uno snapshot senza modificare
     * direttamente la lista condivisa.
     */
    public synchronized ArrayList<Noleggio> getNoleggi() {
        return new ArrayList<>(noleggi);
    }

    public void stampaNoleggi() {

        ArrayList<Noleggio> copia = getNoleggi();

        if (copia.isEmpty()) {
            System.out.println("Nessun noleggio attivo.");
            return;
        }

        for (Noleggio noleggio : copia) {
            System.out.println(noleggio);
        }
    }
}


// ============================================================
// THREAD PRODUTTORE
// ============================================================

class Produttore extends Thread {

    private ArchivioUtenti archivio;
    private int numeroNoleggi;
    private int size;

    public Produttore(
            ArchivioUtenti archivio,
            int numeroNoleggi,
            int size) {

        this.archivio = archivio;
        this.numeroNoleggi = numeroNoleggi;
        this.size = size;
    }

    @Override
    public void run() {

        for (int i = 1; i <= numeroNoleggi; i++) {

            try {

                // Generiamo un VIN libero tra 1 e SIZE.
                int vin =
                        archivio.scegliVINCasualeLibero(size);

                // L'e-mail viene creata con un contatore progressivo.
                String email = "user" + i + "@gmail.com";

                Utente utente = new Utente(
                        "Utente " + i,
                        email
                );

                /*
                 * Qui stiamo simulando esclusivamente l'accesso
                 * concorrente all'ArchivioUtenti.
                 *
                 * Il costo è 0 perché questo noleggio di prova
                 * non è collegato a un veicolo dell'altro archivio.
                 */
                Noleggio noleggio = new Noleggio(
                        1,
                        utente,
                        0.0,
                        vin
                );

                archivio.registraNoleggio(noleggio);

                System.out.println(
                        "PRODUTTORE: inserito "
                        + email + " con VIN " + vin
                );

                Thread.sleep(100);

            } catch (InterruptedException e) {

                // Rispetta l'interruzione del thread.
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}


// ============================================================
// THREAD CONSUMATORE
// ============================================================

class Consumatore extends Thread {

    private ArchivioUtenti archivio;
    private int numeroRestituzioni;

    public Consumatore(
            ArchivioUtenti archivio,
            int numeroRestituzioni) {

        this.archivio = archivio;
        this.numeroRestituzioni = numeroRestituzioni;
    }

    @Override
    public void run() {

        for (int i = 0; i < numeroRestituzioni; i++) {

            try {

                /*
                 * Se non ci sono noleggi, questo metodo aspetta
                 * fino a quando il Produttore ne inserisce uno.
                 */
                Noleggio noleggio =
                        archivio.rimuoviPrimoNoleggio();

                System.out.println(
                        "CONSUMATORE: restituito VIN "
                        + noleggio.getVin()
                        + " dell'utente "
                        + noleggio.getUtente().getEmail()
                );

                Thread.sleep(150);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}


// ============================================================
// THREAD LETTORE
// ============================================================

class Lettore extends Thread {

    private ArchivioUtenti archivio;
    private int numeroLetture;

    public Lettore(
            ArchivioUtenti archivio,
            int numeroLetture) {

        this.archivio = archivio;
        this.numeroLetture = numeroLetture;
    }

    @Override
    public void run() {

        for (int i = 0; i < numeroLetture; i++) {

            /*
             * getNoleggi() restituisce una copia sincronizzata
             * della lista, che può essere letta senza modificare
             * direttamente l'archivio condiviso.
             */
            ArrayList<Noleggio> copia = archivio.getNoleggi();

            System.out.println(
                    "\nLETTORE: lettura numero " + (i + 1)
            );

            if (copia.isEmpty()) {
                System.out.println("Nessun noleggio attivo.");
            } else {
                for (Noleggio noleggio : copia) {
                    System.out.println(noleggio);
                }
            }

            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
